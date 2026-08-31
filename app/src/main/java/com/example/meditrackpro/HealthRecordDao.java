package com.example.meditrackpro;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface HealthRecordDao {

    @Query("SELECT * FROM health_records ORDER BY id DESC")
    LiveData<List<HealthRecord>> getAllRecords();

    @Query("SELECT COUNT(*) FROM health_records WHERE type = :type")
    int countByType(String type);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRecord(HealthRecord record);

    @Update
    void updateRecord(HealthRecord record);

    @Delete
    void deleteRecord(HealthRecord record);

    @Query("SELECT * FROM health_records ORDER BY date DESC LIMIT :limit OFFSET :offset")
    List<HealthRecord> getRecordsPaged(int limit, int offset);

    @Query("DELETE FROM health_records")
    void clearAll();
}
