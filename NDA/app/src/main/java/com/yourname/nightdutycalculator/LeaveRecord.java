package com.yourname.nightdutycalculator;

public class LeaveRecord {
    private String date;   // Leave date
    private String type;   // Leave type (Casual, Sick, etc.)
    private String notes;  // Optional notes

    // Constructor with notes
    public LeaveRecord(String date, String type, String notes) {
        this.date = date;
        this.type = type;
        this.notes = notes;
    }

    // Constructor without notes (for flexibility)
    public LeaveRecord(String date, String type) {
        this(date, type, "");
    }

    // Getters
    public String getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public String getNotes() {
        return notes;
    }

    // Setters
    public void setDate(String date) {
        this.date = date;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
