package com.example.meditrackpro;

import androidx.lifecycle.LiveData;

import java.util.List;

public class MedicineRepository {

    private final MedicineDao medicineDao;
    private final DoseLogDao doseLogDao;

    public MedicineRepository(MedicineDao medicineDao, DoseLogDao doseLogDao) {
        this.medicineDao = medicineDao;
        this.doseLogDao = doseLogDao;
    }

    public LiveData<List<Medicine>> getAllMedicines() {
        return medicineDao.getAllMedicines();
    }

    public long insertMedicine(Medicine medicine) {
        return medicineDao.insertMedicine(medicine);
    }

    public void updateMedicine(Medicine medicine) {
        medicineDao.updateMedicine(medicine);
    }

    public void deleteMedicine(Medicine medicine) {
        medicineDao.deleteMedicine(medicine);
    }

    public Medicine getMedicineById(int id) {
        return medicineDao.getMedicineById(id);
    }

    public void deleteMedicineById(int id) {
        medicineDao.deleteMedicineById(id);
    }

    public void insertLog(DoseLog log) {
        doseLogDao.insertLog(log);
    }

    public void updateLog(DoseLog log) {
        doseLogDao.updateLog(log);
    }

    public LiveData<List<DoseLog>> getLogsForDate(String date) {
        return doseLogDao.getLogsForDate(date);
    }

    public DoseLog getLog(int medId, String date, String slot) {
        return doseLogDao.getLog(medId, date, slot);
    }

    public void deleteLogsForMedicine(int medId) {
        doseLogDao.deleteLogsForMedicine(medId);
    }

    public void deleteLogsForMedicineAndDate(int medId, String date) {
        doseLogDao.deleteLogsForMedicineAndDate(medId, date);
    }
}
