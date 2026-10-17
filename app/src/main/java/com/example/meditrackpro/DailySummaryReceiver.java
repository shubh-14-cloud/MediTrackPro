package com.example.meditrackpro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import timber.log.Timber;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DailySummaryReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PendingResult pendingResult = goAsync();
        MedicineDatabase.databaseWriteExecutor.execute(() -> {
            try {
                SharedPreferences prefs = SecurePrefs.get(context, PrefsConstants.CAREGIVER_PREFS);

                boolean active = prefs.getBoolean("active", false);
                boolean dailySummary = prefs.getBoolean("daily_summary", false);
                boolean emergency = prefs.getBoolean("emergency", true);

                if (!active || (!dailySummary && !emergency)) return;

                MedicineDatabase db = MedicineDatabase.getDatabase(context);
                String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
                int total = db.doseLogDao().countTotalForDate(today);
                int taken = db.doseLogDao().countTakenForDate(today);
                int missed = total - taken;
                int pct = total > 0 ? (taken * 100) / total : 0;

                if (dailySummary && total > 0) {
                    String msg = "MediTrack Daily Summary: " + taken + "/" + total
                            + " doses taken today (" + pct + "% adherence). " + missed + " missed.";
                    msg = LocationHelper.appendLocation(context, msg);
                    CaregiverSmsSender.send(context, msg);
                }

                // Emergency alert: no doses taken at all today
                if (emergency && total > 0 && taken == 0) {
                    String msg = "MediTrack EMERGENCY: No medications were taken today. Please check on your patient immediately.";
                    msg = LocationHelper.appendLocation(context, msg);
                    CaregiverSmsSender.send(context, msg);
                }
            } catch (Exception e) {
                Timber.e(e, "DailySummaryReceiver error");
            } finally {
                pendingResult.finish();
            }
        });

        // Reschedule for the next day
        CaregiverAlarmScheduler.scheduleDailySummary(context);
    }
}
