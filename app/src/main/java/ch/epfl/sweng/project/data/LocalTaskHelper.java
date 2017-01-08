package ch.epfl.sweng.project.data;

import ch.epfl.sweng.project.Task;
import ch.epfl.sweng.project.TaskListAdapter;
import ch.epfl.sweng.project.User;

/**
 * Local proxy that behave as firebase but does not require
 * any internet connection.
 * Mostly use to mock firebase in order to run tests faster.
 */
public class LocalTaskHelper implements TaskHelper {

    private final TaskListAdapter mAdapter;

    /**
     * Constructor of the class.
     *
     * @param adapter The adapter of the list that need to be edited
     */
    public LocalTaskHelper(TaskListAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public void retrieveAllData(User user, boolean isUnfilled) {
        //Nothing to retrieve when doing tests
    }

    @Override
    public void refreshData(User user, boolean isUnfilled) {
        //Nothing to refresh when doing tests
    }


    @Override
    public void addNewTask(Task task, int position, boolean unfilled) {
        mAdapter.add(task, position);
    }

    @Override
    public void updateTask(Task original, Task updated, int position) {
        deleteTask(original, position);
        addNewTask(updated, position, false);
    }

    @Override
    public void deleteTask(Task task, int position) {
        mAdapter.remove(position);
    }
}
