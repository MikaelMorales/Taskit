package ch.epfl.sweng.project.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Class that extends BroadcastReceiver
 *
 * Receiver that is called when a notification rises.
 * It notifies the user.
 */
public class NotificationReceiver extends BroadcastReceiver{
    public static final String NOTIFICATION_ID = "notification-id";
    public static final String NOTIFICATION_KEY = "notification";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = intent.getParcelableExtra(NOTIFICATION_KEY);
        int id = intent.getIntExtra(NOTIFICATION_ID, 0);

        notificationManager.notify(id, notification);
    }
}
