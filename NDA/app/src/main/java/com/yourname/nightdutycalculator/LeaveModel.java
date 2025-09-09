package com.yourname.nightdutycalculator;

public class LeaveModel {
    private int days;
    private String type;

    public LeaveModel(int days, String type) {
        this.days = days;
        this.type = type;
    }

    public int getDays() {
        return days;
    }

    public String getType() {
        return type;
    }
}
