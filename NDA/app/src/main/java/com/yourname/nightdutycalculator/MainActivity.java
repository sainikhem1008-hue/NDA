package com.yourname.nightdutycalculator;
import android.net.Uri;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

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
    private CheckBox cbNationalHoliday, cbWeeklyRest;
    private MaterialButton btnCalculate, btnSave, btnExportPdf, btnClearAll, btnExit, btnLeaveManagement;
    private TextView tvResults, tvCeilingWarning;
    private LinearLayout llResults;
    private List<DutyRecord> dutyRecords;
    private DutyRecord currentCalculation;
    private Gson gson;
    private DecimalFormat decimalFormat;
    private static final double CEILING_LIMIT = 18000.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupListeners();
        loadDutyRecords();
        setDefaultValues();
    }

    private void initializeViews() {
        etDutyDate = findViewById(R.id.etDutyDate);
        etDutyFrom = findViewById(R.id.etDutyFrom);
        etDutyTo = findViewById(R.id.etDutyTo);
        etBasicPay = findViewById(R.id.etBasicPay);
        etCeilingLimit = findViewById(R.id.etCeilingLimit);
        etDearnessAllowance = findViewById(R.id.etDearnessAllowance);
        cbNationalHoliday = findViewById(R.id.cbNationalHoliday);
        cbWeeklyRest = findViewById(R.id.cbWeeklyRest);
        btnCalculate = findViewById(R.id.btnCalculate);
        btnSave = findViewById(R.id.btnSave);
        btnExportPdf = findViewById(R.id.btnExport);
        btnClearAll = findViewById(R.id.btnClear);
        btnExit = findViewById(R.id.btnExit);
        btnLeaveManagement = findViewById(R.id.btnLeaveManagement);
        tvResults = findViewById(R.id.tvResults);
        tvCeilingWarning = findViewById(R.id.tvCeilingWarning);
        llResults = findViewById(R.id.llResults);

        dutyRecords = new ArrayList<>();
        gson = new Gson();
        decimalFormat = new DecimalFormat("#,##0.00");

        etCeilingLimit.setText(String.valueOf(CEILING_LIMIT));
        etDearnessAllowance.setText("0");
    }

    private void setupListeners() {
        etDutyDate.setOnClickListener(v -> showDatePicker());
        etDutyFrom.setOnClickListener(v -> showTimePicker(etDutyFrom));
        etDutyTo.setOnClickListener(v -> showTimePicker(etDutyTo));

        btnCalculate.setOnClickListener(v -> calculateNightDuty());
        btnSave.setOnClickListener(v -> saveRecord());
        btnExportPdf.setOnClickListener(v -> exportToPDF());
        btnClearAll.setOnClickListener(v -> clearAll());
        btnExit.setOnClickListener(v -> finish());
        btnLeaveManagement.setOnClickListener(v -> openLeaveManagement());

        etBasicPay.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) { checkCeilingLimit(); }
        });
    }

    private void loadDutyRecords() {
        SharedPreferences prefs = getSharedPreferences("DutyRecords", Context.MODE_PRIVATE);
        String recordsJson = prefs.getString("duty_records", "[]");
        Type recordListType = new TypeToken<List<DutyRecord>>() {}.getType();
        dutyRecords = gson.fromJson(recordsJson, recordListType);
        if (dutyRecords == null) {
            dutyRecords = new ArrayList<>();
        }
    }

    private void checkCeilingLimit() {
        try {
            double basicPay = Double.parseDouble(etBasicPay.getText().toString());
            double ceilingLimit = Double.parseDouble(etCeilingLimit.getText().toString());

            if (basicPay > ceilingLimit) {
                if (tvCeilingWarning != null) {
                    tvCeilingWarning.setVisibility(View.VISIBLE);
                    tvCeilingWarning.setText("⚠ Using ceiling limit ₹" + decimalFormat.format(ceilingLimit));
                }
            } else if (tvCeilingWarning != null) {
                tvCeilingWarning.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            if (tvCeilingWarning != null) tvCeilingWarning.setVisibility(View.GONE);
        }
    }

    private void setDefaultValues() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        etDutyDate.setText(sdf.format(new Date()));
        etDutyFrom.setText("22:00");
        etDutyTo.setText("06:00");
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    etDutyDate.setText(sdf.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePicker(TextInputEditText editText) {
        Calendar calendar = Calendar.getInstance();
        android.app.TimePickerDialog timePickerDialog = new android.app.TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> editText.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)),
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }

    private void openLeaveManagement() {
        Intent intent = new Intent(MainActivity.this, LeaveManagementActivity.class);
        startActivity(intent);
    }

    private void calculateNightDuty() {
        try {
            String date = etDutyDate.getText().toString();
            String fromTime = etDutyFrom.getText().toString();
            String toTime = etDutyTo.getText().toString();
            double basicPay = Double.parseDouble(etBasicPay.getText().toString());
            double ceilingLimit = Double.parseDouble(etCeilingLimit.getText().toString());
            double daPercent = Double.parseDouble(etDearnessAllowance.getText().toString());

            if (basicPay > ceilingLimit) basicPay = ceilingLimit;
            double effectiveBasic = basicPay + (basicPay * daPercent / 100);

            double totalNightHours = 8; // placeholder – replace with real calc
            double hourlyRate = effectiveBasic / 200;
            double nda = hourlyRate * (totalNightHours / 6);

            currentCalculation = new DutyRecord(date, fromTime, toTime, totalNightHours, effectiveBasic, nda);

            tvResults.setText("Date: " + date +
                    "\nFrom: " + fromTime +
                    "\nTo: " + toTime +
                    "\nEffective Basic: ₹" + decimalFormat.format(effectiveBasic) +
                    "\nNight Duty Hours: " + totalNightHours +
                    "\nAllowance: ₹" + decimalFormat.format(nda));
            llResults.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveRecord() {
        if (currentCalculation != null) {
            dutyRecords.add(currentCalculation);
            SharedPreferences prefs = getSharedPreferences("DutyRecords", Context.MODE_PRIVATE);
            prefs.edit().putString("duty_records", gson.toJson(dutyRecords)).apply();
            Toast.makeText(this, "Record saved!", Toast.LENGTH_SHORT).show();
        }
    }

    private void exportToPDF() {
        if (currentCalculation == null) {
            Toast.makeText(this, "No record to export", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            PdfDocument pdfDoc = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();
            PdfDocument.Page page = pdfDoc.startPage(pageInfo);
            page.getCanvas().drawText("Night Duty Allowance Report", 80, 50, new android.graphics.Paint());
            page.getCanvas().drawText(tvResults.getText().toString(), 40, 100, new android.graphics.Paint());
            pdfDoc.finishPage(page);

            File file = new File(getExternalFilesDir(null), "NightDutyReport.pdf");
            FileOutputStream fos = new FileOutputStream(file);
            pdfDoc.writeTo(fos);
            pdfDoc.close();
            fos.close();

            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(shareIntent, "Share PDF"));

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void clearAll() {
        etDutyDate.setText("");
        etDutyFrom.setText("");
        etDutyTo.setText("");
        etBasicPay.setText("");
        etDearnessAllowance.setText("0");
        tvResults.setText("");
        llResults.setVisibility(View.GONE);
        currentCalculation = null;
    }
}
