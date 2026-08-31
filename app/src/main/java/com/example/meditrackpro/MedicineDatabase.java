package com.example.meditrackpro;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {Medicine.class, HealthRecord.class, DoseLog.class},
        version = 5,
        exportSchema = false
)
public abstract class MedicineDatabase extends RoomDatabase {

    public abstract MedicineDao medicineDao();

    public abstract HealthRecordDao healthRecordDao();

    public abstract DoseLogDao doseLogDao();

    /** Shared background executor for DAO calls that must not run on the main thread. */
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    private static volatile MedicineDatabase INSTANCE;

    // Migration 2->3: no schema change at this stage (explicit, non-destructive)
    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            // No-op: schema unchanged between versions 2 and 3
        }
    };

    // Migration 3->4: add unique index on dose_logs (medicineId, date, timeSlot)
    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_dose_logs_medicineId_date_timeSlot " +
                            "ON dose_logs (medicineId, date, timeSlot)"
            );
        }
    };

    // Migration 4->5: add non-unique index on dose_logs (date) for query performance
    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE INDEX IF NOT EXISTS index_dose_logs_date ON dose_logs (date)");
        }
    };

    public static MedicineDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (MedicineDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    MedicineDatabase.class,
                                    "medicine_database"
                            )
                            .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
