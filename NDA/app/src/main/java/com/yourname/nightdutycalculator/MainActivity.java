package com.yourname.nightdutycalculator;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etDutyDate, etDutyFrom, etDutyTo, etBasicPay, etCeilingLimit, etDearnessAllowance;
    private CheckBox cbNationalHoliday, cbWeeklyRest, cbDualDuty, cbDutyOnWeeklyRest;
    private MaterialButton btnCalculate, btnSave, btnViewRecord, btnExport, btnClear, btnExit, btnLeaveManagement;

    private MaterialButton btnMorningDuty, btnEveningDuty, btnNightDuty;

    private TextView tvResults, tvCeilingWarning;
    private LinearLayout llResults;

    private List<DutyRecord> dutyRecords;
    private List<LeaveRecord> leaveRecords = new ArrayList<>();
    private DutyRecord currentCalculation;
    private Gson gson;
    private DecimalFormat decimalFormat;
    private static final double DEFAULT_CEILING = 18000.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gson = new Gson();
        decimalFormat = new DecimalFormat("#,##0.00");

        etDutyDate = findViewById(R.id.etDutyDate);
        etDutyFrom = findViewById(R.id.etDutyFrom);
        etDutyTo = findViewById(R.id.etDutyTo);
        etBasicPay = findViewById(R.id.etBasicPay);
        etCeilingLimit = findViewById(R.id.etCeilingLimit);
        etDearnessAllowance = findViewById(R.id.etDearnessAllowance);

        cbNationalHoliday = findViewById(R.id.cbNationalHoliday);
        cbWeeklyRest = findViewById(R.id.cbWeeklyRest);
        cbDualDuty = findViewById(R.id.cbDualDuty);
        cbDutyOnWeeklyRest = findViewById(R.id.cbDutyOnWeeklyRest);

        btnCalculate = findViewById(R.id.btnCalculate);
        btnSave = findViewById(R.id.btnSave);
        btnViewRecord = findViewById(R.id.btnView);
        btnExport = findViewById(R.id.btnExport);
        btnClear = findViewById(R.id.btnClear);
        btnExit = findViewById(R.id.btnExit);
        btnLeaveManagement = findViewById(R.id.btnLeaveManagement);

        btnMorningDuty = findViewById(R.id.btnMorningDuty);
        btnEveningDuty = findViewById(R.id.btnEveningDuty);
        btnNightDuty = findViewById(R.id.btnNightDuty);

        btnMorningDuty.setOnClickListener(v -> {
            etDutyFrom.setText("08:00");
            etDutyTo.setText("16:00");
        });

        btnEveningDuty.setOnClickListener(v -> {
            etDutyFrom.setText("16:00");
            etDutyTo.setText("00:00");
        });

        btnNightDuty.setOnClickListener(v -> {
            etDutyFrom.setText("00:00");
            etDutyTo.setText("08:00");
        });

        tvResults = findViewById(R.id.tvResults);
        tvCeilingWarning = findViewById(R.id.tvCeilingWarning);
        llResults = findViewById(R.id.llResults);

        etCeilingLimit.setText(String.valueOf(DEFAULT_CEILING));
        etDearnessAllowance.setText("0");
        setDefaultDateTimeValues();

        loadDutyRecords();

        etDutyDate.setOnClickListener(v -> showDatePicker());
        etDutyFrom.setOnClickListener(v -> showTimePicker(etDutyFrom));
        etDutyTo.setOnClickListener(v -> showTimePicker(etDutyTo));

        btnCalculate.setOnClickListener(v -> calculateAndDisplay());
        btnSave.setOnClickListener(v -> saveRecord());
        btnViewRecord.setOnClickListener(v -> showRecordsDialog());
        btnExport.setOnClickListener(v -> exportToPDF());
        btnClear.setOnClickListener(v -> clearAll());
        btnExit.setOnClickListener(v -> finish());
        btnLeaveManagement.setOnClickListener(v -> startActivity(new Intent(this, LeaveManagementActivity.class)));

        etBasicPay.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) { checkCeiling(); }
        });
    }

    private void setDefaultDateTimeValues() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        etDutyDate.setText(sdf.format(new Date()));
        etDutyFrom.setText("22:00");
        etDutyTo.setText("06:00");
    }

    private void loadDutyRecords() {
        SharedPreferences prefs = getSharedPreferences("DutyRecords", Context.MODE_PRIVATE);
        String json = prefs.getString("duty_records", "[]");
        Type t = new TypeToken<List<DutyRecord>>(){}.getType();
        dutyRecords = gson.fromJson(json, t);
        if (dutyRecords == null) dutyRecords = new ArrayList<>();
    }

    private void persistDutyRecords() {
        SharedPreferences prefs = getSharedPreferences("DutyRecords", Context.MODE_PRIVATE);
        prefs.edit().putString("duty_records", gson.toJson(dutyRecords)).apply();
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            c.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            etDutyDate.setText(sdf.format(c.getTime()));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker(final TextInputEditText target) {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (TimePicker view, int hourOfDay, int minute) -> {
            target.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    private void calculateAndDisplay() {
        try {
            String dutyDate = etDutyDate.getText().toString().trim();
            String dutyFrom = etDutyFrom.getText().toString().trim();
            String dutyTo = etDutyTo.getText().toString().trim();
            double basicPay = Double.parseDouble(etBasicPay.getText().toString().trim());
            double ceiling = Double.parseDouble(etCeilingLimit.getText().toString().trim());
            double daPercent = Double.parseDouble(etDearnessAllowance.getText().toString().trim());
            boolean isHoliday = cbNationalHoliday.isChecked();
            boolean isWeekly = cbWeeklyRest.isChecked();
            boolean isDualDuty = cbDualDuty.isChecked();
            boolean isDutyOnWeeklyRest = cbDutyOnWeeklyRest.isChecked();

            if (dutyDate.isEmpty() || dutyFrom.isEmpty() || dutyTo.isEmpty()) {
                Toast.makeText(this, "Please fill date/from/to", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check leave records for this date
            boolean isOnLeave = false;
            String leaveStatus = "";
            SharedPreferences leavePrefs = getSharedPreferences("LeaveRecords", Context.MODE_PRIVATE);
            String leaveJson = leavePrefs.getString("leave_records", "[]");
            Type lt = new TypeToken<List<LeaveRecord>>(){}.getType();
            List<LeaveRecord> leaveRecords = gson.fromJson(leaveJson, lt);
            if (leaveRecords != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Calendar dutyCal = Calendar.getInstance();
                dutyCal.setTime(sdf.parse(dutyDate));
                for (LeaveRecord lr : leaveRecords) {
                    if (lr.getStatus() == null) continue;
                    if (lr.getStatus().equalsIgnoreCase("Applied") || lr.getStatus().equalsIgnoreCase("Approved")) {
                        Calendar lf = Calendar.getInstance();
                        Calendar ltCal = Calendar.getInstance();
                        lf.setTime(sdf.parse(lr.getLeaveFrom()));
                        ltCal.setTime(sdf.parse(lr.getLeaveTo()));
                        if (!dutyCal.before(lf) && !dutyCal.after(ltCal)) {
                            isOnLeave = true;
                            leaveStatus = "On Leave (" + lr.getLeaveType() + ": " + lr.getLeaveFrom() + " to " + lr.getLeaveTo() + ")";
                            break;
                        }
                    }
                }
            }

            double effectiveBasic = Math.min(basicPay, ceiling);
            double salaryWithDA = effectiveBasic * (1 + daPercent / 100.0);

            String[] fromParts = dutyFrom.split(":");
            String[] toParts = dutyTo.split(":");
            Calendar fromCal = Calendar.getInstance();
            Calendar toCal = Calendar.getInstance();
            fromCal.set(Calendar.SECOND, 0); toCal.set(Calendar.SECOND, 0);
            fromCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(fromParts[0]));
            fromCal.set(Calendar.MINUTE, Integer.parseInt(fromParts[1]));
            toCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(toParts[0]));
            toCal.set(Calendar.MINUTE, Integer.parseInt(toParts[1]));
            if (!toCal.after(fromCal)) toCal.add(Calendar.DAY_OF_MONTH, 1);

            double totalDutyHours = (toCal.getTimeInMillis() - fromCal.getTimeInMillis()) / (1000.0 * 60 * 60);
            double night1 = calculateNightHours(fromCal, toCal, 22, 0, 0, 0);
            double night2 = calculateNightHours(fromCal, toCal, 0, 0, 6, 0);
            double totalNightHours = night1 + night2;

            boolean holidayAllowancePaid = false;
            double nightDutyAllowance = 0.0;
            String allowanceStatus = "No Allowance";

            if (isOnLeave) {
                totalDutyHours = 0.0;
                totalNightHours = 0.0;
                holidayAllowancePaid = false;
                nightDutyAllowance = 0.0;
                allowanceStatus = "No Allowance (On Leave)";
            } else if (isWeekly) {
                totalDutyHours = 0.0;
                totalNightHours = 0.0;
                holidayAllowancePaid = isHoliday;
                nightDutyAllowance = 0.0;
                allowanceStatus = isHoliday ? "Holiday Allowance (Weekly Rest)" : "No Allowance (Weekly Rest)";
                if (isDutyOnWeeklyRest) {
                    allowanceStatus += " | Duty on Weekly Rest (CR pending)";
                }
            } else {
                if (isDualDuty) {
                    allowanceStatus = "Dual Duty (CR pending)";
                    if (totalNightHours > 0.0) {
                        double nightHoursDivided = totalNightHours / 6.0;
                        nightDutyAllowance = nightHoursDivided * (salaryWithDA) / 200.0;
                    }
                } else {
                    if (isHoliday) holidayAllowancePaid = true;
                    if (totalNightHours > 0.0) {
                        double nightHoursDivided = totalNightHours / 6.0;
                        nightDutyAllowance = nightHoursDivided * (salaryWithDA) / 200.0;
                        allowanceStatus = "Calculated";
                    } else {
                        allowanceStatus = isHoliday ? "Holiday Allowance (No Night Hours)" : "No Night Hours";
                    }
                }
            }

            currentCalculation = new DutyRecord();
            currentCalculation.setDate(dutyDate);
            currentCalculation.setDutyFrom(dutyFrom);
            currentCalculation.setDutyTo(dutyTo);
            currentCalculation.setTotalDutyHours(totalDutyHours);
            currentCalculation.setNightHours1(night1);
            currentCalculation.setNightHours2(night2);
            currentCalculation.setTotalNightHours(totalNightHours);
            currentCalculation.setBasicPay(basicPay);
            currentCalculation.setEffectiveBasicPay(effectiveBasic);
            currentCalculation.setDearnessAllowance(daPercent);
            currentCalculation.setNightDutyAllowance(nightDutyAllowance);
            currentCalculation.setNationalHoliday(isHoliday);
            currentCalculation.setWeeklyRest(isWeekly);
            currentCalculation.setAllowanceStatus(allowanceStatus);
            currentCalculation.setLeaveStatus(leaveStatus);
            currentCalculation.setHolidayAllowancePaid(holidayAllowancePaid);
            currentCalculation.setDualDuty(isDualDuty);
            currentCalculation.setDutyOnWeeklyRest(isDutyOnWeeklyRest);

            displayCurrentCalculation();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: fill fields correctly", Toast.LENGTH_SHORT).show();
        }
    }

    private double calculateNightHours(Calendar fromCal, Calendar toCal, int startHour, int startMin, int endHour, int endMin) {
        Calendar nightStart = (Calendar) fromCal.clone();
        nightStart.set(Calendar.HOUR_OF_DAY, startHour);
        nightStart.set(Calendar.MINUTE, startMin);
        nightStart.set(Calendar.SECOND, 0);

        Calendar nightEnd = (Calendar) nightStart.clone();
        nightEnd.set(Calendar.HOUR_OF_DAY, endHour);
        nightEnd.set(Calendar.MINUTE, endMin);
        nightEnd.set(Calendar.SECOND, 0);
        if (!nightEnd.after(nightStart)) nightEnd.add(Calendar.DAY_OF_MONTH, 1);

        long overlapStart = Math.max(fromCal.getTimeInMillis(), nightStart.getTimeInMillis());
        long overlapEnd = Math.min(toCal.getTimeInMillis(), nightEnd.getTimeInMillis());
        if (overlapEnd > overlapStart) {
            return (overlapEnd - overlapStart) / (1000.0 * 60 * 60);
        }
        return 0.0;
    }

    private void displayCurrentCalculation() {
        if (currentCalculation == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("📅 Date: ").append(currentCalculation.getDate()).append("\n");
        sb.append("⏰ Duty: ").append(currentCalculation.getDutyFrom()).append(" - ").append(currentCalculation.getDutyTo()).append("\n");
        sb.append("⏱ Total Duty Hours (day): ").append(String.format(Locale.getDefault(), "%.2f", currentCalculation.getTotalDutyHours())).append(" hrs\n");
        sb.append("🌃 Night Hours: ").append(String.format(Locale.getDefault(), "%.2f", currentCalculation.getTotalNightHours())).append(" hrs\n");
        sb.append("💰 Night Duty Allowance: ₹").append(decimalFormat.format(currentCalculation.getNightDutyAllowance())).append("\n");
        sb.append("🎉 Holiday Allowance Paid: ").append(currentCalculation.isHolidayAllowancePaid() ? "Yes" : "No").append("\n");
        if (currentCalculation.getLeaveStatus() != null && !currentCalculation.getLeaveStatus().isEmpty())
            sb.append("📋 ").append(currentCalculation.getLeaveStatus()).append("\n");
        if (cbDualDuty.isChecked())
            sb.append("⚠ Dual Duty performed: CR pending\n");
        if (cbDutyOnWeeklyRest.isChecked())
            sb.append("⚠ Duty on Weekly Rest: CR pending\n");

        tvResults.setText(sb.toString());
        llResults.setVisibility(View.VISIBLE);
    }

    private void saveRecord() {
        if (currentCalculation == null) {
            Toast.makeText(this, "Please calculate first", Toast.LENGTH_SHORT).show();
            return;
        }
        dutyRecords.add(currentCalculation);
        persistDutyRecords();
        Toast.makeText(this, "Record saved", Toast.LENGTH_SHORT).show();
        currentCalculation = null;
        llResults.setVisibility(View.GONE);
        tvResults.setText("");
    }

    private void showRecordsDialog() {
        if (dutyRecords == null || dutyRecords.isEmpty()) {
            Toast.makeText(this, "No records available", Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuilder sb = new StringBuilder();
        int idx = 1;
        for (DutyRecord r : dutyRecords) {
            sb.append(idx++).append(". ").append(r.getDate())
              .append(" | ").append(r.getDutyFrom()).append("-").append(r.getDutyTo())
              .append(" | Night: ").append(String.format(Locale.getDefault(), "%.2f", r.getTotalNightHours())).append("h")
              .append(" | NDA: ₹").append(decimalFormat.format(r.getNightDutyAllowance()));

            if (r.isHolidayAllowancePaid()) sb.append(" | Holiday Paid");
            if (r.isDualDuty()) sb.append(" | Dual Duty CR Pending");
            if (r.isDutyOnWeeklyRest()) sb.append(" | Duty on Weekly Rest CR Pending");

            sb.append("\n\n");
        }
        new AlertDialog.Builder(this)
                .setTitle("Saved Records")
                .setMessage(sb.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void checkCeiling() {
        try {
            double basic = Double.parseDouble(etBasicPay.getText().toString());
            double ceiling = Double.parseDouble(etCeilingLimit.getText().toString());
            if (basic > ceiling) {
                tvCeilingWarning.setVisibility(View.VISIBLE);
                tvCeilingWarning.setText("⚠ Using ceiling ₹" + decimalFormat.format(ceiling));
            } else {
                tvCeilingWarning.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            tvCeilingWarning.setVisibility(View.GONE);
        }
    }

    private void exportToPDF() {
    if ((dutyRecords == null || dutyRecords.isEmpty()) &&
        (leaveRecords == null || leaveRecords.isEmpty())) {
        Toast.makeText(this, "No records to export", Toast.LENGTH_SHORT).show();
        return;
    }

    PdfDocument pdfDoc = new PdfDocument();
    PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
    PdfDocument.Page page = pdfDoc.startPage(pageInfo);
    android.graphics.Canvas canvas = page.getCanvas();
    Paint paint = new Paint();
    paint.setTextSize(12);

    int x = 40;
    int y = 40;

    canvas.drawText("NIGHT DUTY ALLOWANCE REPORT", x, y, paint);
    y += 25;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    canvas.drawText("Generated: " + sdf.format(new Date()), x, y, paint);
    y += 30;

    // ================= DUTY RECORDS ==================
    if (dutyRecords != null && !dutyRecords.isEmpty()) {
        canvas.drawText("----- Duty Records -----", x, y, paint);
        y += 20;

        canvas.drawText("Date", x, y, paint);
        canvas.drawText("Duty (day)", x + 90, y, paint);
        canvas.drawText("Night hrs", x + 240, y, paint);
        canvas.drawText("Night NDA", x + 330, y, paint);
        canvas.drawText("Holiday Paid", x + 440, y, paint);
        y += 18;

        double totalNightH = 0.0;
        double totalNDA = 0.0;

        for (DutyRecord r : dutyRecords) {
            if (y > 780) {
                pdfDoc.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 2).create();
                page = pdfDoc.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 40;
            }

            canvas.drawText(r.getDate(), x, y, paint);
            String dutyTime = r.getDutyFrom() + " - " + r.getDutyTo();
            canvas.drawText(dutyTime, x + 90, y, paint);
            canvas.drawText(String.format(Locale.getDefault(), "%.2f", r.getTotalNightHours()), x + 240, y, paint);
            canvas.drawText("₹" + decimalFormat.format(r.getNightDutyAllowance()), x + 330, y, paint);
            canvas.drawText(r.isHolidayAllowancePaid() ? "Yes" : "No", x + 440, y, paint);
            y += 18;

            totalNightH += r.getTotalNightHours();
            totalNDA += r.getNightDutyAllowance();
        }

        y += 16;
        canvas.drawText("Duty Summary:", x, y, paint);
        y += 18;
        canvas.drawText("Total Night Hours: " + String.format(Locale.getDefault(), "%.2f", totalNightH), x, y, paint);
        y += 18;
        canvas.drawText("Total NDA: ₹" + decimalFormat.format(totalNDA), x, y, paint);
        y += 25;
    }

    // ================= LEAVE RECORDS ==================
    if (leaveRecords != null && !leaveRecords.isEmpty()) {
        canvas.drawText("----- Leave Records -----", x, y, paint);
        y += 20;

        canvas.drawText("From - To", x, y, paint);
        canvas.drawText("Type", x + 150, y, paint);
        canvas.drawText("Status", x + 270, y, paint);
        canvas.drawText("Notes", x + 390, y, paint);
        y += 18;

        for (LeaveRecord lr : leaveRecords) {
            if (y > 780) {
                pdfDoc.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 3).create();
                page = pdfDoc.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 40;
            }

            String leavePeriod = lr.getLeaveFrom() + " - " + lr.getLeaveTo();
            canvas.drawText(leavePeriod, x, y, paint);
            canvas.drawText(lr.getLeaveType(), x + 150, y, paint);
            canvas.drawText(lr.getStatus(), x + 270, y, paint);

            if (lr.getNotes() != null && !lr.getNotes().trim().isEmpty()) {
                canvas.drawText(lr.getNotes(), x + 390, y, paint);
            }
            y += 18;
        }

        y += 16;
        canvas.drawText("Total Leaves: " + leaveRecords.size(), x, y, paint);
    }

    pdfDoc.finishPage(page);

    try {
        File file = new File(getExternalFilesDir(null), "night_duty_report.pdf");
        FileOutputStream fos = new FileOutputStream(file);
        pdfDoc.writeTo(fos);
        pdfDoc.close();
        fos.close();

        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("application/pdf");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(share, "Share PDF"));
    } catch (IOException e) {
        e.printStackTrace();
        Toast.makeText(this, "PDF export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }
    } 
    
    private void clearAll() {
        etDutyDate.setText("");
        etDutyFrom.setText("");
        etDutyTo.setText("");
        etBasicPay.setText("");
        etDearnessAllowance.setText("0");
        etCeilingLimit.setText(String.valueOf(DEFAULT_CEILING));
        cbNationalHoliday.setChecked(false);
        cbWeeklyRest.setChecked(false);
        cbDualDuty.setChecked(false);
        cbDutyOnWeeklyRest.setChecked(false);
        tvResults.setText("");
        llResults.setVisibility(View.GONE);
        currentCalculation = null;
    }
}
