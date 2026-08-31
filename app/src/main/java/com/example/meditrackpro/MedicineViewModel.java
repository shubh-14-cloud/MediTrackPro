package com.example.meditrackpro;

import android.app.Application;
import android.database.sqlite.SQLiteConstraintException;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import timber.log.Timber;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MedicineViewModel extends AndroidViewModel {

    private final MedicineRepository repository;
    private final LiveData<List<Medicine>> allMedicines;

    public MedicineViewModel(@NonNull Application application) {
        super(application);
        MedicineDatabase db = MedicineDatabase.getDatabase(application);
        repository = new MedicineRepository(db.medicineDao(), db.doseLogDao());
        allMedicines = repository.getAllMedicines();
    }

    public LiveData<List<Medicine>> getAllMedicines() {
        return allMedicines;
    }

    public LiveData<List<DoseLog>> todayDoseLogs() {
        return repository.getLogsForDate(today());
    }

    public void insert(Medicine medicine) {
        MedicineDatabase.databaseWriteExecutor.execute(() -> {
            try {
                long id = repository.insertMedicine(medicine);
                Medicine inserted = new Medicine((int) id, medicine.getName(), medicine.getDose(),
                        medicine.getTimes(), medicine.getRefillDays(), medicine.getColor());
                AlarmScheduler.scheduleForMedicine(getApplication(), inserted);
            } catch (Exception e) {
                Timber.e(e, "Error inserting medicine: %s", e.getMessage());
            }
        });
    }

    public void update(Medicine medicine) {
        MedicineDatabase.databaseWriteExecutor.execute(() -> {
            try {
                repository.updateMedicine(medicine);
                AlarmScheduler.cancelForMedicine(getApplication(), medicine);
                AlarmScheduler.scheduleForMedicine(getApplication(), medicine);
                // Remove today's logs for this medicine so ensureDoseLogsForToday
                // recreates them with the new time slots — prevents stale slots showing up
                repository.deleteLogsForMedicineAndDate(medicine.getId(), today());
            } catch (Exception e) {
                Timber.e(e, "Error updating medicine: %s", e.getMessage());
            }
        });
    }

    public void delete(Medicine medicine) {
        MedicineDatabase.databaseWriteExecutor.execute(() -> {
            try {
                AlarmScheduler.cancelForMedicine(getApplication(), medicine);
                repository.deleteLogsForMedicine(medicine.getId());
                repository.deleteMedicine(medicine);
            } catch (Exception e) {
                Timber.e(e, "Error deleting medicine: %s", e.getMessage());
            }
        });
    }

    public void deleteById(int id) {
        MedicineDatabase.databaseWriteExecutor.execute(() -> {
            try {
                Medicine med = repository.getMedicineById(id);
                if (med == null) return;
                AlarmScheduler.cancelForMedicine(getApplication(), med);
                repository.deleteLogsForMedicine(id);
                repository.deleteMedicineById(id);
            } catch (Exception e) {
                Timber.e(e, "Error deleting medicine by id: %s", e.getMessage());
            }
        });
    }

    public void markDoseTaken(Medicine medicine, String timeSlot) {
        MedicineDatabase.databaseWriteExecutor.execute(() -> {
            try {
                String date = today();
                DoseLog existing = repository.getLog(medicine.getId(), date, timeSlot);
                if (existing == null) {
                    repository.insertLog(new DoseLog(medicine.getId(), date, timeSlot, true));
                } else {
                    existing.setTaken(!existing.isTaken());
                    repository.updateLog(existing);
                }
            } catch (Exception e) {
                Timber.e(e, "Error marking dose taken: %s", e.getMessage());
            }
        });
    }

    public void ensureDoseLogsForToday(List<Medicine> medicines) {
        MedicineDatabase.databaseWriteExecutor.execute(() -> {
            try {
                String date = today();
                for (Medicine med : medicines) {
                    for (String rawSlot : med.getTimes().split(",")) {
                        String slot = rawSlot.trim();
                        if (!slot.isEmpty() && repository.getLog(med.getId(), date, slot) == null) {
                            try {
                                repository.insertLog(new DoseLog(med.getId(), date, slot));
                            } catch (SQLiteConstraintException e) {
                                // Duplicate entry — silently ignore
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Timber.e(e, "Error ensuring dose logs for today: %s", e.getMessage());
            }
        });
    }

    public void scheduleSnooze(String medName, String dose, int minutesFromNow) {
        AlarmScheduler.scheduleSnooze(getApplication(), medName, dose, minutesFromNow);
    }

    private String today() {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
