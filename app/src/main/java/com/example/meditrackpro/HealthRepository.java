package com.example.meditrackpro;

import androidx.lifecycle.LiveData;

import java.util.List;

public class HealthRepository {

    private final HealthRecordDao healthRecordDao;

    public HealthRepository(HealthRecordDao healthRecordDao) {
        this.healthRecordDao = healthRecordDao;
    }

    public LiveData<List<HealthRecord>> getAllRecords() {
        return healthRecordDao.getAllRecords();
    }

    public void insertRecord(HealthRecord record) {
        healthRecordDao.insertRecord(record);
    }

    public void updateRecord(HealthRecord record) {
        healthRecordDao.updateRecord(record);
    }

    public void deleteRecord(HealthRecord record) {
        healthRecordDao.deleteRecord(record);
    }

    public List<HealthRecord> getRecordsPaged(int limit, int offset) {
        return healthRecordDao.getRecordsPaged(limit, offset);
    }

    public int countByType(String type) {
        return healthRecordDao.countByType(type);
    }
}
