package ch.epfl.sweng.project.data;


import android.content.Context;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import ch.epfl.sweng.project.R;
import ch.epfl.sweng.project.Task;
import ch.epfl.sweng.project.Utils;
import ch.epfl.sweng.project.chat.Message;
import ch.epfl.sweng.project.chat.MessageAdapter;

/**
 * Proxy that does all the work between the app and the firebase real time database.
 * It allows the user to fetch the messages from the database and to push
 * a new message into the database.
 *
 * Note: The queries are done asynchronously
 */
public class FirebaseChatHelper {

    private Query mQuery;
    private ValueEventListener mListener;

    private final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private final MessageAdapter mAdapter;
    private final Context mContext;

    /**
     * Constructor of the class
     *
     * @param context The context of the class
     * @param adapter The adapter of the list that need to be edited
     */
    public FirebaseChatHelper(Context context, MessageAdapter adapter) {
        mAdapter = adapter;
        mContext = context;
    }

    /**
     * Retrieve the messages from firebase.
     *
     * @param mail The mail of the user
     * @param task The task we need to recover the messages from
     */
    public void retrieveMessages(final String mail, final Task task) {
        final Query mChat = mDatabase.child("tasks").child(Utils.encodeMailAsFirebaseKey(mail)).child(task.getName()).getRef();
        if(mChat != null) {
            mChat.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mQuery = mChat;
                    mListener = this;
                    retrieveListOfMessages(dataSnapshot, mail, task);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        }
    }

    /**
     * Change the value that define if the user has unread
     * messages in the database.
     *
     * @param mail The mail of the user
     * @param task The corresponding task
     */
    public void setNewMessagesHasRead(String mail, Task task) {
        task.setHasNewMessages(false);
        DatabaseReference taskRef = mDatabase.child("tasks").child(Utils.encodeMailAsFirebaseKey(mail)).child(task.getName()).getRef();
        taskRef.setValue(task);
    }

    /**
     * Push the given new message on the database.
     *
     * @param task The task that has a new message
     * @param newMessage The new message
     * @param myEmail The mail of the user
     */
    public void updateChat(Task task, Message newMessage, String myEmail) {
        task.addMessage(newMessage);
        if(Utils.hasContributors(task)){
            for (String mail : task.getListOfContributors()) {
                Task updateTask = Utils.sharedTaskPreProcessing(task, mail);
                updateTask.setHasNewMessages(!myEmail.equals(mail));
                DatabaseReference taskRef = mDatabase.child("tasks").child(Utils.encodeMailAsFirebaseKey(mail)).child(updateTask.getName()).getRef();
                taskRef.setValue(updateTask);
            }
        }else{
            DatabaseReference taskRef = mDatabase.child("tasks").child(Utils.encodeMailAsFirebaseKey(task.getListOfContributors().get(0))).child(task.getName()).getRef();
            taskRef.setValue(task);
        }
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Remove firebase's listener
     */
    public void removeListener() {
        if(mQuery != null && mListener != null) {
            mQuery.removeEventListener(mListener);
        }
    }

    /**
     * Private method that reconstruct the list of message from a task
     * on the database
     * @param dataSnapshot The data recovered from database
     * @param mail The mail of the user
     * @param task The corresponding task
     */
    private void retrieveListOfMessages(DataSnapshot dataSnapshot, String mail, Task task) {
        if(dataSnapshot.getChildrenCount() == 0) {
            Toast.makeText(mContext, mContext.getString(R.string.no_messages), Toast.LENGTH_SHORT).show();
        }else {
            mAdapter.clear();
            GenericTypeIndicator<List<Message>> gen = new GenericTypeIndicator<List<Message>>() {};
            List<Message> newListOfMessages = dataSnapshot.child("listOfMessages").getValue(gen);
            if(newListOfMessages != null) {
                task.setListOfMessages(newListOfMessages);
                setNewMessagesHasRead(mail, task);
                mAdapter.addAll(newListOfMessages);
                mAdapter.notifyDataSetChanged();
            }
            task.setHasNewMessages(false);
        }
    }
}
