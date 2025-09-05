package com.yourname.nightdutycalculator;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
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
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * MainActivity split in 3 parts. Paste Part1 then Part2 then Part3 in same file.
 * This MainActivity:
 *  - supports quick duty buttons (Morning/Evening/Night)
 *  - handles national holiday, weekly rest, weekly-rest-duty, dual duty
 *  - calculates night hours (22:00-06:00) and NDA amount
 *  - checks leave records to suppress allowances/hours appropriately
 *  - persists duty records to SharedPreferences and to SQLite via DatabaseHelper
 *  - exports a PDF containing duty and leave records (summary counts night hours only)
 */

public class MainActivity extends AppCompatActivity {

    // UI
    private TextInputEditText etDutyDate, etDutyFrom, etDutyTo, etBasicPay, etCeilingLimit, etDearnessAllowance;
    private CheckBox cbNationalHoliday, cbWeeklyRest, cbWeeklyRestDuty, cbDualDuty;
    private MaterialButton btnMorningDuty, btnEveningDuty, btnNightDuty;
    private MaterialButton btnCalculate, btnSave, btnViewRecord, btnExport, btnClear, btnExit, btnLeaveManagement;
    private androidx.appcompat.widget.AppCompatTextView tvResults;
    private androidx.appcompat.widget.AppCompatTextView tvCeilingWarning;
    private LinearLayout llResults;

    // Data
    private List<DutyRecord> dutyRecords;
    private DutyRecord currentCalculation;
    private Gson gson;
    private DecimalFormat decimalFormat;
    private static final double DEFAULT_CEILING = 18000.0;

    // DB helper
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // ensure your layout matches ids used here

        // utilities
        gson = new Gson();
        decimalFormat = new DecimalFormat("#,##0.00");
        dbHelper = new DatabaseHelper(this);

        // bind views (IDs must match your activity_main.xml)
        etDutyDate = findViewById(R.id.etDutyDate);
        etDutyFrom = findViewById(R.id.etDutyFrom);
        etDutyTo = findViewById(R.id.etDutyTo);
        etBasicPay = findViewById(R.id.etBasicPay);
        etCeilingLimit = findViewById(R.id.etCeilingLimit);
        etDearnessAllowance = findViewById(R.id.etDearnessAllowance);

        cbNationalHoliday = findViewById(R.id.cbNationalHoliday);
        cbWeeklyRest = findViewById(R.id.cbWeeklyRest);           // day is weekly rest (no duty)
        cbWeeklyRestDuty = findViewById(R.id.cbWeeklyRestDuty);   // duty performed on weekly rest -> CR pending
        cbDualDuty = findViewById(R.id.cbDualDuty);

        btnMorningDuty = findViewById(R.id.btnMorningDuty);
        btnEveningDuty = findViewById(R.id.btnEveningDuty);
        btnNightDuty = findViewById(R.id.btnNightDuty);

        btnCalculate = findViewById(R.id.btnCalculate);
        btnSave = findViewById(R.id.btnSave);
        btnViewRecord = findViewById(R.id.btnView);
        btnExport = findViewById(R.id.btnExport);
        btnClear = findViewById(R.id.btnClear);
        btnExit = findViewById(R.id.btnExit);
        btnLeaveManagement = findViewById(R.id.btnLeaveManagement);

        tvResults = findViewById(R.id.tvResults);
        tvCeilingWarning = findViewById(R.id.tvCeilingWarning);
        llResults = findViewById(R.id.llResults);

        // defaults
        if (etCeilingLimit != null) etCeilingLimit.setText(String.valueOf(DEFAULT_CEILING));
        if (etDearnessAllowance != null) etDearnessAllowance.setText("0");
        setDefaultDateTimeValues();

        // load existing records (SharedPreferences)
        loadDutyRecords();

        // pickers and quick buttons
        if (etDutyDate != null) etDutyDate.setOnClickListener(v -> showDatePicker(etDutyDate));
        if (etDutyFrom != null) etDutyFrom.setOnClickListener(v -> showTimePicker(etDutyFrom));
        if (etDutyTo != null) etDutyTo.setOnClickListener(v -> showTimePicker(etDutyTo));

