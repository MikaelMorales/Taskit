package ch.epfl.sweng.project.data;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.epfl.sweng.project.MainActivity;
import ch.epfl.sweng.project.R;
import ch.epfl.sweng.project.Task;
import ch.epfl.sweng.project.TaskListAdapter;
import ch.epfl.sweng.project.User;
import ch.epfl.sweng.project.Utils;
import ch.epfl.sweng.project.chat.Message;

import static ch.epfl.sweng.project.Utils.separateTitleAndSuffix;


/**
 * Proxy that does all the work between the app and the firebase real time database.
 * It allows the user to fetch task from the database, he can also remove/edit or
 * add tasks in the database through this interface.
 *
 * Note: The queries are done asynchronously
 */
public class FirebaseTaskHelper implements TaskHelper {

    private static final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private final TaskListAdapter mAdapter;
    private final ArrayList<Task> mTaskList;
    private final Context mContext;

    /**
     * Constructor of the class
     *
     * @param context The context of the class
     * @param adapter The adapter of the list that need to be edited
     * @param taskList The list of tasks
     */
    public FirebaseTaskHelper(Context context, TaskListAdapter adapter, ArrayList<Task> taskList) {
        mAdapter = adapter;
        mTaskList = taskList;
        mContext = context;
    }

    @Override
    public void retrieveAllData(User user, final boolean requestUnfilled) {
        Query myTasks = mDatabase.child("tasks").child(Utils.encodeMailAsFirebaseKey(user.getEmail())).getRef();

        myTasks.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){
                retrieveTasks(dataSnapshot, requestUnfilled);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void refreshData(User user, final boolean requestUnfilled) {
        final Query myTasks = mDatabase.child("tasks").child(Utils.encodeMailAsFirebaseKey(user.getEmail())).getRef();

        myTasks.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                retrieveTasks(dataSnapshot, requestUnfilled);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void addNewTask(Task task, int position, boolean unfilled) {
        Task toAdd = task;
        if(Utils.hasContributors(task)){
            //in this case, it is the duty of newTask/editTaskActivity
            //to give a title with the correct format (title@@email@@email
            for (String mail : task.getListOfContributors()) {
                toAdd = Utils.sharedTaskPreProcessing(task, mail);
                String[] suffix = Utils.getCreatorAndSharer(Utils.separateTitleAndSuffix(toAdd.getName())[1]);
                if(suffix[0].equals(suffix[1])){
                    toAdd.setIfNewContributor(0L);
                }else{
                    toAdd.setIfNewContributor(1L);
                }
                DatabaseReference taskRef = mDatabase.child("tasks").child(Utils.encodeMailAsFirebaseKey(mail)).child(toAdd.getName()).getRef();
                taskRef.setValue(toAdd);
            }
        }else{
            DatabaseReference taskRef = mDatabase.child("tasks").child(Utils.encodeMailAsFirebaseKey(task.getListOfContributors().get(0))).child(task.getName()).getRef();
            taskRef.setValue(toAdd);
        }
        if(!unfilled){
            mAdapter.add(toAdd, position);
        }
    }

    @Override
    public void deleteTask(Task task, int position) {
        if(Utils.hasContributors(task)){
            for (String mail : task.getListOfContributors()) {
                String taskTitle = Utils.sharedTaskPreProcessing(task, mail).getName();
                DatabaseReference taskRef = mDatabase.child("tasks").child(Utils.encodeMailAsFirebaseKey(mail)).child(taskTitle).getRef();
                taskRef.removeValue();
            }
        }else{
            DatabaseReference taskRef = mDatabase.child("tasks").child(Utils.encodeMailAsFirebaseKey(task.getListOfContributors().get(0))).child(task.getName()).getRef();
            taskRef.removeValue();
        }
        mAdapter.remove(position);
    }

    @Override
    public void updateTask(final Task original, final Task updated, int position) {
        if(Utils.hasContributors(updated) || Utils.hasContributors(original)){
            /**
             * This for loop takes care of updating the tasks according to the new
             * contributor list of the updated task.
             * If a new contributor has been added, it will
             * add the updated task to this new contributor.
             */
            for(final String mail : updated.getListOfContributors()){
                if(original.getListOfContributors().contains(mail)){
                    String oldName;
                    if(Utils.hasContributors(original)) {
                        oldName =  Utils.sharedTaskPreProcessing(original, mail).getName();
                    }else{
                        oldName = original.getName();
                    }

                    Task updatedTask = Utils.sharedTaskPreProcessing(updated, mail);
                    //removing the old task
                    DatabaseReference taskRef = mDatabase.child("tasks").child(Utils.encodeMailAsFirebaseKey(mail)).child(oldName).getRef();
                    taskRef.removeValue();

                    //adding the new task.
                    DatabaseReference taskReference = mDatabase.child("tasks").child(Utils.encodeMailAsFirebaseKey(mail)).child(updatedTask.getName()).getRef();
                    taskReference.setValue(updatedTask);

                }else{
                    Task updatedTask = Utils.sharedTaskPreProcessing(updated, mail);
                    updatedTask.setIfNewContributor(1L);
                    DatabaseReference taskRef = mDatabase.child("tasks").child(Utils.encodeMailAsFirebaseKey(mail)).child(updatedTask.getName()).getRef();
                    taskRef.setValue(updatedTask);
                }
            }
            /**
             * Now this for loop takes care of removing the task from the contributor that have been
             * removed from the shared task.
             */
            for(final String mail : original.getListOfContributors()){
                if(!updated.getListOfContributors().contains(mail)){
                    String oldName =  Utils.sharedTaskPreProcessing(original, mail).getName();
                    //removing the old task
                    DatabaseReference taskRef = mDatabase.child("tasks").child(Utils.encodeMailAsFirebaseKey(mail)).child(oldName).getRef();
                    taskRef.removeValue();
                }
            }

            mAdapter.remove(position);
            //we don't want to update the adapter in the case of the updated task has been completed.
            if(!(Utils.isUnfilled(original) && !Utils.isUnfilled(updated))){
                mAdapter.add(updated, position);
            }

        }else{
            //in either the case of no contributors, or no contributors previously but contributors
            //now, we can simply delete then add the task.
            deleteTask(original, position);
            addNewTask(updated, position, false);
        }
    }

    /**
     * Will warn current user of any new changes made by another contributor on his shared tasks.
     *
     * @param mTaskList the list of task to check if changes have been made to.
     */
    private void warnContributor(List<Task> mTaskList) {
        List<Task> taskAddedAsContributor = new ArrayList<>();

        // search task that has been added by someone else:
        for (Task t : mTaskList) {
            if (t.getIfNewContributor() == 1L ) {
                taskAddedAsContributor.add(t);
            }
        }

        if (!taskAddedAsContributor.isEmpty()) {
            // DIALOG
            // Build the Dialog:
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

            builder.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });

            if (taskAddedAsContributor.size() != 1) {
                builder.setMessage(mContext.getString(R.string.added_as_contributor_on_multiple_tasks)+ taskAddedAsContributor.size()
                        + mContext.getString(R.string.ending_of_added_as_contributor_on_multiple_tasks));
            } else {
                builder.setMessage(mContext.getString(R.string.added_as_contributor_on_one_task)
                        + separateTitleAndSuffix(taskAddedAsContributor.get(0).getName())[0]
                        + mContext.getString(R.string.ending_of_added_as_contributor_on_one_text));
            }

            // Set IfNewContributor on Firebase to 0:
            for (Task t : taskAddedAsContributor) {
                if (mTaskList.indexOf(t) >= 0) {
                    updateTask(t, t.setIfNewContributor(0L), mTaskList.indexOf(t));
                }
            }

            // Create the Dialog:
            AlertDialog dialog = builder.create();
            dialog.show();

        }
    }


