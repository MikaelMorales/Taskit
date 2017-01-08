package ch.epfl.sweng.project;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import static ch.epfl.sweng.project.FilledTaskFragment.INDEX_TASK_TO_BE_EDITED_KEY;
import static ch.epfl.sweng.project.FilledTaskFragment.TASKS_LIST_KEY;

/**
 * View holder used by the recycler view. It defines how each line
 * of the recycler view is represented.
 */
public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final Context mContext;
    private final ArrayList<Task> tasksList;
    public final View colorIndicator;
    public final TextView taskDuration;
    public final TextView taskLocation;
    public final TextView taskRemainingDays;
    public final TextView taskTitle;
    public final ImageView imageSharedTask;
    public final ImageView imageHasNewMessages;

    /**
     * Constructor of the class
     * @param v The view
     * @param context The context of the activity
     * @param tasksList The list of tasks
     */
    public ViewHolder(View v, Context context, ArrayList<Task> tasksList) {
        super(v);
        this.tasksList = tasksList;
        mContext = context;
        colorIndicator = v.findViewById(R.id.list_colored_indicator);
        taskDuration = (TextView) v.findViewById(R.id.list_item_duration);
        taskLocation = (TextView) v.findViewById(R.id.list_item_location);
        taskRemainingDays = (TextView) v.findViewById(R.id.list_remaining_days);
        taskTitle = (TextView) v.findViewById(R.id.list_entry_title);
        imageSharedTask = (ImageView) v.findViewById(R.id.imageSharedTask);
        imageHasNewMessages = (ImageView) v.findViewById(R.id.task_new_messages);

        v.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int itemPosition = getAdapterPosition();
        Intent intent = new Intent(mContext, EditTaskActivity.class);
        intent.putExtra(INDEX_TASK_TO_BE_EDITED_KEY, itemPosition);
        intent.putParcelableArrayListExtra(TASKS_LIST_KEY, tasksList);
        ((Activity)mContext).startActivityForResult(intent, FilledTaskFragment.EDIT_TASK_REQUEST_CODE);
    }

}