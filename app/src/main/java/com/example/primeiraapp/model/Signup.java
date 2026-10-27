package com.example.primeiraapp.model;

import com.google.firebase.Timestamp;

public class Signup {
    private String userId;
    private String displayName;
    private String shiftId;
    private String date;
    private Timestamp createdAt;
    private String source;

    public Signup() {
    }

    public Signup(String userId, String displayName, String shiftId, String date, Timestamp createdAt, String source) {
        this.userId = userId;
        this.displayName = displayName;
        this.shiftId = shiftId;
        this.date = date;
        this.createdAt = createdAt;
        this.source = source;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
