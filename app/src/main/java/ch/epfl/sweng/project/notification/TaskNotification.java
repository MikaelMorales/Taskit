package ch.epfl.sweng.project.notification;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.epfl.sweng.project.EntryActivity;
import ch.epfl.sweng.project.R;
import ch.epfl.sweng.project.Task;
import ch.epfl.sweng.project.Utils;
import ch.epfl.sweng.project.receiver.NotificationReceiver;

/**
 * Task which create notifications. It also handle the deletion of a notification.
 */
public class TaskNotification extends AsyncTask<Integer, Void, Void> {
    private final ArrayList<Task> taskList;
    private final Context mContext;

    /**
     * Public constructor of TaskNotification which take the list of tasks
     * and a Context.
     * @param taskList The list of Task
     * @param context The Context of the caller
     */
    public TaskNotification(List<Task> taskList, Context context) {
        this.taskList = new ArrayList<>(taskList);
        mContext = context;
    }

    /**
     * Handle the heavy operations of removing and recreating all
     * notifications on a background thread.
     * @param params The integers representing the numberOfIds
     */
    @Override
    protected Void doInBackground(Integer... params) {
        clearAllNotifications(params[0]);
        createAllNotifications(params[1]);
        return null;
    }

    /**
     * Create a notification given the position of the task in the list.
     * @param id The index of the task in the list.
     */
    public void createUniqueNotification(int id) {
        if(id < taskList.size() && id >= 0) {
            Task task = taskList.get(id);
            scheduleNotification(buildNotification(task), setDelayToNotify(task), id);
        }
    }

    /**
     * Remove all pending notifications.
     * @param numberOfIds Number of notifications pending.
     */
    private void clearAllNotifications(int numberOfIds) {
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        for (int i = 0; i < numberOfIds; i++) {

            Intent notificationIntent = new Intent(mContext, NotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, i, notificationIntent, PendingIntent.FLAG_ONE_SHOT);

            try {
                alarmManager.cancel(pendingIntent);
            } catch (Exception e) {
                Log.e("Alarm", "AlarmManager update was not canceled. " + e.toString());
            }
            pendingIntent.cancel();
        }
    }

    /**
     * Create a notification with different id for all task until the parameter
     * numberOfIds.
     * @param numberOfIds The maximal id of a notification.
     *                    Is normally set to the size of the list.
     */
    private void createAllNotifications(int numberOfIds) {
        for (int i = 0; i < numberOfIds; i++) {
            Task task = taskList.get(i);
            scheduleNotification(buildNotification(task), setDelayToNotify(task), i);
        }
    }

    /**
     * Compute the time when the notification should arrive. It is
     * computed according to the time needed to do the task and the due date of the task.
     * @param task The task we need the delay
     * @return The delay as a long
     */
    private long setDelayToNotify(Task task) {
        Date dueDate = task.getDueDate();
        long timeNeeded = task.getDuration();
        long delay = dueDate.getTime() - System.currentTimeMillis();

        switch ((int) timeNeeded) {
            case 5:
            case 15:
            case 30:
            case 60: {
                delay -= 23.98 * 60 * 60 * 1000L;
                break;
            }
            case 120:
            case 240: {
                delay -= 48 * 60 * 60 * 1000L;
                break;
            }
            case 480: {
                delay -= 96 * 60 * 60 * 1000L;
                break;
            }
            case 960:
            case 1920: {
                delay -= 168 * 60 * 60 * 1000L;
                break;
            }
            case 2400: {
                delay -= 336 * 60 * 60 * 1000L;
                break;
            }
            case 4800: {
                delay -= 672 * 60 * 60 * 1000L;
                break;
            }
            case 9600: {
                delay -= 672 * 60 * 60 * 1000L;
                break;
            }
        }

        return Math.max(0, delay);
    }

    /**
     * Set the alarm on the device given a notification, the delay and the id
     * of the notification.
     * @param notification The notification
     * @param delay The delay to notify
     * @param id The id of the notification
     */
    private void scheduleNotification(Notification notification, long delay, int id) {
        if (delay >= 0) {
            Intent notificationIntent = new Intent(mContext, NotificationReceiver.class);
            notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, 1);
            notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_KEY, notification);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, id, notificationIntent, PendingIntent.FLAG_ONE_SHOT);

            long futureInMillis = SystemClock.elapsedRealtime() + delay;
            AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            Log.d("TEST", "DELAY : " + delay + " ID : " + id);
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
        }
    }

    /**
     * Build a notification content such as the icon, the title and information
     * of the notification.
     * @param task The task we need to create a notification for.
     *
     * @return The notification of the task given.
     */
    private Notification buildNotification(Task task) {
        // Intent
        Intent openInformationActivity = new Intent(mContext, EntryActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                mContext,
                0,
                openInformationActivity,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        //Notification builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.drawable.ic_event_notification);
        builder.setContentTitle(Utils.separateTitleAndSuffix(task.getName())[0] + mContext.getString(R.string.notification_content_task));
        builder.setContentIntent(resultPendingIntent);
        builder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND);

        // inbox style
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.addLine(mContext.getString(R.string.notification_detail_date) + task.dueDateToString());
        inboxStyle.addLine(mContext.getString(R.string.notification_detail_location) + task.getLocationName());

        // Moves the expanded layout object into the notification object.
        builder.setStyle(inboxStyle);

        return builder.build();
    }
}
