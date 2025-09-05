package com.yourname.nightdutycalculator;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

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

    // UI Elements
    private TextInputEditText etDutyDate, etDutyFrom, etDutyTo, etBasicPay, etCeilingLimit, etDearnessAllowance;
    private CheckBox cbDualDuty, cbWeeklyRestDuty;
    private MaterialButton btnMorningDuty, btnEveningDuty, btnNightDuty;
    private MaterialButton btnCalculate, btnSave, btnViewRecord, btnExport, btnClear, btnExit, btnLeaveManagement;
    private TextView tvResults, tvCeilingWarning;
    private LinearLayout llResults;

    // Data
    private List<DutyRecord> dutyRecords;
    private Gson gson;
    private DecimalFormat decimalFormat;
    private static final double DEFAULT_CEILING = 18000.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Core utilities
        gson = new Gson();
        decimalFormat = new DecimalFormat("#,##0.00");

        // Find views
        etDutyDate = findViewById(R.id.etDutyDate);
        etDutyFrom = findViewById(R.id.etDutyFrom);
        etDutyTo = findViewById(R.id.etDutyTo);
        etBasicPay = findViewById(R.id.etBasicPay);
        etCeilingLimit = findViewById(R.id.etCeilingLimit);
        etDearnessAllowance = findViewById(R.id.etDearnessAllowance);

        cbDualDuty = findViewById(R.id.cbDualDuty);
        cbWeeklyRestDuty = findViewById(R.id.cbWeeklyRestDuty);

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

        // Defaults
        etCeilingLimit.setText(String.valueOf(DEFAULT_CEILING));
        etDearnessAllowance.setText("0");
        setDefaultDateTimeValues();

        // Load records
        loadDutyRecords();

        // Date/time pickers
        etDutyDate.setOnClickListener(v -> showDatePicker());
        etDutyFrom.setOnClickListener(v -> showTimePicker(etDutyFrom));
        etDutyTo.setOnClickListener(v -> showTimePicker(etDutyTo));

        // Quick duty buttons
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

        // Buttons
        btnCalculate.setOnClickListener(v -> calculateAndDisplay());
        btnSave.setOnClickListener(v -> saveRecord());
        btnViewRecord.setOnClickListener(v -> showRecordsDialog());
        btnExport.setOnClickListener(v -> exportToPDF());
        btnClear.setOnClickListener(v -> clearAll());
        btnExit.setOnClickListener(v -> finish());
        btnLeaveManagement.setOnClickListener(v -> startActivity(new Intent(this, LeaveManagementActivity.class)));
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

    // Date/time pickers
    private void showDatePicker() {
        // Implement date picker
    }

    private void showTimePicker(TextInputEditText et) {
        // Implement time picker
    }

    // Placeholder methods for Part 2
    private void calculateAndDisplay() {}
    private void saveRecord() {}
    private void showRecordsDialog() {}
    private void exportToPDF() {}
    private void clearAll() {}
}
// Part 2 – inside MainActivity.java