    /**
     * Reconstruct the tasks from the DataSnapshot given.
     *
     * @param dataSnapshot Data recovered from the database
     */
    private void retrieveTasks(DataSnapshot dataSnapshot, boolean requestUnfilled) {
        mTaskList.clear();
        dataSnapshotTaskParserRetriever(mTaskList, requestUnfilled, dataSnapshot);

        mAdapter.setBackground(requestUnfilled);
        mAdapter.notifyDataSetChanged();
        // Manage the dialog that warn the user that he has been added to a task:
        warnContributor(mTaskList);
        if(!requestUnfilled){
            MainActivity.triggerDynamicSort();
        }
    }

    /**
     * Take care of extracting and "parsing" the data from a database snapshot
     * in order to construct a list of task.
     *
     * @param mTaskList the taskList to fill
     * @param requestUnfilled whether we want unfilled tasks or filled task from the database.
     * @param dataSnapshot the snapshot from the firebase database containing the tasks to extract.
     */
    private static void dataSnapshotTaskParserRetriever(ArrayList<Task> mTaskList, boolean requestUnfilled, DataSnapshot dataSnapshot){
        for (DataSnapshot task : dataSnapshot.getChildren()) {
            List<String> taskNames = new ArrayList<>();
            //to avoid synchronization duplicate.
            for(Task t: mTaskList){
                taskNames.add(t.getName());
            }
            if (task != null) {
                String title = task.child("name").getValue(String.class);
                String description = task.child("description").getValue(String.class);
                Long durationInMinutes = task.child("durationInMinutes").getValue(Long.class);
                String energy = task.child("energy").getValue(String.class);
                Boolean hasNewMessage = task.child("hasNewMessages").getValue(Boolean.class);

                //Define a GenericTypeIndicator to get back properly typed collection
                GenericTypeIndicator<List<String>> stringListTypeIndicator =
                        new GenericTypeIndicator<List<String>>() {};
                List<String> contributors = task.child("listOfContributors").getValue(stringListTypeIndicator);

                //Construct Location object
                String locationName = task.child("locationName").getValue(String.class);
                //Construct the date
                Long date = task.child("dueDate").child("time").getValue(Long.class);
                Date dueDate = new Date(date);
                long newContributor;
                if(task.child("ifNewContributor").getValue() != null){
                    newContributor  = (long) task.child("ifNewContributor").getValue();
                } else {
                    newContributor = 0L;
                }
                //Define a GenericTypeIndicator to get back properly typed collection
                GenericTypeIndicator<List<Message>> messageListTypeIndicator = new GenericTypeIndicator<List<Message>>() {};
                //Construct list of message
                List<Message> listOfMessages = task.child("listOfMessages").getValue(messageListTypeIndicator);
                Task newTask;

                if(listOfMessages == null) {
                    newTask = new Task(title, description, locationName, dueDate, durationInMinutes, energy, contributors, newContributor, hasNewMessage);
                }else{
                    newTask = new Task(title, description, locationName, dueDate, durationInMinutes, energy, contributors, newContributor, hasNewMessage, listOfMessages);
                }
                //will add a new task to the current list only in the case where the task will stay in its current adapter
                // (i.e. will not add it if and unfilled task has been filled, thus leaving current adapter).
                if((requestUnfilled == Utils.isUnfilled(newTask)) && !taskNames.contains(newTask.getName())){
                    mTaskList.add(newTask);
                }
            }
        }
    }

