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
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.TextView;

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
        setContentView(R.layout.activity_main); // ensure layout has correct IDs

        gson = new Gson();
        decimalFormat = new DecimalFormat("#,##0.00");

        // find views
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

        tvResults = findViewById(R.id.tvResults);
        tvCeilingWarning = findViewById(R.id.tvCeilingWarning);
        llResults = findViewById(R.id.llResults);

        // defaults
        etCeilingLimit.setText(String.valueOf(DEFAULT_CEILING));
        etDearnessAllowance.setText("0");
        setDefaultDateTimeValues();

        // load saved data
        loadDutyRecords();
        loadLeaveRecords(); // ensures leaveRecords available for checks & PDF

        // pickers / quick duty buttons
        etDutyDate.setOnClickListener(v -> showDatePicker());
        etDutyFrom.setOnClickListener(v -> showTimePicker(etDutyFrom));
        etDutyTo.setOnClickListener(v -> showTimePicker(etDutyTo));

        btnMorningDuty.setOnClickListener(v -> { etDutyFrom.setText("08:00"); etDutyTo.setText("16:00"); });
        btnEveningDuty.setOnClickListener(v -> { etDutyFrom.setText("16:00"); etDutyTo.setText("00:00"); });
        btnNightDuty.setOnClickListener(v -> { etDutyFrom.setText("00:00"); etDutyTo.setText("08:00"); });

        // buttons
        btnCalculate.setOnClickListener(v -> calculateAndDisplay());
        btnSave.setOnClickListener(v -> saveRecord());
        btnViewRecord.setOnClickListener(v -> showRecordsDialog());
        btnExport.setOnClickListener(v -> exportToPDF());
        btnClear.setOnClickListener(v -> clearAll());
        btnExit.setOnClickListener(v -> finish());
        btnLeaveManagement.setOnClickListener(v -> startActivity(new Intent(this, LeaveManagementActivity.class)));

        // ceiling check
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

    private void loadLeaveRecords() {
        SharedPreferences prefs = getSharedPreferences("LeaveRecords", Context.MODE_PRIVATE);
        String json = prefs.getString("leave_records", "[]");
        Type t = new TypeToken<List<LeaveRecord>>(){}.getType();
        List<LeaveRecord> loaded = gson.fromJson(json, t);
        if (loaded != null) {
            leaveRecords.clear();
            leaveRecords.addAll(loaded);
        }
    }

    // date picker for duty date
    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            c.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            etDutyDate.setText(sdf.format(c.getTime()));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    // time picker (24-hour) for duty from/to
    private void showTimePicker(final TextInputEditText target) {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (TimePicker view, int hourOfDay, int minute) -> {
            target.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

//part 2 started 
    // ---------- calculation and helpers ----------

    private void calculateAndDisplay() {
        try {
            String dutyDate = etDutyDate.getText().toString().trim();
            String dutyFrom = etDutyFrom.getText().toString().trim();
            String dutyTo = etDutyTo.getText().toString().trim();
            double basicPay = parseDoubleOrZero(etBasicPay.getText().toString().trim());
            double ceiling = parseDoubleOrZero(etCeilingLimit.getText().toString().trim());
            double daPercent = parseDoubleOrZero(etDearnessAllowance.getText().toString().trim());

            boolean isHoliday = cbNationalHoliday.isChecked();
            boolean isWeeklyRest = cbWeeklyRest.isChecked();
            boolean isDualDuty = cbDualDuty.isChecked();
            boolean isDutyOnWeeklyRest = cbDutyOnWeeklyRest.isChecked();

            if (dutyDate.isEmpty() || dutyFrom.isEmpty() || dutyTo.isEmpty()) {
                Toast.makeText(this, "Please fill date/from/to", Toast.LENGTH_SHORT).show();
                return;
            }

            // check if on leave on this date (Applied/Approved)
            boolean isOnLeave = false;
            String leaveStatus = "";
            if (leaveRecords != null && !leaveRecords.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Calendar dutyCal = Calendar.getInstance();
                dutyCal.setTime(sdf.parse(dutyDate));
                for (LeaveRecord lr : leaveRecords) {
                    if (lr == null) continue;
                    String status = lr.getStatus();
                    if (status == null) continue;
                    if (status.equalsIgnoreCase("Applied") || status.equalsIgnoreCase("Approved")) {
                        Calendar lf = Calendar.getInstance();
                        Calendar lt = Calendar.getInstance();
                        lf.setTime(sdf.parse(lr.getLeaveFrom()));
                        lt.setTime(sdf.parse(lr.getLeaveTo()));
                        if (!dutyCal.before(lf) && !dutyCal.after(lt)) {
                            isOnLeave = true;
                            leaveStatus = "On Leave (" + lr.getLeaveType() + ": " + lr.getLeaveFrom() + " to " + lr.getLeaveTo() + ")";
                            break;
                        }
                    }
                }
            }

            double effectiveBasic = Math.min(basicPay, ceiling);
            double salaryWithDA = effectiveBasic * (1 + daPercent / 100.0);

            // parse times & create calendars
            String[] fromParts = dutyFrom.split(":");
            String[] toParts = dutyTo.split(":");
            Calendar fromCal = Calendar.getInstance();
            Calendar toCal = Calendar.getInstance();
            fromCal.set(Calendar.SECOND, 0);
            toCal.set(Calendar.SECOND, 0);
            fromCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(fromParts[0]));
            fromCal.set(Calendar.MINUTE, Integer.parseInt(fromParts[1]));
            toCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(toParts[0]));
            toCal.set(Calendar.MINUTE, Integer.parseInt(toParts[1]));
            if (!toCal.after(fromCal)) toCal.add(Calendar.DAY_OF_MONTH, 1);

            double totalDutyHours = (toCal.getTimeInMillis() - fromCal.getTimeInMillis()) / (1000.0 * 60 * 60);
            double night1 = calculateNightHours(fromCal, toCal, 22, 0, 0, 0);
            double night2 = calculateNightHours(fromCal, toCal, 0, 0, 6, 0);
            double totalNightHours = night1 + night2;

            // default
            boolean holidayAllowance = false;
            double nightDutyAllowance = 0.0;
            String allowanceStatus = "No Allowance";

            // Apply rules (as you described)
            if (isOnLeave) {
                // 3 & 4: on leave => no NDA, no duty hours counted; if holiday falls in leave => no holiday allowance
                totalDutyHours = 0.0;
                totalNightHours = 0.0;
                holidayAllowance = false;
                nightDutyAllowance = 0.0;
                allowanceStatus = "No Allowance (On Leave)";
            } else if (isWeeklyRest && !isDutyOnWeeklyRest) {
                // 7 & 5: weekly rest day with NO duty => no duty hours; if weekly rest falls on holiday then holiday allowance paid
                totalDutyHours = 0.0;
                totalNightHours = 0.0;
                holidayAllowance = isHoliday;
                nightDutyAllowance = 0.0;
                allowanceStatus = isHoliday ? "Holiday Allowance (Weekly Rest)" : "No Allowance (Weekly Rest)";
            } else {
                // Normal duty day OR duty on weekly rest (isDutyOnWeeklyRest==true)
                if (isHoliday && !isOnLeave) {
                    // 1 & 2: if duty on holiday -> holiday allowance paid (and NDA if night hours exist)
                    holidayAllowance = true;
                }
                if (totalNightHours > 0.0) {
                    double nightHoursDivided = totalNightHours / 6.0;
                    nightDutyAllowance = nightHoursDivided * (salaryWithDA) / 200.0;
                    allowanceStatus = "Calculated";
                } else {
                    allowanceStatus = isHoliday ? "Holiday Allowance (No Night Hours)" : "No Night Hours";
                }
            }

            // Build result object
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
            currentCalculation.setWeeklyRest(isWeeklyRest);
            currentCalculation.setAllowanceStatus(allowanceStatus);
            currentCalculation.setLeaveStatus(leaveStatus);
            currentCalculation.setHolidayAllowancePaid(holidayAllowance);
            currentCalculation.setDualDuty(isDualDuty);
            currentCalculation.setDutyOnWeeklyRest(isDutyOnWeeklyRest);

            displayCurrentCalculation();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: fill fields correctly", Toast.LENGTH_SHORT).show();
        }
    }

    // overlap calculation for night windows (handles crossing midnight)
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
        sb.append("💰 Night NDA: ₹").append(decimalFormat.format(currentCalculation.getNightDutyAllowance())).append("\n");
        sb.append("🎉 Holiday Allowance Paid: ").append(currentCalculation.isHolidayAllowancePaid() ? "Yes" : "No").append("\n");
        if (currentCalculation.getLeaveStatus() != null && !currentCalculation.getLeaveStatus().isEmpty()) {
            sb.append("📋 ").append(currentCalculation.getLeaveStatus()).append("\n");
        }
        if (currentCalculation.isDualDuty()) sb.append("⚠ Dual Duty: CR pending\n");
        if (currentCalculation.isDutyOnWeeklyRest()) sb.append("⚠ Duty on Weekly Rest: CR pending\n");

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

    private double parseDoubleOrZero(String s) {
        try { return Double.parseDouble(s); } catch (Exception e) { return 0.0; }
    }

    private void checkCeiling() {
        try {
            double basic = parseDoubleOrZero(etBasicPay.getText().toString());
            double ceiling = parseDoubleOrZero(etCeilingLimit.getText().toString());
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

    // ---------- PDF Export (Duty + Leave) ----------
    
        private void exportToPDF() {
    // ensure leaveRecords up to date
    loadLeaveRecords();

    if ((dutyRecords == null || dutyRecords.isEmpty()) && (leaveRecords == null || leaveRecords.isEmpty())) {
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

    // Title
    canvas.drawText("NIGHT DUTY & LEAVE REPORT", x, y, paint);
    y += 25;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    canvas.drawText("Generated: " + sdf.format(new Date()), x, y, paint);
    y += 30;

    // Duty records section
    if (dutyRecords != null && !dutyRecords.isEmpty()) {
        canvas.drawText("----- Duty Records -----", x, y, paint);
        y += 18;
        canvas.drawText("Date", x, y, paint);
        canvas.drawText("Duty", x + 90, y, paint);
        canvas.drawText("Night hrs", x + 240, y, paint);
        canvas.drawText("NDA", x + 330, y, paint);
        canvas.drawText("HolidayPaid", x + 420, y, paint);
        y += 16;

        double totalNightH = 0.0;
        double totalNDA = 0.0;

        for (DutyRecord r : dutyRecords) {
            if (y > 760) {
                pdfDoc.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pdfDoc.getPages().size() + 1).create();
                page = pdfDoc.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 40;
            }
            canvas.drawText(r.getDate(), x, y, paint);
            canvas.drawText(r.getDutyFrom() + " - " + r.getDutyTo(), x + 90, y, paint);
            canvas.drawText(String.format(Locale.getDefault(), "%.2f", r.getTotalNightHours()), x + 240, y, paint);
            canvas.drawText("₹" + decimalFormat.format(r.getNightDutyAllowance()), x + 330, y, paint);
            canvas.drawText(r.isHolidayAllowancePaid() ? "Yes" : "No", x + 420, y, paint);
            y += 16;

            if (r.isDualDuty()) {
                canvas.drawText("Dual Duty: CR pending", x + 90, y, paint);
                y += 14;
            }
            if (r.isDutyOnWeeklyRest()) {
                canvas.drawText("Duty on Weekly Rest: CR pending", x + 90, y, paint);
                y += 14;
            }

            totalNightH += r.getTotalNightHours();
            totalNDA += r.getNightDutyAllowance();
        }

        y += 12;
        canvas.drawText("Duty Summary: Total Night Hours: " + String.format(Locale.getDefault(), "%.2f", totalNightH) + " hrs", x, y, paint);
        y += 14;
        canvas.drawText("Total NDA: ₹" + decimalFormat.format(totalNDA), x, y, paint);
        y += 18;
    }

    // Leave records section
    if (leaveRecords != null && !leaveRecords.isEmpty()) {
        if (y > 700) {
            pdfDoc.finishPage(page);
            pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pdfDoc.getPages().size() + 1).create();
            page = pdfDoc.startPage(pageInfo);
            canvas = page.getCanvas();
            y = 40;
        }
        canvas.drawText("----- Leave Records -----", x, y, paint);
        y += 18;
        canvas.drawText("From - To", x, y, paint);
        canvas.drawText("Type", x + 160, y, paint);
        canvas.drawText("Status", x + 300, y, paint);
        canvas.drawText("Notes", x + 380, y, paint);
        y += 16;

        // map to group leaves by type
        Map<String, Integer> leaveSummary = new HashMap<>();

        for (LeaveRecord lr : leaveRecords) {
            if (y > 760) {
                pdfDoc.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pdfDoc.getPages().size() + 1).create();
                page = pdfDoc.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 40;
            }
            canvas.drawText(lr.getLeaveFrom() + " - " + lr.getLeaveTo(), x, y, paint);
            canvas.drawText(lr.getLeaveType(), x + 160, y, paint);
            canvas.drawText(lr.getStatus(), x + 300, y, paint);
            if (lr.getNotes() != null && !lr.getNotes().trim().isEmpty()) {
                canvas.drawText(lr.getNotes(), x + 380, y, paint);
            }
            y += 16;

            // group leaves by type
            int days = calculateLeaveDays(lr.getLeaveFrom(), lr.getLeaveTo());
            leaveSummary.put(lr.getLeaveType(), leaveSummary.getOrDefault(lr.getLeaveType(), 0) + days);
        }

        y += 12;
        canvas.drawText("Leave Summary:", x, y, paint);
        y += 16;
        for (Map.Entry<String, Integer> entry : leaveSummary.entrySet()) {
            canvas.drawText(entry.getValue() + " days " + entry.getKey(), x + 20, y, paint);
            y += 16;
        }
    }

    pdfDoc.finishPage(page);

    try {
        File file = new File(getExternalFilesDir(null), "duty_leave_report.pdf");
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

/**
 * Helper method to calculate leave days between from/to.
 */
private int calculateLeaveDays(String from, String to) {
    try {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date start = sdf.parse(from);
        Date end = sdf.parse(to);
        if (start != null && end != null) {
            long diff = end.getTime() - start.getTime();
            return (int) (diff / (1000 * 60 * 60 * 24)) + 1; // inclusive
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return 1;
}}
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
} // end MainActivity
