package com.yourname.nightdutycalculator;

public class DutyRecord {
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
    private boolean nationalHoliday;
    private boolean weeklyRest;
    private boolean holidayAllowancePaid;
    private String allowanceStatus;
    private String leaveStatus;

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

    public boolean isNationalHoliday() { return nationalHoliday; }
    public void setNationalHoliday(boolean nationalHoliday) { this.nationalHoliday = nationalHoliday; }

    public boolean isWeeklyRest() { return weeklyRest; }
    public void setWeeklyRest(boolean weeklyRest) { this.weeklyRest = weeklyRest; }

    public boolean isHolidayAllowancePaid() { return holidayAllowancePaid; }
    public void setHolidayAllowancePaid(boolean holidayAllowancePaid) { this.holidayAllowancePaid = holidayAllowancePaid; }

    public String getAllowanceStatus() { return allowanceStatus; }
    public void setAllowanceStatus(String allowanceStatus) { this.allowanceStatus = allowanceStatus; }

    public String getLeaveStatus() { return leaveStatus; }
    public void setLeaveStatus(String leaveStatus) { this.leaveStatus = leaveStatus; }
}