        if (btnMorningDuty != null) btnMorningDuty.setOnClickListener(v -> {
            etDutyFrom.setText("08:00");
            etDutyTo.setText("16:00");
        });
        if (btnEveningDuty != null) btnEveningDuty.setOnClickListener(v -> {
            etDutyFrom.setText("16:00");
            etDutyTo.setText("00:00");
        });
        if (btnNightDuty != null) btnNightDuty.setOnClickListener(v -> {
            etDutyFrom.setText("00:00");
            etDutyTo.setText("08:00");
        });

        // main actions
        if (btnCalculate != null) btnCalculate.setOnClickListener(v -> calculateAndDisplay());
        if (btnSave != null) btnSave.setOnClickListener(v -> saveRecord());
        if (btnViewRecord != null) btnViewRecord.setOnClickListener(v -> showRecordsDialog());
        if (btnExport != null) btnExport.setOnClickListener(v -> exportToPDF());
        if (btnClear != null) btnClear.setOnClickListener(v -> clearAll());
        if (btnExit != null) btnExit.setOnClickListener(v -> finish());
        if (btnLeaveManagement != null) btnLeaveManagement.setOnClickListener(v -> startActivity(new Intent(this, LeaveManagementActivity.class)));

        // ceiling check hook
        if (etBasicPay != null) etBasicPay.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) { checkCeilingLimit(); }
        });
    } // end onCreate (Part 1)
    // ------------------------- Part 2 -------------------------
    private void setDefaultDateTimeValues() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        if (etDutyDate != null) etDutyDate.setText(sdf.format(new Date()));
        if (etDutyFrom != null && TextUtils.isEmpty(etDutyFrom.getText())) etDutyFrom.setText("22:00");
        if (etDutyTo != null && TextUtils.isEmpty(etDutyTo.getText())) etDutyTo.setText("06:00");
    }

    private void showDatePicker(final TextInputEditText target) {
        if (target == null) return;
        final Calendar c = Calendar.getInstance();
        DatePickerDialog dp = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar chosen = Calendar.getInstance();
                    chosen.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    target.setText(sdf.format(chosen.getTime()));
                },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dp.show();
    }

    private void showTimePicker(final TextInputEditText target) {
        if (target == null) return;
        final Calendar c = Calendar.getInstance();
        TimePickerDialog tp = new TimePickerDialog(this,
                (TimePicker view, int hourOfDay, int minute) -> target.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)),
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
        tp.show();
    }

    // Load / Persist dutyRecords (SharedPreferences)
    private void loadDutyRecords() {
        try {
            SharedPreferences prefs = getSharedPreferences("DutyRecords", Context.MODE_PRIVATE);
            String json = prefs.getString("duty_records", "[]");
            Type t = new TypeToken<List<DutyRecord>>(){}.getType();
            dutyRecords = gson.fromJson(json, t);
            if (dutyRecords == null) dutyRecords = new ArrayList<>();
        } catch (Exception e) {
            dutyRecords = new ArrayList<>();
        }
    }

    private void persistDutyRecords() {
        try {
            SharedPreferences prefs = getSharedPreferences("DutyRecords", Context.MODE_PRIVATE);
            prefs.edit().putString("duty_records", gson.toJson(dutyRecords)).apply();
        } catch (Exception ignored) {}
    }

    private boolean isDateWithinLeave(String dutyDate) {
        // checks leave records in SharedPreferences to find if dutyDate falls inside any Applied/Approved leave
        try {
            SharedPreferences leavePrefs = getSharedPreferences("LeaveRecords", Context.MODE_PRIVATE);
            String json = leavePrefs.getString("leave_records", "[]");
            Type t = new TypeToken<List<LeaveRecord>>(){}.getType();
            List<LeaveRecord> leaves = gson.fromJson(json, t);
            if (leaves == null || leaves.isEmpty()) return false;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date d = sdf.parse(dutyDate);
            Calendar dutyCal = Calendar.getInstance();
            dutyCal.setTime(d);

            for (LeaveRecord lr : leaves) {
                if (lr == null) continue;
                if (lr.getStatus() == null) continue;
                if (!("Applied".equalsIgnoreCase(lr.getStatus()) || "Approved".equalsIgnoreCase(lr.getStatus()))) continue;
                Date from = sdf.parse(lr.getLeaveFrom());
                Date to = sdf.parse(lr.getLeaveTo());
                if (from == null || to == null) continue;
                Calendar fromCal = Calendar.getInstance(); fromCal.setTime(from);
                Calendar toCal = Calendar.getInstance(); toCal.setTime(to);
                if (!dutyCal.before(fromCal) && !dutyCal.after(toCal)) return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    // Overlap calculation helper (returns hours overlap between [fromCal,toCal) and interval [sHour:sMin, eHour:eMin])
    private double overlapHours(Calendar fromCal, Calendar toCal, int sHour, int sMin, int eHour, int eMin) {
        Calendar intervalStart = (Calendar) fromCal.clone();
        intervalStart.set(Calendar.HOUR_OF_DAY, sHour); intervalStart.set(Calendar.MINUTE, sMin); intervalStart.set(Calendar.SECOND, 0);
        Calendar intervalEnd = (Calendar) intervalStart.clone();
        intervalEnd.set(Calendar.HOUR_OF_DAY, eHour); intervalEnd.set(Calendar.MINUTE, eMin); intervalEnd.set(Calendar.SECOND, 0);
        if (!intervalEnd.after(intervalStart)) intervalEnd.add(Calendar.DAY_OF_MONTH,1);

        long overlapStart = Math.max(fromCal.getTimeInMillis(), intervalStart.getTimeInMillis());
        long overlapEnd = Math.min(toCal.getTimeInMillis(), intervalEnd.getTimeInMillis());
        if (overlapEnd > overlapStart) {
            return (overlapEnd - overlapStart) / (1000.0 * 60.0 * 60.0);
        }
        return 0.0;
    }

    // calc night hours between two calendars (handles cross-midnight)
    private double calculateNightHours(Calendar fromCal, Calendar toCal) {
        // night period is 22:00-24:00 and 00:00-06:00
        double h1 = overlapHours(fromCal, toCal, 22,0, 24,0); // 22:00 - 24:00
        double h2 = overlapHours(fromCal, toCal, 0,0, 6,0);   // 00:00 - 06:00
        return h1 + h2;
    }

    private double calculateTotalHours(Calendar fromCal, Calendar toCal) {
        return (toCal.getTimeInMillis() - fromCal.getTimeInMillis()) / (1000.0 * 60.0 * 60.0);
    }
    // ------------------------- Part 3 -------------------------
    private void calculateAndDisplay() {
        try {
            String dutyDate = etDutyDate.getText().toString();
            String dutyFrom = etDutyFrom.getText().toString();
            String dutyTo = etDutyTo.getText().toString();
            double basicPay = Double.parseDouble(etBasicPay.getText().toString().isEmpty() ? "0" : etBasicPay.getText().toString());
            double ceiling = Double.parseDouble(etCeilingLimit.getText().toString().isEmpty() ? String.valueOf(DEFAULT_CEILING) : etCeilingLimit.getText().toString());
            double daPercent = Double.parseDouble(etDearnessAllowance.getText().toString().isEmpty() ? "0" : etDearnessAllowance.getText().toString());

            boolean isNationalHoliday = cbNationalHoliday.isChecked();
            boolean isWeeklyRestChecked = cbWeeklyRest.isChecked();
            boolean isWeeklyRestDuty = cbWeeklyRestDuty.isChecked();
            boolean isDualDuty = cbDualDuty.isChecked();

            if (dutyDate.isEmpty() || dutyFrom.isEmpty() || dutyTo.isEmpty()) {
                Toast.makeText(this, "Please fill duty date and times", Toast.LENGTH_SHORT).show();
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date baseDate = sdfDate.parse(dutyDate);
            Calendar fromCal = Calendar.getInstance();
            Calendar toCal = Calendar.getInstance();

            // parse from/to times and attach to the correct date (to may be next day)
            String[] fromParts = dutyFrom.split(":");
            String[] toParts = dutyTo.split(":");
            fromCal.setTime(baseDate);
            fromCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(fromParts[0]));
            fromCal.set(Calendar.MINUTE, Integer.parseInt(fromParts[1]));
            fromCal.set(Calendar.SECOND, 0);
            toCal.setTime(baseDate);
            toCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(toParts[0]));
            toCal.set(Calendar.MINUTE, Integer.parseInt(toParts[1]));
            toCal.set(Calendar.SECOND, 0);
            if (!toCal.after(fromCal)) toCal.add(Calendar.DAY_OF_MONTH, 1);

            // check leave
            boolean isOnLeave = isDateWithinLeave(dutyDate);
            String leaveStatus = isOnLeave ? "On Leave" : "";

            // effective basic pay (apply ceiling)
            double effectiveBasic = Math.min(basicPay, ceiling);
            double salaryWithDA = effectiveBasic * (1 + daPercent/100.0);

            // compute hours
            double dutyHours = calculateTotalHours(fromCal, toCal); // full duty hours
            double nightHours = calculateNightHours(fromCal, toCal); // 22-06
            double nightHoursDivided = nightHours / 6.0;
            double nightDutyAllowance = 0.0;
            boolean holidayAllowancePaid = false;

            // Apply rules:
            // 3 & 4: on leave -> no night duty allowance; holiday during leave => no holiday allowance
            if (isOnLeave) {
                nightDutyAllowance = 0.0;
                holidayAllowancePaid = false;
                dutyHours = 0.0;
                nightHours = 0.0;
            } else if (isWeeklyRestChecked) {
                // rule 6 & 7: weekly rest => no duty hours counted and not in total; but if weekly rest falls on holiday -> holiday allowance
                dutyHours = 0.0;
                nightHours = 0.0;
                if (isNationalHoliday) {
                    holidayAllowancePaid = true; // rule 5
                }
            } else {
                // normal or holiday duty
                if (isNationalHoliday) {
                    // Rule 1/2: if duty on holiday -> holiday allowance paid
                    holidayAllowancePaid = true;
                }
                // night allowance only if not on leave and nightHours > 0
                if (nightHours > 0) {
                    nightDutyAllowance = (nightHoursDivided * salaryWithDA) / 200.0;
                }
            }

            // if both weekly rest + holiday checked user told: no duty hours counted; holiday allowance may still be paid (handled above)
            if (isWeeklyRestChecked && isNationalHoliday) {
                // already set dutyHours=0; holidayAllowancePaid true above
            }

            // Dual duty text: indicate CR pending (we don't create second duty line here)
            String extraNotes = "";
            if (isDualDuty) extraNotes += "Dual Duty: CR Pending. ";
            if (isWeeklyRestDuty) extraNotes += "Duty on Weekly Rest: CR Pending. ";

            // prepare record
            currentCalculation = new DutyRecord();
            currentCalculation.setDate(dutyDate);
            currentCalculation.setDutyFrom(dutyFrom);
            currentCalculation.setDutyTo(dutyTo);
            currentCalculation.setTotalDutyHours(dutyHours);
            // split night hours into two segments for clarity (22-00 and 00-06)
            double nightPart1 = overlapHours(fromCal, toCal, 22,0, 24,0);
            double nightPart2 = overlapHours(fromCal, toCal, 0,0, 6,0);
            currentCalculation.setNightHours1(nightPart1);
            currentCalculation.setNightHours2(nightPart2);
            currentCalculation.setTotalNightHours(nightPart1 + nightPart2);
            currentCalculation.setBasicPay(basicPay);
            currentCalculation.setEffectiveBasicPay(effectiveBasic);
            currentCalculation.setDearnessAllowance(daPercent);
            currentCalculation.setNightDutyAllowance(nightDutyAllowance);
            currentCalculation.setNationalHoliday(isNationalHoliday);
            currentCalculation.setWeeklyRest(isWeeklyRestChecked);
            currentCalculation.setHolidayAllowancePaid(holidayAllowancePaid);
            currentCalculation.setAllowanceStatus((nightDutyAllowance > 0 ? "NDA Calculated" : "No NDA") + (holidayAllowancePaid ? " + Holiday Allowance" : ""));
            currentCalculation.setLeaveStatus(leaveStatus);
            currentCalculation.setDualDuty(isDualDuty);
            currentCalculation.setWeeklyRestDuty(isWeeklyRestDuty);
            currentCalculation.setNotes(extraNotes);

            displayResults();

        } catch (Exception e) {
            Toast.makeText(this, "Calculation error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void displayResults() {
        if (currentCalculation == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("📅 Date: ").append(currentCalculation.getDate()).append("\n");
        sb.append("⏰ Duty: ").append(currentCalculation.getDutyFrom()).append(" - ").append(currentCalculation.getDutyTo()).append("\n");
        sb.append("⏱ Total Duty Hours (day): ").append(String.format(Locale.getDefault(), "%.2f", currentCalculation.getTotalDutyHours())).append(" hrs\n");
        sb.append("🌃 Night Hours (22:00-06:00): ").append(String.format(Locale.getDefault(), "%.2f", currentCalculation.getTotalNightHours())).append(" hrs\n");
        sb.append("💰 Night Duty Allowance: ₹").append(decimalFormat.format(currentCalculation.getNightDutyAllowance())).append("\n");
        sb.append("🎉 Holiday Allowance Paid: ").append(currentCalculation.isHolidayAllowancePaid() ? "Yes" : "No").append("\n");
        if (!TextUtils.isEmpty(currentCalculation.getLeaveStatus())) sb.append("📋 Leave: ").append(currentCalculation.getLeaveStatus()).append("\n");
        if (!TextUtils.isEmpty(currentCalculation.getNotes())) sb.append("ℹ ").append(currentCalculation.getNotes()).append("\n");

        tvResults.setText(sb.toString());
        llResults.setVisibility(View.VISIBLE);
    }

    private void saveRecord() {
        if (currentCalculation == null) {
            Toast.makeText(this, "Please calculate before saving", Toast.LENGTH_SHORT).show();
            return;
        }
        // Persist to memory list + SharedPreferences
        dutyRecords.add(currentCalculation);
        persistDutyRecords();

        // Save to SQLite DB as well (for History/export)
        boolean saved = dbHelper.insertDuty(
                currentCalculation.getDate(),
                currentCalculation.getDutyFrom(),
                currentCalculation.getDutyTo(),
                currentCalculation.getTotalDutyHours(),
                currentCalculation.getTotalNightHours(),
                currentCalculation.getBasicPay(),
                currentCalculation.getDearnessAllowance(),
                currentCalculation.getNightDutyAllowance(),
                currentCalculation.isHolidayAllowancePaid(),
                currentCalculation.getAllowanceStatus(),
                currentCalculation.isWeeklyRest(),
                currentCalculation.isWeeklyRestDuty(),
                currentCalculation.isDualDuty(),
                currentCalculation.getLeaveStatus(),
                currentCalculation.getNotes()
        );

        Toast.makeText(this, saved ? "Record saved" : "Record saved (memory) - DB save failed", Toast.LENGTH_SHORT).show();

        // reset currentCalculation (optional)
        currentCalculation = null;
        tvResults.setText("");
        llResults.setVisibility(View.GONE);
    }

    private void showRecordsDialog() {
        if (dutyRecords == null || dutyRecords.isEmpty()) {
            Toast.makeText(this, "No saved records", Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuilder sb = new StringBuilder();
        int idx = 1;
        for (DutyRecord r : dutyRecords) {
            sb.append(idx++).append(". ").append(r.getDate())
                    .append(" | ").append(r.getDutyFrom()).append("-").append(r.getDutyTo())
                    .append(" | Night: ").append(String.format(Locale.getDefault(), "%.2f", r.getTotalNightHours())).append("h")
                    .append(" | NDA: ₹").append(decimalFormat.format(r.getNightDutyAllowance()))
                    .append(r.isHolidayAllowancePaid() ? " | Holiday Paid" : "")
                    .append(!TextUtils.isEmpty(r.getNotes()) ? " | " + r.getNotes() : "")
                    .append("\n\n");
        }
        new AlertDialog.Builder(this)
                .setTitle("Saved Records")
                .setMessage(sb.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void checkCeilingLimit() {
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

    private void clearAll() {
        etDutyDate.setText("");
        etDutyFrom.setText("");
        etDutyTo.setText("");
        etBasicPay.setText("");
        etDearnessAllowance.setText("0");
        etCeilingLimit.setText(String.valueOf(DEFAULT_CEILING));
        cbNationalHoliday.setChecked(false);
        cbWeeklyRest.setChecked(false);
        cbWeeklyRestDuty.setChecked(false);
        cbDualDuty.setChecked(false);
        tvResults.setText("");
        llResults.setVisibility(View.GONE);
    }

    private void exportToPDF() {
        // Export dutyRecords and leaveRecords.
        if (dutyRecords == null) dutyRecords = new ArrayList<>();
        if (dutyRecords.isEmpty()) {
            Toast.makeText(this, "No records to export", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument pdf = new PdfDocument();
        Paint paint = new Paint();
        int pageNumber = 1;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595,842,pageNumber).create();
        PdfDocument.Page page = pdf.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        int y = 40;

        paint.setColor(Color.BLACK);
        paint.setTextSize(16);
        paint.setFakeBoldText(true);
        canvas.drawText("NIGHT DUTY ALLOWANCE REPORT", 40, y, paint);
        y += 30;
        paint.setFakeBoldText(false);
        paint.setTextSize(12);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        canvas.drawText("Generated: " + sdf.format(new Date()), 40, y, paint);
        y += 24;

        // Headers
        canvas.drawText("Date", 40, y, paint);
        canvas.drawText("Duty (from-to)", 120, y, paint);
        canvas.drawText("Day hrs", 280, y, paint);
        canvas.drawText("Night hrs", 340, y, paint);
        canvas.drawText("NDA (₹)", 420, y, paint);
        canvas.drawText("Holiday Paid", 500, y, paint);
        y += 18;

        double totalNightSum = 0.0;
        double totalNdaSum = 0.0;
        int count = 0;

        for (DutyRecord r : dutyRecords) {
            count++;
            if (y > 780) {
                pdf.finishPage(page);
                pageNumber++;
                pageInfo = new PdfDocument.PageInfo.Builder(595,842,pageNumber).create();
                page = pdf.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 40;
            }

            String dutyTime = r.getDutyFrom() + "-" + r.getDutyTo();
            canvas.drawText(r.getDate(), 40, y, paint);
            canvas.drawText(dutyTime, 120, y, paint);
            canvas.drawText(String.format(Locale.getDefault(), "%.2f", r.getTotalDutyHours()), 280, y, paint);
            canvas.drawText(String.format(Locale.getDefault(), "%.2f", r.getTotalNightHours()), 340, y, paint);
            canvas.drawText(decimalFormat.format(r.getNightDutyAllowance()), 420, y, paint);
            canvas.drawText(r.isHolidayAllowancePaid() ? "Yes" : "No", 500, y, paint);
            y += 18;

            // details line
            String notes = "";
            if (!TextUtils.isEmpty(r.getLeaveStatus())) notes += "Leave: " + r.getLeaveStatus() + ". ";
            if (!TextUtils.isEmpty(r.getNotes())) notes += r.getNotes();
            if (!TextUtils.isEmpty(notes)) {
                if (y > 780) {
                    pdf.finishPage(page);
                    pageNumber++;
                    pageInfo = new PdfDocument.PageInfo.Builder(595,842,pageNumber).create();
                    page = pdf.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 40;
                }
                canvas.drawText(notes, 60, y, paint);
                y += 16;
            }

            totalNightSum += r.getTotalNightHours();
            totalNdaSum += r.getNightDutyAllowance();
        }

        // Summary (only night hours count in total)
        y += 10;
        canvas.drawText("SUMMARY", 40, y, paint); y += 18;
        canvas.drawText("Total Night Hours: " + String.format(Locale.getDefault(), "%.2f", totalNightSum) + " hrs", 40, y, paint); y += 18;
        canvas.drawText("Total Night Duty Allowance: ₹" + decimalFormat.format(totalNdaSum), 40, y, paint); y += 18;
        canvas.drawText("Total Records: " + count, 40, y, paint);

        // Append Leave Records, if any
        SharedPreferences leavePrefs = getSharedPreferences("LeaveRecords", Context.MODE_PRIVATE);
        String leaveJson = leavePrefs.getString("leave_records", "[]");
        Type lt = new TypeToken<List<LeaveRecord>>(){}.getType();
        List<LeaveRecord> leaveRecords = gson.fromJson(leaveJson, lt);
        if (leaveRecords != null && !leaveRecords.isEmpty()) {
            y += 24;
            canvas.drawText("LEAVE RECORDS:", 40, y, paint); y += 18;
            for (LeaveRecord lr : leaveRecords) {
                if (y > 780) {
                    pdf.finishPage(page);
                    pageNumber++;
                    pageInfo = new PdfDocument.PageInfo.Builder(595,842,pageNumber).create();
                    page = pdf.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 40;
                }
                canvas.drawText(lr.getLeaveFrom() + " to " + lr.getLeaveTo() + " | " + lr.getLeaveType() + " | " + lr.getStatus(), 40, y, paint);
                y += 16;
            }
        }

        pdf.finishPage(page);

        try {
            File pdfFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "night_duty_report.pdf");
            pdf.writeTo(new FileOutputStream(pdfFile));
            pdf.close();

            Uri pdfUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", pdfFile);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share PDF"));
            Toast.makeText(this, "PDF exported", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Export error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

} // end of MainActivity
