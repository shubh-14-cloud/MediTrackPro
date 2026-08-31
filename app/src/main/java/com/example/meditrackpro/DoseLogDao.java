package com.example.meditrackpro;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DoseLogDao {

    @Query("SELECT * FROM dose_logs WHERE date = :date ORDER BY timeSlot ASC")
    LiveData<List<DoseLog>> getLogsForDate(String date);

    @Query("SELECT COUNT(*) FROM dose_logs WHERE date = :date AND taken = 1")
    int countTakenForDate(String date);

    @Query("SELECT COUNT(*) FROM dose_logs WHERE date = :date")
    int countTotalForDate(String date);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLog(DoseLog log);

    @Update
    void updateLog(DoseLog log);

    @Query("SELECT * FROM dose_logs WHERE medicineId = :medId AND date = :date AND timeSlot = :slot LIMIT 1")
    DoseLog getLog(int medId, String date, String slot);

    // Deletes ALL historical logs for a medicine (used on delete)
    @Query("DELETE FROM dose_logs WHERE medicineId = :medId")
    void deleteLogsForMedicine(int medId);

    // Deletes only today's logs for a medicine (used on update, so stale time slots are removed)
    @Query("DELETE FROM dose_logs WHERE medicineId = :medId AND date = :date")
    void deleteLogsForMedicineAndDate(int medId, String date);

    @Query("DELETE FROM dose_logs WHERE date < :cutoff")
    void deleteOldLogs(String cutoff);
}
