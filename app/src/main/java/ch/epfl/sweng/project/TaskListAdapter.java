package ch.epfl.sweng.project;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;


/**
 * Adapter used to display the task list
 */
public class TaskListAdapter extends RecyclerView.Adapter<ViewHolder> {

    private final Context mContext;
    private final ArrayList<Task> tasksList;

    /**
     * Constructor of the class
     *
     * @param context  Current context contains global information about the application environment.
     * @param taskList The list of tasks
     */
    TaskListAdapter(Context context, ArrayList<Task> taskList) {
        this.tasksList = taskList;
        mContext = context;
    }

    /**
     * Set the background for the filled tasks.
     * If there is no filled tasks, bb8 is displayed.
     */
    public void setBackground(boolean isUnfilled) {
        RecyclerView recyclerView = (RecyclerView) ((Activity) mContext).findViewById(R.id.list_view_tasks);
        if(recyclerView != null) {
            if(!isUnfilled && getItemCount() == 0) {
                recyclerView.setBackgroundResource(R.drawable.db8);
            }else{
                recyclerView.setBackgroundColor(0x00000000);
            }
        }
    }

    /**
     * Sort the list with the given comparator
     * @param comparator A comparator used to sort the list
     */
    public void sort(Comparator<Task> comparator) {
        Collections.sort(tasksList, comparator);
        notifyDataSetChanged();
    }

    /**
     * Remove a task from the ListAdapter
     *
     * @param position the position of the task to be removed
     */
    public void remove(int position) {
        if(position <= tasksList.size() -1 && position >= 0) {
            tasksList.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * Add a task to the ListAdapter
     *
     * @param task the task to add
     * @param position the position at which the task has to be added
     */
    public void add(Task task, int position) {
        if(position == 0 || position >= tasksList.size()) {
            tasksList.add(task);
        } else {
            tasksList.add(position, task);
        }
        notifyItemInserted(position);
    }

    /**
     * Get the size of the tasks list
     *
     * @return the size of the list
     */
    @Override
    public int getItemCount() {
        return tasksList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_task, parent, false);

        // create ViewHolder
        return new ViewHolder(itemLayoutView, mContext, tasksList);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Task taskInTheView = tasksList.get(position);

        if (taskInTheView != null) {
            String titleWithoutSuffix = Utils.separateTitleAndSuffix(taskInTheView.getName())[0];
            holder.taskTitle.setText(titleWithoutSuffix);

            Calendar c = Calendar.getInstance();
            int days = taskInTheView.daysBetween(c.getTime(), taskInTheView.getDueDate());
            if(Utils.isDueDateUnfilled(taskInTheView)){
                holder.taskRemainingDays.setText("-");
            }else{
                if (days > 1) {
                    holder.taskRemainingDays.setText(String.format(Locale.UK, "%d" + mContext.getString(R.string.days_left), days));
                } else if (days == 1) {
                    holder.taskRemainingDays.setText(String.format(Locale.UK, "%d" + mContext.getString(R.string.day_left), days));
                } else if (days == 0) {
                    holder.taskRemainingDays.setText(R.string.due_today);
                } else if (days == -1) {
                    int days_value_for_text = days * -1;
                    holder.taskRemainingDays.setText(String.format(Locale.UK, "%d" + mContext.getString(R.string.day_late), days_value_for_text));
                } else if (days < 1) {
                    int days_value_for_text = days * -1;
                    holder.taskRemainingDays.setText(String.format(Locale.UK, "%d" + mContext.getString(R.string.days_late), days_value_for_text));
                }

                if (days < 10 && days >= 1) {
                    holder.taskRemainingDays.setTextColor(ContextCompat.getColor(mContext, R.color.flat_orange));
                } else if (days < 1) {
                    holder.taskRemainingDays.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                } else {
                    holder.taskRemainingDays.setTextColor(ContextCompat.getColor(mContext, R.color.flat_green));
                }
            }

            if(Utils.isLocationUnfilled(taskInTheView, mContext)){
                holder.taskLocation.setText("-");
            }else{
                holder.taskLocation.setText(taskInTheView.getLocationName());
            }

            if(Utils.isDurationUnfilled(taskInTheView)){
                holder.taskDuration.setText("-");
            }else{
                holder.taskDuration.setText(MainActivity.DURATION_MAP.get((int) taskInTheView.getDuration()));
            }

            if (!Utils.isUnfilled(taskInTheView)) {
                double static_sort_value = taskInTheView.getStaticSortValue();
                double urgency_percentage;
                if(days <= 2 || static_sort_value > 100){
                    urgency_percentage = 100;
                } else {
                    urgency_percentage = static_sort_value;
                }
                float[] hsv = new float[3];
                hsv[0]= (float)Math.floor((100 - urgency_percentage) * 120 / 100);
                hsv[1] = 1;
                hsv[2] = 1;

                //flatten the color (formula : Saturation - 37%, lightness + 3.95% (modified))
                float[] hsl = new float[3];
                ColorUtils.colorToHSL(Color.HSVToColor(hsv), hsl);
                hsl[1] = hsl[1] - (float)0.2 * hsl[1];
                hsl[2] = hsl[2] + (float)0.2 * hsl[2];

                holder.colorIndicator.setBackgroundColor(ColorUtils.HSLToColor(hsl));
            }
            if(Utils.isUnfilled(taskInTheView)){
                holder.colorIndicator.setVisibility(View.INVISIBLE);
            }
            if(!Utils.hasContributors(taskInTheView)) {
                holder.imageSharedTask.setVisibility(View.INVISIBLE);
            } else {
                holder.imageSharedTask.setVisibility(View.VISIBLE);
            }
            if(taskInTheView.getHasNewMessages()) {
                holder.imageHasNewMessages.setVisibility(View.VISIBLE);
            } else {
                holder.imageHasNewMessages.setVisibility(View.INVISIBLE);
            }
        }
    }
}