package com.example.saftyapp.data.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sms_logs")
public class SmsLog {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int sessionId;
    private String contactPhone;
    private String message;
    private long timestamp;
    private String status;

    public SmsLog(int sessionId, String contactPhone, String message, long timestamp, String status) {
        this.sessionId = sessionId;
        this.contactPhone = contactPhone;
        this.message = message;
        this.timestamp = timestamp;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
