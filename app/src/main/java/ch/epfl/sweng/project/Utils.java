package ch.epfl.sweng.project;


import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Build;

/**
 * Utility class with methods used throughout the app.
 */
public class Utils extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static final String FIREBASE_PROVIDER = "Firebase";
    public static final String TEST_PROVIDER = "Tests";

    private static Context getContext(){
        return mContext;
    }
    /**
     * Encode a given mail to be compatible with keys in firebase
     *
     * @param mail The user email
     * @return The encoded email
     */
    public static String encodeMailAsFirebaseKey(String mail) {
        return mail.replace('.', ' ');
    }


    /**
     * Look at the fields of the task and determines if the task is not completely filled,
     * and thus need to finish in the inbox of unfinished tasks
     *
     * @param task The task to test
     * @return a boolean whether the task in unfilled or not
     */
    public static boolean isUnfilled(Task task) {

        return isLocationUnfilled(task, getContext())
                || isDurationUnfilled(task) || isDueDateUnfilled(task);
    }

    /**
     * Method that returns true when task's location is the unfilled task's location.
     * The unfilled task's location is "Select one".
     *
     * @param task The task to test
     * @param context The context, needed to retrieve string in string.xml
     * @return true if the location is the unfilled task's location.
     */
    public static boolean isLocationUnfilled(Task task, Context context) {
        return task.getLocationName().equals(context.getString(R.string.select_one));
    }

    /**
     * Method that returns true when task's duration is the unfilled task's duration.
     * The unfilled task's duration is 0.
     *
     * @param task The task to test
     * @return true if the duration is the unfilled task's duration, false otherwise.
     */
    public static boolean isDurationUnfilled(Task task) {
        return task.getDuration() == 0;
    }

    /**
     * Method that returns true when task's due date is the unfilled task's due date.
     * The unfilled task's due date year is 1970.
     *
     * @param task The task to test
     * @return true if the due date is the unfilled task's due date, false otherwise.
     */
    @TargetApi(Build.VERSION_CODES.N)
    public static boolean isDueDateUnfilled(Task task) {
        Calendar c = Calendar.getInstance();
        c.setTime(task.getDueDate());
        int year = c.get(Calendar.YEAR);
        return year == 1970;
    }

    /**
     * tells whether a task has contributors (i.e. task is shared across people)
     *
     * @param task the task to test
     * @return true if the task is shared, false otherwise.
     */
    public static boolean hasContributors(Task task){
        return task.getListOfContributors().size() > 1;
    }

    /**
     * for a task shared with contributors, take care of separating
     * the suffix (@@{creator}@@{sharer}) from the title.
     *
     * @param title the title whom we want to separate the suffix
     * @return an array which element at zero is the title,
     *          and at index 1 is the suffix if it exists,
     *          or the empty string otherwise.
     */
    public static String[] separateTitleAndSuffix(String title){
        String separatorSequence = mContext.getResources().getString(R.string.contributors_separator);
        String[] stringAndSuffix = new String[2];
        if(title.contains(separatorSequence)){
            int charIndex = title.indexOf(separatorSequence);
            stringAndSuffix[0] = title.substring(0, charIndex);
            stringAndSuffix[1] = title.substring(charIndex);
        }else{
            stringAndSuffix[0] = title;
            stringAndSuffix[1] = "";
        }

        return stringAndSuffix;
    }

    /**
     * Given the suffix of a title of shared task, gives back an array containing
     * in the first index the creator of the task, and in its second cell,
     * the person whom this task is shared.
     *
     * @param suffix the suffix to separate the creator and sharer
     * @return and Array containing the creator and the sharer
     */
    public static String[] getCreatorAndSharer(String suffix){
        if(!suffix.isEmpty()) {
            String separatorSequence = mContext.getResources().getString(R.string.contributors_separator);
            String[] creatorAndSharer = new String[2];
            String removedFirstSeparator = suffix.substring(separatorSequence.length());
            creatorAndSharer[0] = removedFirstSeparator.substring(0, removedFirstSeparator.indexOf(separatorSequence));
            creatorAndSharer[1] = removedFirstSeparator.substring(removedFirstSeparator.indexOf(separatorSequence) + separatorSequence.length());
            return creatorAndSharer;
        }else{
            return new String[]{suffix};
        }
    }

    /**
     * Create a shared task title in the form of :
     * title--separatorSequence--creatorEmail--separatorSequence--sharerEmail
     *
     * @param title the visible to the user title of the task
     * @param creatorEmail the email of the person who first created the shared task
     * @param sharerEmail the email of the person possessing the current version of the task.
     *
     * @return the database ready constructed shared task's title.
     */
    public static String constructSharedTitle(String title, String creatorEmail, String sharerEmail){
        return title + mContext.getResources().getString(R.string.contributors_separator) +
                encodeMailAsFirebaseKey(creatorEmail) +
                mContext.getResources().getString(R.string.contributors_separator) +
                encodeMailAsFirebaseKey(sharerEmail);
    }

    /**
     * Using hashcode will produce a unique color for the name
     * of the user to display in the chat.
     *
     * @param userName the user Name (the real one, not the email)
     *
     * @return an int representing the ARGB value of the uer name to display with.
     */
    public static int generateRandomChatColorAsHex(String userName){
        int hash = userName.hashCode();
        int red = hash & 0x000000FF;
        int green = (hash & 0x000FF000)>> 12;
        int blue = (hash & 0xFF00000)>> 20;
        return Color.argb(200,red, green, blue );
    }


    /**
     * Takes care of preparing a shared task given the email of the shared user,
     * and the original task. Since we can't put personal locations on a shared task,
     * it forces the location to be everywhere.
     *
     * @param task the task to process
     * @param mail the mail of the person shared
     * @return a newly created task having the correct name and the default location.
     */
    public static Task sharedTaskPreProcessing(Task task, String mail){
        String[] title = Utils.separateTitleAndSuffix(task.getName());
        String[] suffix = Utils.getCreatorAndSharer(title[1]);
        Task toAdd;
        if(suffix[0].equals(mail)){
            //in the case where we add the task to the creator, nothing to pre-process.
            toAdd = new Task(task.getName(),task.getDescription(),Utils.getEverywhereLocation(),task.getDueDate(),task.getDuration(),task.getEnergy().toString(),task.getListOfContributors(), 0L, task.getHasNewMessages(), task.getListOfMessages());
        }else{
            String newTitle = Utils.constructSharedTitle(title[0],suffix[0],mail);
            toAdd = new Task(newTitle,task.getDescription(),Utils.getEverywhereLocation(),task.getDueDate(),task.getDuration(),task.getEnergy().toString(),task.getListOfContributors(), task.getIfNewContributor(), task.getHasNewMessages(), task.getListOfMessages());
        }
        return toAdd;
    }

    /**
     * Getter for the string resource of the everywhere location
     *
     * @return the string of the everywhere location
     */
    public static String getEverywhereLocation(){
        return mContext.getResources().getString(R.string.everywhere_location);
    }

    /**
     * Getter for the string resource of the select one location
     *
     * @return the string of the select one location
     */
    public static String getSelectOne(){
        return mContext.getResources().getString(R.string.select_one);
    }
}
