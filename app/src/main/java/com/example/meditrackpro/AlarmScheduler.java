package com.example.meditrackpro;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public final class AlarmScheduler {

    // Regular medicine alarms use IDs in range 0–999999 (medicineId * 100 + slotIndex)
    // Snooze alarms use IDs starting at 1_000_000 to avoid collision with regular alarms
    private static final int SNOOZE_BASE = 1_000_000;

    /** Max slots per medicine ID to fit within the 0–999999 alarm ID range */
    public static final int SLOTS_PER_MEDICINE = 100;

    /** Window used to disambiguate snooze alarm IDs within the SNOOZE_BASE range */
    private static final long SNOOZE_ID_WINDOW = 10_000L;

    /** Milliseconds per minute, used for snooze fire-time calculation */
    private static final long MILLIS_PER_MINUTE = 60_000L;

    private AlarmScheduler() {
    }

    public static void scheduleForMedicine(Context context, Medicine medicine) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        String[] slots = medicine.getTimes().split(",");
        for (int index = 0; index < slots.length; index++) {
            String slot = slots[index].trim();
            String[] parts = slot.split(":");
            if (parts.length != 2) continue;
            Integer hour = parseIntOrNull(parts[0]);
            Integer minute = parseIntOrNull(parts[1]);
            if (hour == null || minute == null) continue;

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            int notifId = requestCode(medicine.getId(), index);
            scheduleExact(context, am, notifId, medicine.getId(), medicine.getName(), medicine.getDose(),
                    slot, calendar.getTimeInMillis());
        }
    }

    /** Called from MedicationAlarmReceiver to reschedule the next day's alarm. */
    public static void rescheduleNextDay(Context context, int notifId, String medName, String dose, String timeSlot) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        String[] parts = timeSlot.split(":");
        Integer scheduledHour = parts.length > 0 ? parseIntOrNull(parts[0]) : null;
        Integer scheduledMinute = parts.length > 1 ? parseIntOrNull(parts[1]) : null;
        if (scheduledHour == null) scheduledHour = 0;
        if (scheduledMinute == null) scheduledMinute = 0;

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, scheduledHour);
        calendar.set(Calendar.MINUTE, scheduledMinute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        int medId = notifId / SLOTS_PER_MEDICINE;
        scheduleExact(context, am, notifId, medId, medName, dose, timeSlot, calendar.getTimeInMillis());
    }

    private static void scheduleExact(Context context, AlarmManager am, int notifId, int medId,
                                       String medName, String dose, String timeSlot, long fireAt) {
        Intent intent = new Intent(context, MedicationAlarmReceiver.class);
        intent.putExtra("med_name", medName);
        intent.putExtra("med_dose", dose);
        intent.putExtra("time_slot", timeSlot);
        intent.putExtra("notif_id", notifId);
        intent.putExtra("med_id", medId);

        PendingIntent pi = PendingIntent.getBroadcast(
                context, notifId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        try {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireAt, pi);
        } catch (SecurityException e) {
            am.set(AlarmManager.RTC_WAKEUP, fireAt, pi);
        }
    }

    public static void cancelForMedicine(Context context, Medicine medicine) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        String[] slots = medicine.getTimes().split(",");
        for (int index = 0; index < slots.length; index++) {
            int notifId = requestCode(medicine.getId(), index);
            Intent intent = new Intent(context, MedicationAlarmReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(
                    context, notifId, intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
            if (pi == null) continue;
            am.cancel(pi);
            CaregiverAlarmScheduler.cancelMissedDoseCheck(context, medicine.getId(), index);
        }
    }

    public static void scheduleSnooze(Context context, String medName, String dose, int minutesFromNow) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long fireAt = System.currentTimeMillis() + minutesFromNow * MILLIS_PER_MINUTE;
        // Snooze alarms use 1_000_000+ range; regular alarms use 0–999999
        int notifId = SNOOZE_BASE + (int) ((System.currentTimeMillis() / 1000) % SNOOZE_ID_WINDOW);

        Intent intent = new Intent(context, MedicationAlarmReceiver.class);
        intent.putExtra("med_name", medName);
        intent.putExtra("med_dose", dose);
        intent.putExtra("time_slot", "Snoozed reminder");
        intent.putExtra("notif_id", notifId);
        intent.putExtra("is_snooze", true);

        PendingIntent pi = PendingIntent.getBroadcast(
                context, notifId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        try {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireAt, pi);
        } catch (SecurityException e) {
            am.set(AlarmManager.RTC_WAKEUP, fireAt, pi);
        }
    }

    public static int requestCode(int medicineId, int slotIndex) {
        return medicineId * SLOTS_PER_MEDICINE + slotIndex;
    }

    private static Integer parseIntOrNull(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
