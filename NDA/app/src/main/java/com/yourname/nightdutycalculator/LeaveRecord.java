package com.yourname.nightdutycalculator;

public class LeaveRecord {
    private String leaveFrom;
    private String leaveTo;
    private String leaveType;
    private String status; // Applied / Approved / Rejected

    public String getLeaveFrom() { return leaveFrom; }
    public void setLeaveFrom(String leaveFrom) { this.leaveFrom = leaveFrom; }
    public String getLeaveTo() { return leaveTo; }
    public void setLeaveTo(String leaveTo) { this.leaveTo = leaveTo; }
    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
