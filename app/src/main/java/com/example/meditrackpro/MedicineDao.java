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
public interface MedicineDao {

    @Query("SELECT * FROM medicines ORDER BY name ASC")
    LiveData<List<Medicine>> getAllMedicines();

    @Query("SELECT * FROM medicines ORDER BY name ASC")
    List<Medicine> getRawMedicines();

    @Query("SELECT * FROM medicines WHERE id = :id")
    Medicine getMedicineById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertMedicine(Medicine medicine);

    @Update
    void updateMedicine(Medicine medicine);

    @Delete
    void deleteMedicine(Medicine medicine);

    @Query("DELETE FROM medicines WHERE id = :id")
    void deleteMedicineById(int id);

    @Query("DELETE FROM medicines")
    void clearAllMedicines();
}
