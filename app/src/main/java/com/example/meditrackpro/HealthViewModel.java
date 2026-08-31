package com.example.meditrackpro;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import timber.log.Timber;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HealthViewModel extends AndroidViewModel {

    private final HealthRepository repository;
    private final LiveData<List<HealthRecord>> allRecords;

    private static final int PAGE_SIZE = 20;
    private int currentOffset = 0;

    private final MutableLiveData<List<HealthRecord>> pagedRecords =
            new MutableLiveData<>(Collections.emptyList());

    public HealthViewModel(@NonNull Application application) {
        super(application);
        MedicineDatabase db = MedicineDatabase.getDatabase(application);
        repository = new HealthRepository(db.healthRecordDao());
        allRecords = repository.getAllRecords();
    }

    public LiveData<List<HealthRecord>> getAllRecords() {
        return allRecords;
    }

    public LiveData<List<HealthRecord>> getPagedRecords() {
        return pagedRecords;
    }

    public void loadNextPage() {
        MedicineDatabase.databaseWriteExecutor.execute(() -> {
            try {
                List<HealthRecord> newRecords = repository.getRecordsPaged(PAGE_SIZE, currentOffset);
                if (!newRecords.isEmpty()) {
                    List<HealthRecord> combined = new ArrayList<>(pagedRecords.getValue());
                    combined.addAll(newRecords);
                    pagedRecords.postValue(combined);
                    currentOffset += newRecords.size();
                }
            } catch (Exception e) {
                Timber.e(e, "Error loading next page: %s", e.getMessage());
            }
        });
    }

    public void insert(HealthRecord record) {
        MedicineDatabase.databaseWriteExecutor.execute(() -> {
            try {
                repository.insertRecord(record);
            } catch (Exception e) {
                Timber.e(e, "Error inserting health record: %s", e.getMessage());
            }
        });
    }

    public void update(HealthRecord record) {
        MedicineDatabase.databaseWriteExecutor.execute(() -> {
            try {
                repository.updateRecord(record);
            } catch (Exception e) {
                Timber.e(e, "Error updating health record: %s", e.getMessage());
            }
        });
    }

    public void delete(HealthRecord record) {
        MedicineDatabase.databaseWriteExecutor.execute(() -> {
            try {
                repository.deleteRecord(record);
            } catch (Exception e) {
                Timber.e(e, "Error deleting health record: %s", e.getMessage());
            }
        });
    }
}
