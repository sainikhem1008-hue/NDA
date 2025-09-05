package com.yourname.nightdutycalculator;

public class LeaveRecord {
    private long id;
    private String leaveFrom;
    private String leaveTo;
    private String leaveType;
    private String appliedDate;
    private String status; // Applied, Approved, Rejected, Completed
    private String notes;

    public LeaveRecord() { 
        this.id = System.currentTimeMillis(); 
        this.status = "Applied";
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getLeaveFrom() { return leaveFrom; }
    public void setLeaveFrom(String leaveFrom) { this.leaveFrom = leaveFrom; }

    public String getLeaveTo() { return leaveTo; }
    public void setLeaveTo(String leaveTo) { this.leaveTo = leaveTo; }

    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }

    public String getAppliedDate() { return appliedDate; }
    public void setAppliedDate(String appliedDate) { this.appliedDate = appliedDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
