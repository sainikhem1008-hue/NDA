package com.yourname.nightdutycalculator;

public class DutyRecord {
    private long id;
    private String date;
    private String dutyFrom;
    private String dutyTo;
    private double totalDutyHours;
    private double nightHours1;
    private double nightHours2;
    private double totalNightHours;
    private double basicPay;
    private double effectiveBasicPay;
    private double dearnessAllowance;
    private double nightDutyAllowance;
    private boolean isNationalHoliday;
    private boolean isWeeklyRest;
    private String allowanceStatus;
    private String leaveStatus;


    private boolean isDualDuty;
    private String secondDutyFrom;
    private String secondDutyTo;
    private double secondDutyHours;
    private double secondDutyNightHours;
    private double secondDutyNightAllowance;
    private boolean isDutyOnWeeklyRest;
    private boolean crPending;

    // ✅ NEW FIELD
    private boolean holidayAllowancePaid;

    // ✅ Default constructor
    public DutyRecord() {
        this.id = System.currentTimeMillis();
    }

    // ✅ NEW constructor (this fixes your MainActivity.java error)
    public DutyRecord(String date, String dutyFrom, String dutyTo,
                      double totalNightHours, double effectiveBasicPay, double nightDutyAllowance) {
        this.id = System.currentTimeMillis();
        this.date = date;
        this.dutyFrom = dutyFrom;
        this.dutyTo = dutyTo;
        this.totalNightHours = totalNightHours;
        this.effectiveBasicPay = effectiveBasicPay;
        this.nightDutyAllowance = nightDutyAllowance;
    }

    // --- Getters & Setters ---
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getDutyFrom() { return dutyFrom; }
    public void setDutyFrom(String dutyFrom) { this.dutyFrom = dutyFrom; }

    public String getDutyTo() { return dutyTo; }
    public void setDutyTo(String dutyTo) { this.dutyTo = dutyTo; }

    public double getTotalDutyHours() { return totalDutyHours; }
    public void setTotalDutyHours(double totalDutyHours) { this.totalDutyHours = totalDutyHours; }

    public double getNightHours1() { return nightHours1; }
    public void setNightHours1(double nightHours1) { this.nightHours1 = nightHours1; }

    public double getNightHours2() { return nightHours2; }
    public void setNightHours2(double nightHours2) { this.nightHours2 = nightHours2; }

    public double getTotalNightHours() { return totalNightHours; }
    public void setTotalNightHours(double totalNightHours) { this.totalNightHours = totalNightHours; }

    public double getBasicPay() { return basicPay; }
    public void setBasicPay(double basicPay) { this.basicPay = basicPay; }

    public double getEffectiveBasicPay() { return effectiveBasicPay; }
    public void setEffectiveBasicPay(double effectiveBasicPay) { this.effectiveBasicPay = effectiveBasicPay; }

    public double getDearnessAllowance() { return dearnessAllowance; }
    public void setDearnessAllowance(double dearnessAllowance) { this.dearnessAllowance = dearnessAllowance; }

    public double getNightDutyAllowance() { return nightDutyAllowance; }
    public void setNightDutyAllowance(double nightDutyAllowance) { this.nightDutyAllowance = nightDutyAllowance; }

    public boolean isNationalHoliday() { return isNationalHoliday; }
    public void setNationalHoliday(boolean nationalHoliday) { isNationalHoliday = nationalHoliday; }

    public boolean isWeeklyRest() { return isWeeklyRest; }
    public void setWeeklyRest(boolean weeklyRest) { isWeeklyRest = weeklyRest; }

    public String getAllowanceStatus() { return allowanceStatus; }
    public void setAllowanceStatus(String allowanceStatus) { this.allowanceStatus = allowanceStatus; }

    public String getLeaveStatus() { return leaveStatus; }
    public void setLeaveStatus(String leaveStatus) { this.leaveStatus = leaveStatus; }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public boolean isDualDuty() { return isDualDuty; }
    
    
    
    public void setDualDuty(boolean dualDuty) { isDualDuty = dualDuty; }

    public String getSecondDutyFrom() { return secondDutyFrom; }
    public void setSecondDutyFrom(String secondDutyFrom) { this.secondDutyFrom = secondDutyFrom; }

    public String getSecondDutyTo() { return secondDutyTo; }
    public void setSecondDutyTo(String secondDutyTo) { this.secondDutyTo = secondDutyTo; }

    public double getSecondDutyHours() { return secondDutyHours; }
    public void setSecondDutyHours(double secondDutyHours) { this.secondDutyHours = secondDutyHours; }

    public double getSecondDutyNightHours() { return secondDutyNightHours; }
    public void setSecondDutyNightHours(double secondDutyNightHours) { this.secondDutyNightHours = secondDutyNightHours; }

    public double getSecondDutyNightAllowance() { return secondDutyNightAllowance; }
    public void setSecondDutyNightAllowance(double secondDutyNightAllowance) { this.secondDutyNightAllowance = secondDutyNightAllowance; }

    public boolean isDutyOnWeeklyRest() { return isDutyOnWeeklyRest; }
    public void setDutyOnWeeklyRest(boolean dutyOnWeeklyRest) { isDutyOnWeeklyRest = dutyOnWeeklyRest; }

    public boolean isCrPending() { return crPending; }
    public void setCrPending(boolean crPending) { this.crPending = crPending; }
    

    // ✅ NEW METHODS
    public boolean isHolidayAllowancePaid() {
        return holidayAllowancePaid;
    }

    public void setHolidayAllowancePaid(boolean holidayAllowancePaid) {
        this.holidayAllowancePaid = holidayAllowancePaid;
    }
}
