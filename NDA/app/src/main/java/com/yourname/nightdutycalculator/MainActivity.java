package com.yourname.nightdutycalculator;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
    private CheckBox cbNationalHoliday, cbWeeklyRest, cbWeeklyRestDuty, cbDualDuty;
    private MaterialButton btnMorningDuty, btnEveningDuty, btnNightDuty;
    private MaterialButton btnCalculate, btnSave, btnViewRecord, btnExport, btnClear, btnExit, btnLeaveManagement;
    private TextView tvResults, tvCeilingWarning;
    private LinearLayout llResults;

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

        cbNationalHoliday = findViewById(R.id.cbNationalHoliday);
        cbWeeklyRest = findViewById(R.id.cbWeeklyRest);
        cbWeeklyRestDuty = findViewById(R.id.cbWeeklyRestDuty);
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

        // Defaults
        etCeilingLimit.setText(String.valueOf(DEFAULT_CEILING));
        etDearnessAllowance.setText("0");
        setDefaultDateTimeValues();

        // Load existing records
        loadDutyRecords();

        // Pickers
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

        // Ceiling check
        etBasicPay.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { checkCeiling(); }
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

// Part 2 — Calculation and Display

private void calculateAndDisplay() {
    String fromTime = etDutyFrom.getText().toString();
    String toTime = etDutyTo.getText().toString();

    int startHour = Integer.parseInt(fromTime.split(":")[0]);
    int startMinute = Integer.parseInt(fromTime.split(":")[1]);
    int endHour = Integer.parseInt(toTime.split(":")[0]);
    int endMinute = Integer.parseInt(toTime.split(":")[1]);

    double dutyHours = calculateDutyHours(startHour, startMinute, endHour, endMinute);
    double nightDutyHours = calculateNightDutyHours(startHour, startMinute, endHour, endMinute);

    double basicPay = parseDoubleOrZero(etBasicPay.getText().toString());
    double da = parseDoubleOrZero(etDearnessAllowance.getText().toString());
    double hourlyNDA = (basicPay + da) / 200.0;
    double ndaPay = hourlyNDA * (nightDutyHours / 6.0);

    StringBuilder result = new StringBuilder();
    result.append("Duty Hours: ").append(dutyHours).append("\n");
    result.append("Night Duty Hours: ").append(nightDutyHours).append("\n");
    result.append("NDA Pay: ₹").append(decimalFormat.format(ndaPay)).append("\n");

    // Dual duty check
    if (cbDualDuty.isChecked()) {
        result.append("Dual Duty: CR pending\n");
    }

    // Weekly rest duty check
    if (cbWeeklyRestDuty.isChecked()) {
        result.append("Duty on Weekly Rest: CR pending\n");
    }

    // Old weekly rest (holiday)
    if (cbWeeklyRest.isChecked()) {
        result.append("Weekly Rest Day: No duty\n");
    }

    tvResults.setText(result.toString());
}

private double calculateDutyHours(int startHour, int startMinute, int endHour, int endMinute) {
    int startTotal = startHour * 60 + startMinute;
    int endTotal = endHour * 60 + endMinute;

    if (endTotal <= startTotal) {
        endTotal += 24 * 60; // cross midnight
    }
    return (endTotal - startTotal) / 60.0;
}

private double calculateNightDutyHours(int startHour, int startMinute, int endHour, int endMinute) {
    int startTotal = startHour * 60 + startMinute;
    int endTotal = endHour * 60 + endMinute;
    if (endTotal <= startTotal) endTotal += 24 * 60;

    // Night duty from 22:00 to 06:00
    int nightStart = 22 * 60;
    int nightEnd = 6 * 60 + 24 * 60; // handle next day

    int nightDutyMinutes = Math.max(0, Math.min(endTotal, nightEnd) - Math.max(startTotal, nightStart));

    // Adjust if nightEnd was next day
    if (nightEnd > 24 * 60) {
        if (startTotal < 24 * 60 && endTotal > 24 * 60) {
            nightDutyMinutes += Math.min(endTotal - 24 * 60, 6 * 60);
        }
    }

    return nightDutyMinutes / 60.0;
}

private double parseDoubleOrZero(String s) {
    try {
        return Double.parseDouble(s);
    } catch (NumberFormatException e) {
        return 0.0;
    }
}

private void checkCeiling() {
    double basicPay = parseDoubleOrZero(etBasicPay.getText().toString());
    double ceiling = parseDoubleOrZero(etCeilingLimit.getText().toString());

    if (basicPay > ceiling) {
        tvCeilingWarning.setText("Warning: Basic pay exceeds ceiling!");
        tvCeilingWarning.setTextColor(Color.RED);
    } else {
        tvCeilingWarning.setText("");
    }
}
// Part 3 — Save, View, Clear, Export PDF

