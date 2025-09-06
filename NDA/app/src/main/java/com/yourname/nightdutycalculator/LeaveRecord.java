package com.khem.nightdutycalculator;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "leave_records")
public class LeaveRecord {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String date;
    private String leaveType;
    private String remarks;

    // Constructor
    public LeaveRecord(String date, String leaveType, String remarks) {
        this.date = date;
        this.leaveType = leaveType;
        this.remarks = remarks;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
