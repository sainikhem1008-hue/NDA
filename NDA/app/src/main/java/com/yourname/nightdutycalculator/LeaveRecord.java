package com.yourname.nightdutycalculator;

public class LeaveRecord {
    private String leaveFrom;   // Leave start date
    private String leaveTo;     // Leave end date
    private String leaveType;   // Type of leave (e.g., Casual, Sick, etc.)
    private String status;      // Applied / Approved / Rejected
    private String notes;       // Optional notes

    // Full constructor
    public LeaveRecord(String leaveFrom, String leaveTo, String leaveType, String status, String notes) {
        this.leaveFrom = leaveFrom;
        this.leaveTo = leaveTo;
        this.leaveType = leaveType;
        this.status = status;
        this.notes = notes;
    }

    // Constructor without notes
    public LeaveRecord(String leaveFrom, String leaveTo, String leaveType, String status) {
        this(leaveFrom, leaveTo, leaveType, status, "");
    }

    // Getters
    public String getLeaveFrom() {
        return leaveFrom;
    }

    public String getLeaveTo() {
        return leaveTo;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public String getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    // Setters
    public void setLeaveFrom(String leaveFrom) {
        this.leaveFrom = leaveFrom;
    }

    public void setLeaveTo(String leaveTo) {
        this.leaveTo = leaveTo;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
