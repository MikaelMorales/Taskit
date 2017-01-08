package ch.epfl.sweng.project.data;

import android.content.Context;

import java.util.ArrayList;

import ch.epfl.sweng.project.Task;
import ch.epfl.sweng.project.TaskListAdapter;
import ch.epfl.sweng.project.Utils;

/**
 * Class that decide which provider the app use in
 * order to manipulate tasks in the database
 */
public class TaskProvider {

    public static String mProvider = Utils.FIREBASE_PROVIDER;
    private final TaskListAdapter mAdapter;
    private final ArrayList<Task> mTaskList;
    private final Context mContext;

    /**
     * Constructor of the TaskProvider.
     *
     * @param context The context in which the taskList is.
     * @param adapter The Adapter of the taskList
     * @param taskList The list of tasks
     */
    public TaskProvider(Context context, TaskListAdapter adapter, ArrayList<Task> taskList) {
        mAdapter = adapter;
        mTaskList = taskList;
        mContext = context;
    }

    /**
     * Getter that return the Proxy to reach
     * the database
     * @return TaskHelper, the proxy
     */
    public TaskHelper getTaskProvider() {
        switch (mProvider) {
            case Utils.FIREBASE_PROVIDER:
                return new FirebaseTaskHelper(mContext, mAdapter, mTaskList);
            case Utils.TEST_PROVIDER:
                return new LocalTaskHelper(mAdapter);
            default:
                throw new IllegalArgumentException("This provider does not exists !");
        }
    }


    /**
     * Setter that allow to switch between Providers
     * @param provider should be TaskProvider.FIREBASE_PROVIDER or
     *                 TaskProvider.TEST_PROVIDER
     */
    public static void setProvider(String provider) {
        mProvider = provider;
    }
}
