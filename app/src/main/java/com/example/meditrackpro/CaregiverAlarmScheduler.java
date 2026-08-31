package com.example.meditrackpro;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public final class CaregiverAlarmScheduler {

    // Missed dose check IDs: 2_000_000 + (medId * 100 + slotIndex)
    // This range does not overlap with AlarmScheduler (0–999_999) or snooze (1_000_000–1_999_999).
    private static final int MISSED_DOSE_BASE = 2_000_000;

    // Single fixed request code for the recurring daily summary alarm.
    public static final int DAILY_SUMMARY_REQUEST_CODE = 9_999_998;

    private CaregiverAlarmScheduler() {
    }

    /** Schedule a missed-dose check 30 minutes after the dose alarm fires. */
    public static void scheduleMissedDoseCheck(Context context, int medId, int slotIndex, String medName,
                                                String dose, String timeSlot, long doseFireAtMillis) {
        long checkAt = doseFireAtMillis + 30 * 60 * 1_000L;
        int requestCode = MISSED_DOSE_BASE + medId * 100 + slotIndex;

        Intent intent = new Intent(context, MissedDoseCheckReceiver.class);
        intent.putExtra("med_id", medId);
        intent.putExtra("med_name", medName);
        intent.putExtra("med_dose", dose);
        intent.putExtra("time_slot", timeSlot);

        PendingIntent pi = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        try {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, checkAt, pi);
        } catch (SecurityException e) {
            am.set(AlarmManager.RTC_WAKEUP, checkAt, pi);
        }
    }

    /** Cancel a pending missed-dose check for a given medicine slot. */
    public static void cancelMissedDoseCheck(Context context, int medId, int slotIndex) {
        int requestCode = MISSED_DOSE_BASE + medId * 100 + slotIndex;
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = PendingIntent.getBroadcast(
                context, requestCode, new Intent(context, MissedDoseCheckReceiver.class),
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (pi == null) return;
        am.cancel(pi);
    }

    /**
     * Schedule the daily summary alarm for 9:00 PM today (or tomorrow if already past).
     * Safe to call multiple times — uses FLAG_UPDATE_CURRENT.
     */
    public static void scheduleDailySummary(Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 21);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        PendingIntent pi = PendingIntent.getBroadcast(
                context, DAILY_SUMMARY_REQUEST_CODE,
                new Intent(context, DailySummaryReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        try {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
        } catch (SecurityException e) {
            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
        }
    }
}
