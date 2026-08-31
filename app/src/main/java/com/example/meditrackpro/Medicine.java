package com.example.meditrackpro;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "medicines")
public class Medicine {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String name;

    @NonNull
    private String dose;

    @NonNull
    private String times; // e.g. "08:00, 20:00"

    private int refillDays;

    @NonNull
    private String color;

    public Medicine(int id, @NonNull String name, @NonNull String dose, @NonNull String times,
                     int refillDays, @NonNull String color) {
        this.id = id;
        this.name = name;
        this.dose = dose;
        this.times = times;
        this.refillDays = refillDays;
        this.color = color;
    }

    @Ignore
    public Medicine(@NonNull String name, @NonNull String dose, @NonNull String times, int refillDays) {
        this(0, name, dose, times, refillDays, "accent");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getDose() {
        return dose;
    }

    public void setDose(@NonNull String dose) {
        this.dose = dose;
    }

    @NonNull
    public String getTimes() {
        return times;
    }

    public void setTimes(@NonNull String times) {
        this.times = times;
    }

    public int getRefillDays() {
        return refillDays;
    }

    public void setRefillDays(int refillDays) {
        this.refillDays = refillDays;
    }

    @NonNull
    public String getColor() {
        return color;
    }

    public void setColor(@NonNull String color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Medicine)) return false;
        Medicine medicine = (Medicine) o;
        return id == medicine.id
                && refillDays == medicine.refillDays
                && name.equals(medicine.name)
                && dose.equals(medicine.dose)
                && times.equals(medicine.times)
                && color.equals(medicine.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, dose, times, refillDays, color);
    }

    @NonNull
    @Override
    public String toString() {
        return "Medicine{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dose='" + dose + '\'' +
                ", times='" + times + '\'' +
                ", refillDays=" + refillDays +
                ", color='" + color + '\'' +
                '}';
    }
}
