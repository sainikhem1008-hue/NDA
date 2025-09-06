package com.yourname.nightdutycalculator;

public class LeaveRecord {
    private String leaveType;
    private String leaveFrom;
    private String leaveTo;
    private String status;
    private String notes;

    // --- Constructor ---
    public LeaveRecord(String leaveType, String leaveFrom, String leaveTo, String status, String notes) {
        this.leaveType = leaveType;
        this.leaveFrom = leaveFrom;
        this.leaveTo = leaveTo;
        this.status = status;
        this.notes = notes;
    }

    // --- Empty Constructor (needed for Gson/Room etc.) ---
    public LeaveRecord() {}

    // --- Getters & Setters ---
    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public String getLeaveFrom() {
        return leaveFrom;
    }

    public void setLeaveFrom(String leaveFrom) {
        this.leaveFrom = leaveFrom;
    }

    public String getLeaveTo() {
        return leaveTo;
    }

    public void setLeaveTo(String leaveTo) {
        this.leaveTo = leaveTo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