    /**
     *Used to update a single unfilled task, in the case where its location need
     * to be changed by an action on location settings activity. Should only be used in this case,
     * for general purpose, use updateTask instead.
     *
     * @param user information relative to the user to get his data on firebase
     * @param original the task before the location modification
     * @param updated the task with the location modified
     */
    public static void updateUnfilledFromMain(User user, Task original, Task updated){
        DatabaseReference taskRef = mDatabase.child("tasks").child(Utils.encodeMailAsFirebaseKey(user.getEmail())).child(original.getName()).getRef();
        taskRef.removeValue();

        DatabaseReference taskReference = mDatabase.child("tasks").child(Utils.encodeMailAsFirebaseKey(user.getEmail())).child(updated.getName()).getRef();
        taskReference.setValue(updated);
    }

    /**
     * Take care of retrieving only the unfilled task from the database. This function exists
     * for the mainActivity to be able to have the unfilled tasks in order to show the digest and
     * display the button to access unfilled inbox. To retrieve the task in a broader way,
     * use "retrieveAllData" with the corresponding boolean argument.
     *
     *
     * @param user information relative to the user to get his data on firebase
     * @param unfilledTask the list to be filled by the unfilled tasks fetched from firebase
     */
    public static void retrieveUnfilledFromMain(User user, final ArrayList<Task> unfilledTask){
        Query myTasks = mDatabase.child("tasks").child(Utils.encodeMailAsFirebaseKey(user.getEmail())).getRef();
        myTasks.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){
                dataSnapshotTaskParserRetriever(unfilledTask, true, dataSnapshot);
                MainActivity.unfilledSyncFinished = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                MainActivity.unfilledSyncFinished = true;
            }
        });
    }
}