package com.example.meditrackpro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import timber.log.Timber;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MissedDoseCheckReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int medId = intent.getIntExtra("med_id", -1);
        String medName = intent.getStringExtra("med_name");
        if (medName == null) return;
        String dose = intent.getStringExtra("med_dose");
        if (dose == null) dose = "";
        String timeSlot = intent.getStringExtra("time_slot");
        if (timeSlot == null) return;

        if (medId < 0) return;

        PendingResult pendingResult = goAsync();
        String finalDose = dose;
        MedicineDatabase.databaseWriteExecutor.execute(() -> {
            try {
                SharedPreferences prefs = SecurePrefs.get(context, PrefsConstants.CAREGIVER_PREFS);

                if (!prefs.getBoolean("active", false)) return;
                if (!prefs.getBoolean("missed_dose", true)) return;

                String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
                DoseLog log = MedicineDatabase.getDatabase(context).doseLogDao().getLog(medId, today, timeSlot);

                if (log == null || !log.isTaken()) {
                    String msg = "MediTrack Alert: " + medName + " (" + finalDose + ") scheduled at " + timeSlot + " was not taken.";
                    CaregiverSmsSender.send(context, msg);
                }
            } catch (Exception e) {
                Timber.e(e, "MissedDoseCheckReceiver error");
            } finally {
                pendingResult.finish();
            }
        });
    }
}
