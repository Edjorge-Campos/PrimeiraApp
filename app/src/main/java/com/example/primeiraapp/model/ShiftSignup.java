package com.example.primeiraapp.model;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.PropertyName;

public class ShiftSignup {
    @PropertyName("userId")
    private String userId;
    @PropertyName("shiftId")
    private String shiftId;
    @PropertyName("createdAt")
    private Object createdAt;

    public ShiftSignup() {
    }

    public ShiftSignup(String userId, String shiftId, Object createdAt) {
        this.userId = userId;
        this.shiftId = shiftId;
        this.createdAt = createdAt;
    }

    @PropertyName("userId")
    public String getUserId() {
        return userId;
    }

    @PropertyName("userId")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @PropertyName("shiftId")
    public String getShiftId() {
        return shiftId;
    }

    @PropertyName("shiftId")
    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    @PropertyName("createdAt")
    public Object getCreatedAt() {
        return createdAt;
    }

    @PropertyName("createdAt")
    public void setCreatedAt(Object createdAt) {
        this.createdAt = createdAt;
    }
}
