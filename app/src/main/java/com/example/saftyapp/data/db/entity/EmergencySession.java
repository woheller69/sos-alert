package com.example.saftyapp.data.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "emergency_sessions")
public class EmergencySession {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private long startTime;
    private long endTime;
    private String triggerType;
    private long duration;
    private String audioPath;
    private String frontPhotoPath;
    private String rearPhotoPath;
    private boolean resolved;

    public EmergencySession(long startTime, String triggerType) {
        this.startTime = startTime;
        this.triggerType = triggerType;
        this.resolved = false;
        this.audioPath = "";
        this.frontPhotoPath = "";
        this.rearPhotoPath = "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public String getFrontPhotoPath() {
        return frontPhotoPath;
    }

    public void setFrontPhotoPath(String frontPhotoPath) {
        this.frontPhotoPath = frontPhotoPath;
    }

    public String getRearPhotoPath() {
        return rearPhotoPath;
    }

    public void setRearPhotoPath(String rearPhotoPath) {
        this.rearPhotoPath = rearPhotoPath;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }
}