private void calculateAndDisplay() {
    try {
        String fromTime = etDutyFrom.getText().toString().trim();
        String toTime = etDutyTo.getText().toString().trim();

        if (fromTime.isEmpty() || toTime.isEmpty()) {
            Toast.makeText(this, "Please enter duty times.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse hours and minutes
        String[] startSplit = fromTime.split(":");
        String[] endSplit = toTime.split(":");
        int startHour = Integer.parseInt(startSplit[0]);
        int startMinute = Integer.parseInt(startSplit[1]);
        int endHour = Integer.parseInt(endSplit[0]);
        int endMinute = Integer.parseInt(endSplit[1]);

        // Calculate total duty hours
        double dutyHours = calculateDutyHours(startHour, startMinute, endHour, endMinute);

        // Calculate night duty hours (22:00 – 06:00)
        double nightDutyHours = calculateNightDutyHours(startHour, startMinute, endHour, endMinute);

        // Prepare result string
        StringBuilder result = new StringBuilder();
        result.append("Total Duty Hours: ").append(decimalFormat.format(dutyHours)).append("\n");
        result.append("Night Duty Hours: ").append(decimalFormat.format(nightDutyHours)).append("\n");

        // Dual duty check
        if (cbDualDuty.isChecked()) {
            result.append("Dual Duty: CR pending\n");
        }

        // Weekly rest duty check
        if (cbWeeklyRestDuty.isChecked()) {
            result.append("Duty on Weekly Rest: CR pending\n");
        }

        // NDA calculation
        double basicPay = parseDouble(etBasicPay.getText().toString());
        double da = parseDouble(etDearnessAllowance.getText().toString());
        double hourlyRate = (basicPay + da) / 200.0;
        double ndaAmount = hourlyRate * (nightDutyHours / 6.0);
        result.append("Night Duty Allowance: ₹").append(decimalFormat.format(ndaAmount));

        // Show results
        tvResults.setText(result.toString());

        // Save current calculation
        currentCalculation = new DutyRecord();
        currentCalculation.dutyDate = etDutyDate.getText().toString();
        currentCalculation.startTime = fromTime;
        currentCalculation.endTime = toTime;
        currentCalculation.dutyHours = dutyHours;
        currentCalculation.nightHours = nightDutyHours;
        currentCalculation.dualDuty = cbDualDuty.isChecked();
        currentCalculation.weeklyRestDuty = cbWeeklyRestDuty.isChecked();
        currentCalculation.ndaAmount = ndaAmount;

    } catch (Exception e) {
        Toast.makeText(this, "Error in calculation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }
}

private double calculateDutyHours(int startHour, int startMinute, int endHour, int endMinute) {
    int startTotalMin = startHour * 60 + startMinute;
    int endTotalMin = endHour * 60 + endMinute;
    if (endTotalMin <= startTotalMin) {
        endTotalMin += 24 * 60; // handle overnight duty
    }
    return (endTotalMin - startTotalMin) / 60.0;
}

private double calculateNightDutyHours(int startHour, int startMinute, int endHour, int endMinute) {
    int nightStart = 22 * 60; // 22:00
    int nightEnd = 6 * 60;    // 06:00
    int startTotalMin = startHour * 60 + startMinute;
    int endTotalMin = endHour * 60 + endMinute;
    if (endTotalMin <= startTotalMin) endTotalMin += 24 * 60;

    double nightHours = 0;

    // Loop each minute to count night hours
    for (int t = startTotalMin; t < endTotalMin; t++) {
        int minuteOfDay = t % (24 * 60);
        if (minuteOfDay >= nightStart || minuteOfDay < nightEnd) {
            nightHours += 1.0 / 60.0;
        }
    }
    return nightHours;
}

private double parseDouble(String s) {
    try {
        return Double.parseDouble(s);
    } catch (Exception e) {
        return 0.0;
    }
}
// Part 3 – inside MainActivity.java

private void saveRecord() {
    if (currentCalculation == null) {
        Toast.makeText(this, "Please calculate before saving.", Toast.LENGTH_SHORT).show();
        return;
    }

    dutyRecords.add(currentCalculation);
    persistDutyRecords();
    Toast.makeText(this, "Record saved successfully!", Toast.LENGTH_SHORT).show();
    currentCalculation = null;
    tvResults.setText("");
}

private void showRecordsDialog() {
    if (dutyRecords.isEmpty()) {
        Toast.makeText(this, "No records available.", Toast.LENGTH_SHORT).show();
        return;
    }

    StringBuilder builder = new StringBuilder();
    for (DutyRecord dr : dutyRecords) {
        builder.append("Date: ").append(dr.dutyDate).append("\n")
               .append("From: ").append(dr.startTime).append(" To: ").append(dr.endTime).append("\n")
               .append("Duty Hours: ").append(decimalFormat.format(dr.dutyHours)).append("\n")
               .append("Night Hours: ").append(decimalFormat.format(dr.nightHours)).append("\n")
               .append("Dual Duty: ").append(dr.dualDuty ? "Yes, CR pending" : "No").append("\n")
               .append("Weekly Rest Duty: ").append(dr.weeklyRestDuty ? "Yes, CR pending" : "No").append("\n")
               .append("NDA Amount: ₹").append(decimalFormat.format(dr.ndaAmount)).append("\n\n");
    }

    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
    dialog.setTitle("Duty Records")
          .setMessage(builder.toString())
          .setPositiveButton("OK", null)
          .show();
}

private void exportToPDF() {
    if (dutyRecords.isEmpty()) {
        Toast.makeText(this, "No records to export.", Toast.LENGTH_SHORT).show();
        return;
    }

    PdfDocument pdfDocument = new PdfDocument();
    PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
    PdfDocument.Page page = pdfDocument.startPage(pageInfo);

    int y = 50;
    Paint paint = new Paint();
    paint.setTextSize(12);

    for (DutyRecord dr : dutyRecords) {
        page.getCanvas().drawText("Date: " + dr.dutyDate, 20, y, paint); y += 20;
        page.getCanvas().drawText("From: " + dr.startTime + " To: " + dr.endTime, 20, y, paint); y += 20;
        page.getCanvas().drawText("Duty Hours: " + decimalFormat.format(dr.dutyHours), 20, y, paint); y += 20;
        page.getCanvas().drawText("Night Hours: " + decimalFormat.format(dr.nightHours), 20, y, paint); y += 20;
        page.getCanvas().drawText("Dual Duty: " + (dr.dualDuty ? "Yes, CR pending" : "No"), 20, y, paint); y += 20;
        page.getCanvas().drawText("Weekly Rest Duty: " + (dr.weeklyRestDuty ? "Yes, CR pending" : "No"), 20, y, paint); y += 20;
        page.getCanvas().drawText("NDA Amount: ₹" + decimalFormat.format(dr.ndaAmount), 20, y, paint); y += 30;
    }

    pdfDocument.finishPage(page);

    String path = getExternalFilesDir(null) + "/DutyRecords.pdf";
    try {
        FileOutputStream fos = new FileOutputStream(path);
        pdfDocument.writeTo(fos);
        pdfDocument.close();
        fos.close();
        Toast.makeText(this, "PDF saved: " + path, Toast.LENGTH_LONG).show();

        // Open PDF
        File file = new File(path);
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);

    } catch (IOException e) {
        Toast.makeText(this, "Error creating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }
}

private void clearAll() {
    etDutyFrom.setText("");
    etDutyTo.setText("");
    etDutyDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
    tvResults.setText("");
    cbDualDuty.setChecked(false);
    cbWeeklyRestDuty.setChecked(false);
    cbWeeklyRest.setChecked(false);
    cbNationalHoliday.setChecked(false);
}