private void saveRecord() {
    String dutyDate = etDutyDate.getText().toString();
    String fromTime = etDutyFrom.getText().toString();
    String toTime = etDutyTo.getText().toString();

    int startHour = Integer.parseInt(fromTime.split(":")[0]);
    int startMinute = Integer.parseInt(fromTime.split(":")[1]);
    int endHour = Integer.parseInt(toTime.split(":")[0]);
    int endMinute = Integer.parseInt(toTime.split(":")[1]);

    double dutyHours = calculateDutyHours(startHour, startMinute, endHour, endMinute);
    double nightHours = calculateNightDutyHours(startHour, startMinute, endHour, endMinute);

    DutyRecord record = new DutyRecord();
    record.date = dutyDate;
    record.fromTime = fromTime;
    record.toTime = toTime;
    record.dutyHours = dutyHours;
    record.nightHours = nightHours;
    record.basicPay = parseDoubleOrZero(etBasicPay.getText().toString());
    record.dearnessAllowance = parseDoubleOrZero(etDearnessAllowance.getText().toString());
    record.dualDuty = cbDualDuty.isChecked();
    record.weeklyRestDuty = cbWeeklyRestDuty.isChecked();
    record.weeklyRest = cbWeeklyRest.isChecked();

    dutyRecords.add(record);
    persistDutyRecords();

    Toast.makeText(this, "Record saved!", Toast.LENGTH_SHORT).show();
    calculateAndDisplay();
}

private void showRecordsDialog() {
    if (dutyRecords.isEmpty()) {
        Toast.makeText(this, "No records available!", Toast.LENGTH_SHORT).show();
        return;
    }

    StringBuilder sb = new StringBuilder();
    for (DutyRecord r : dutyRecords) {
        sb.append("Date: ").append(r.date)
          .append("\nDuty: ").append(r.fromTime).append(" - ").append(r.toTime)
          .append("\nDuty Hours: ").append(r.dutyHours)
          .append(", Night Hours: ").append(r.nightHours)
          .append("\nDual Duty: ").append(r.dualDuty ? "Yes (CR pending)" : "No")
          .append("\nWeekly Rest Duty: ").append(r.weeklyRestDuty ? "Yes (CR pending)" : "No")
          .append("\nWeekly Rest: ").append(r.weeklyRest ? "Yes" : "No")
          .append("\nBasic Pay: ₹").append(decimalFormat.format(r.basicPay))
          .append(", DA: ₹").append(decimalFormat.format(r.dearnessAllowance))
          .append("\n-----------------------------\n");
    }

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Duty Records");
    builder.setMessage(sb.toString());
    builder.setPositiveButton("OK", null);
    builder.show();
}

private void clearAll() {
    etDutyFrom.setText("");
    etDutyTo.setText("");
    etBasicPay.setText("");
    etDearnessAllowance.setText("");
    cbDualDuty.setChecked(false);
    cbWeeklyRestDuty.setChecked(false);
    cbWeeklyRest.setChecked(false);
    tvResults.setText("");
}

private void exportToPDF() {
    if (dutyRecords.isEmpty()) {
        Toast.makeText(this, "No records to export!", Toast.LENGTH_SHORT).show();
        return;
    }

    PdfDocument pdf = new PdfDocument();
    PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
    PdfDocument.Page page = pdf.startPage(info);

    int x = 10, y = 25;
    Paint paint = new Paint();
    for (DutyRecord r : dutyRecords) {
        String line = "Date: " + r.date + ", Duty: " + r.fromTime + "-" + r.toTime
                + ", DutyHrs: " + r.dutyHours + ", NightHrs: " + r.nightHours
                + ", DualDuty: " + (r.dualDuty ? "Yes" : "No")
                + ", WeeklyRestDuty: " + (r.weeklyRestDuty ? "Yes" : "No")
                + ", WeeklyRest: " + (r.weeklyRest ? "Yes" : "No")
                + ", BasicPay: " + decimalFormat.format(r.basicPay)
                + ", DA: " + decimalFormat.format(r.dearnessAllowance);
        page.getCanvas().drawText(line, x, y, paint);
        y += 20;
    }

    pdf.finishPage(page);

    File file = new File(getExternalFilesDir(null), "DutyRecords.pdf");
    try {
        pdf.writeTo(new FileOutputStream(file));
        Toast.makeText(this, "PDF exported: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    } catch (IOException e) {
        e.printStackTrace();
        Toast.makeText(this, "Error exporting PDF", Toast.LENGTH_SHORT).show();
    }

    pdf.close();
}
