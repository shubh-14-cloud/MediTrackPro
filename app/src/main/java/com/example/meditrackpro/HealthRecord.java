package com.example.meditrackpro;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "health_records")
public class HealthRecord {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String title;

    @NonNull
    private String type;

    @NonNull
    private String date;

    @NonNull
    private String status;

    @NonNull
    private String notes;

    @NonNull
    private String photoPath;

    public HealthRecord(int id, @NonNull String title, @NonNull String type, @NonNull String date,
                         @NonNull String status, @NonNull String notes, @NonNull String photoPath) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.date = date;
        this.status = status;
        this.notes = notes;
        this.photoPath = photoPath;
    }

    @Ignore
    public HealthRecord(@NonNull String title, @NonNull String type, @NonNull String date,
                         @NonNull String status, @NonNull String notes, @NonNull String photoPath) {
        this(0, title, type, date, status, notes, photoPath);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    @NonNull
    public String getType() {
        return type;
    }

    public void setType(@NonNull String type) {
        this.type = type;
    }

    @NonNull
    public String getDate() {
        return date;
    }

    public void setDate(@NonNull String date) {
        this.date = date;
    }

    @NonNull
    public String getStatus() {
        return status;
    }

    public void setStatus(@NonNull String status) {
        this.status = status;
    }

    @NonNull
    public String getNotes() {
        return notes;
    }

    public void setNotes(@NonNull String notes) {
        this.notes = notes;
    }

    @NonNull
    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(@NonNull String photoPath) {
        this.photoPath = photoPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HealthRecord)) return false;
        HealthRecord that = (HealthRecord) o;
        return id == that.id
                && title.equals(that.title)
                && type.equals(that.type)
                && date.equals(that.date)
                && status.equals(that.status)
                && notes.equals(that.notes)
                && photoPath.equals(that.photoPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, type, date, status, notes, photoPath);
    }

    @NonNull
    @Override
    public String toString() {
        return "HealthRecord{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", date='" + date + '\'' +
                ", status='" + status + '\'' +
                ", notes='" + notes + '\'' +
                ", photoPath='" + photoPath + '\'' +
                '}';
    }
}
