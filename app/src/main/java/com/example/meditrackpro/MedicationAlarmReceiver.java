package com.example.meditrackpro;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.app.NotificationCompat;

public class MedicationAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String medName = intent.getStringExtra("med_name");
        if (medName == null) return;
        String dose = intent.getStringExtra("med_dose");
        if (dose == null) dose = "";
        String timeSlot = intent.getStringExtra("time_slot");
        if (timeSlot == null) timeSlot = "";
        int notifId = intent.getIntExtra("notif_id", 0);
        boolean isSnooze = intent.getBooleanExtra("is_snooze", false);

        SharedPreferences prefs = SecurePrefs.get(context, PrefsConstants.NOTIF_PREFS);
        if (!prefs.getBoolean("dose_reminders", true)) return;

        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, notifId, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        android.app.Notification notification = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_DOSES)
                .setSmallIcon(R.drawable.ic_pill)
                .setContentTitle("Time for " + medName)
                .setContentText(dose + " · " + timeSlot)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(notifId, notification);

        // Reschedule for the next day and set up missed-dose check (only for regular alarms)
        if (!isSnooze) {
            AlarmScheduler.rescheduleNextDay(context, notifId, medName, dose, timeSlot);

            int medId = intent.getIntExtra("med_id", -1);
            if (medId >= 0) {
                int slotIndex = notifId % AlarmScheduler.SLOTS_PER_MEDICINE;
                CaregiverAlarmScheduler.scheduleMissedDoseCheck(
                        context, medId, slotIndex, medName, dose, timeSlot, System.currentTimeMillis());
            }
        }
    }
}
