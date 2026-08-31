package com.example.meditrackpro;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "dose_logs",
        indices = {
                @Index(value = {"medicineId", "date", "timeSlot"}, unique = true),
                @Index(value = {"date"})
        }
)
public class DoseLog {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int medicineId;

    @NonNull
    private String date; // "yyyy-MM-dd"

    @NonNull
    private String timeSlot; // e.g. "08:00"

    private boolean taken;

    public DoseLog(int id, int medicineId, @NonNull String date, @NonNull String timeSlot, boolean taken) {
        this.id = id;
        this.medicineId = medicineId;
        this.date = date;
        this.timeSlot = timeSlot;
        this.taken = taken;
    }

    @Ignore
    public DoseLog(int medicineId, @NonNull String date, @NonNull String timeSlot, boolean taken) {
        this(0, medicineId, date, timeSlot, taken);
    }

    @Ignore
    public DoseLog(int medicineId, @NonNull String date, @NonNull String timeSlot) {
        this(0, medicineId, date, timeSlot, false);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(int medicineId) {
        this.medicineId = medicineId;
    }

    @NonNull
    public String getDate() {
        return date;
    }

    public void setDate(@NonNull String date) {
        this.date = date;
    }

    @NonNull
    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(@NonNull String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public boolean isTaken() {
        return taken;
    }

    public void setTaken(boolean taken) {
        this.taken = taken;
    }
}
