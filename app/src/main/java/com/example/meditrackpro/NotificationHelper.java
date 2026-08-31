package com.example.meditrackpro;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public final class NotificationHelper {

    public static final String CHANNEL_DOSES = "channel_doses";
    public static final String CHANNEL_REFILL = "channel_refill";

    private NotificationHelper() {
    }

    public static void createChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel doses = new NotificationChannel(
                    CHANNEL_DOSES, "Dose Reminders", NotificationManager.IMPORTANCE_HIGH);
            doses.setDescription("Alerts when it's time to take your medication");
            nm.createNotificationChannel(doses);

            NotificationChannel refill = new NotificationChannel(
                    CHANNEL_REFILL, "Refill Reminders", NotificationManager.IMPORTANCE_DEFAULT);
            refill.setDescription("Alerts when medication is running low");
            nm.createNotificationChannel(refill);
        }
    }
}
