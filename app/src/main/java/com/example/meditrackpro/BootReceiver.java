package com.example.meditrackpro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        Context appContext = context.getApplicationContext();
        PendingResult pendingResult = goAsync();
        MedicineDatabase.databaseWriteExecutor.execute(() -> {
            try {
                MedicineDatabase db = MedicineDatabase.getDatabase(appContext);
                List<Medicine> medicines = db.medicineDao().getRawMedicines();
                for (Medicine medicine : medicines) {
                    AlarmScheduler.scheduleForMedicine(appContext, medicine);
                }
            } finally {
                pendingResult.finish();
            }
        });

        CaregiverAlarmScheduler.scheduleDailySummary(context);
    }
}
