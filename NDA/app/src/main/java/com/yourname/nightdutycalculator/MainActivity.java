package com.yourname.nightdutycalculator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.pdf.PrintedPdfDocument;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
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
        Intent intent = new Intent(this, LeaveManagementActivity.class);
        startActivity(intent);
    }

    private double calculateNightHours(Calendar fromCal, Calendar toCal, int startHour, int startMin, int endHour, int endMin) {
        Calendar nightStart = (Calendar) fromCal.clone();
        Calendar nightEnd = (Calendar) fromCal.clone();
        nightStart.set(Calendar.HOUR_OF_DAY, startHour);
        nightStart.set(Calendar.MINUTE, startMin);
        nightStart.set(Calendar.SECOND, 0);
        nightEnd.set(Calendar.HOUR_OF_DAY, endHour);
        nightEnd.set(Calendar.MINUTE, endMin);
        nightEnd.set(Calendar.SECOND, 0);
        if (nightEnd.before(nightStart)) nightEnd.add(Calendar.DAY_OF_MONTH, 1);

        long overlapStart = Math.max(fromCal.getTimeInMillis(), nightStart.getTimeInMillis());
        long overlapEnd = Math.min(toCal.getTimeInMillis(), nightEnd.getTimeInMillis());
        if (overlapEnd > overlapStart)
            return (overlapEnd - overlapStart) / (1000.0 * 60 * 60);
        return 0.0;
    }
    private void calculateNightDuty() {
        try {
            String dutyDate = etDutyDate.getText().toString();
            String dutyFrom = etDutyFrom.getText().toString();
            String dutyTo = etDutyTo.getText().toString();
            double basicPay = Double.parseDouble(etBasicPay.getText().toString());
            double ceilingLimit = Double.parseDouble(etCeilingLimit.getText().toString());
            double dearnessAllowance = Double.parseDouble(etDearnessAllowance.getText().toString());
            boolean isNationalHoliday = cbNationalHoliday.isChecked();
            boolean isWeeklyRest = cbWeeklyRest.isChecked();

            if (dutyDate.isEmpty() || dutyFrom.isEmpty() || dutyTo.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isOnLeave = false;
            String leaveStatus = "";

            SharedPreferences leavePrefs = getSharedPreferences("LeaveRecords", Context.MODE_PRIVATE);
            String leaveRecordsJson = leavePrefs.getString("leave_records", "[]");
            Type leaveListType = new TypeToken<List<LeaveRecord>>(){}.getType();
            List<LeaveRecord> leaveRecords = gson.fromJson(leaveRecordsJson, leaveListType);

            if (leaveRecords != null && !leaveRecords.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Calendar dutyDateCal = Calendar.getInstance();
                    dutyDateCal.setTime(sdf.parse(dutyDate));
                    for (LeaveRecord leaveRecord : leaveRecords) {
                        if (leaveRecord.getStatus().equals("Applied") || leaveRecord.getStatus().equals("Approved")) {
                            Calendar leaveFromCal = Calendar.getInstance();
                            Calendar leaveToCal = Calendar.getInstance();
                            leaveFromCal.setTime(sdf.parse(leaveRecord.getLeaveFrom()));
                            leaveToCal.setTime(sdf.parse(leaveRecord.getLeaveTo()));
                            if (!dutyDateCal.before(leaveFromCal) && !dutyDateCal.after(leaveToCal)) {
                                isOnLeave = true;
                                leaveStatus = "📅 On Leave (" + leaveRecord.getLeaveType() + ": " + leaveRecord.getLeaveFrom() + " to " + leaveRecord.getLeaveTo() + ")";
                                break;
                            }
                        }
                    }
                    if (!isOnLeave && !leaveRecords.isEmpty()) leaveStatus = "📅 Leave Records Available";
                } catch (Exception e) { isOnLeave = false; leaveStatus = "📅 Leave Check Error"; }
            }

            // Duty hours and night hours calculation
            Calendar fromCal = Calendar.getInstance();
            Calendar toCal = Calendar.getInstance();
            String[] fromParts = dutyFrom.split(":");
            String[] toParts = dutyTo.split(":");
            fromCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(fromParts[0]));
            fromCal.set(Calendar.MINUTE, Integer.parseInt(fromParts[1]));
            fromCal.set(Calendar.SECOND, 0);
            toCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(toParts[0]));
            toCal.set(Calendar.MINUTE, Integer.parseInt(toParts[1]));
            toCal.set(Calendar.SECOND, 0);
            if (toCal.before(fromCal) || toCal.equals(fromCal)) toCal.add(Calendar.DAY_OF_MONTH, 1);

            double totalDutyHours = 0.0;
            double nightHours1 = calculateNightHours(fromCal, toCal, 22, 0, 0, 0);
            double nightHours2 = calculateNightHours(fromCal, toCal, 0, 0, 6, 0);
            double totalNightHours = nightHours1 + nightHours2;
            double nightHoursDivided = totalNightHours / 6.0;

            double effectiveBasicPay = Math.min(basicPay, ceilingLimit);
            double nightDutyAllowance = 0.0;

            boolean holidayAllowancePaid = isNationalHoliday || (isWeeklyRest && !isOnLeave);

            if (!isOnLeave && !isWeeklyRest) {
                nightDutyAllowance = nightHoursDivided * (effectiveBasicPay * (1 + dearnessAllowance / 100)) / 200;
            }

            currentCalculation = new DutyRecord();
            currentCalculation.setDate(dutyDate);
            currentCalculation.setDutyFrom(dutyFrom);
            currentCalculation.setDutyTo(dutyTo);
            currentCalculation.setTotalDutyHours(totalDutyHours);
            currentCalculation.setNightHours1(nightHours1);
            currentCalculation.setNightHours2(nightHours2);
            currentCalculation.setTotalNightHours(totalNightHours);
            currentCalculation.setBasicPay(basicPay);
            currentCalculation.setEffectiveBasicPay(effectiveBasicPay);
            currentCalculation.setDearnessAllowance(dearnessAllowance);
            currentCalculation.setNightDutyAllowance(nightDutyAllowance);
            currentCalculation.setNationalHoliday(isNationalHoliday);
            currentCalculation.setWeeklyRest(isWeeklyRest);
            currentCalculation.setAllowanceStatus("Calculated");
            currentCalculation.setLeaveStatus(leaveStatus);
            currentCalculation.setHolidayAllowancePaid(holidayAllowancePaid);

            displayResults();

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void displayResults() {
        if (currentCalculation == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("📅 Date: ").append(currentCalculation.getDate()).append("\n");
        sb.append("⏱ Duty Time: ").append(currentCalculation.getDutyFrom()).append(" - ").append(currentCalculation.getDutyTo()).append("\n");

        if (currentCalculation.isWeeklyRest() || currentCalculation.isNationalHoliday() || currentCalculation.getLeaveStatus().contains("On Leave")) {
            sb.append("Status: ").append(currentCalculation.getLeaveStatus()).append("\n");
            if (currentCalculation.isHolidayAllowancePaid()) {
                sb.append("Allowance: Holiday allowance applicable\n");
            } else {
                sb.append("Allowance: No allowance\n");
            }
        } else {
            sb.append("Total Night Hours: ").append(String.format("%.2f", currentCalculation.getTotalNightHours())).append(" hrs\n");
            sb.append("Night Duty Allowance: ₹").append(decimalFormat.format(currentCalculation.getNightDutyAllowance())).append("\n");
        }

        tvResults.setText(sb.toString());
    }

    private void saveRecord() {
        if (currentCalculation == null) {
            Toast.makeText(this, "No calculation to save", Toast.LENGTH_SHORT).show();
            return;
        }

        dutyRecords.add(currentCalculation);
        SharedPreferences prefs = getSharedPreferences("DutyRecords", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("duty_records", gson.toJson(dutyRecords));
        editor.apply();

        Toast.makeText(this, "Record saved successfully", Toast.LENGTH_SHORT).show();
        currentCalculation = null;
        tvResults.setText("");
    }

    private void clearAll() {
        etDutyDate.setText("");
        etDutyFrom.setText("");
        etDutyTo.setText("");
        etBasicPay.setText("");
        etDearnessAllowance.setText("0");
        cbNationalHoliday.setChecked(false);
        cbWeeklyRest.setChecked(false);
        tvResults.setText("");
        currentCalculation = null;
    }
    private void exportToPDF() {
        try {
            android.graphics.pdf.PdfDocument pdfDocument = new android.graphics.pdf.PdfDocument();
            android.graphics.pdf.PdfDocument.PageInfo pageInfo = new android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create();
            android.graphics.pdf.PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            android.graphics.Canvas canvas = page.getCanvas();

            android.graphics.Paint titlePaint = new android.graphics.Paint();
            titlePaint.setColor(Color.BLACK);
            titlePaint.setTextSize(16);
            titlePaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

            android.graphics.Paint paint = new android.graphics.Paint();
            paint.setColor(Color.BLACK);
            paint.setTextSize(12);

            android.graphics.Paint headerPaint = new android.graphics.Paint();
            headerPaint.setColor(Color.BLACK);
            headerPaint.setTextSize(14);
            headerPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

            int y = 50;
            int leftMargin = 50;
            int lineHeight = 20;

            canvas.drawText("NIGHT DUTY ALLOWANCE REPORT", leftMargin, y, titlePaint);
            y += lineHeight * 2;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            canvas.drawText("Generated on: " + sdf.format(new Date()), leftMargin, y, paint);
            y += lineHeight * 2;

            canvas.drawText("DUTY RECORDS:", leftMargin, y, headerPaint);
            y += lineHeight;

            double totalNightHours = 0.0;
            double totalDutyHours = 0.0;
            double totalAllowance = 0.0;
            int recordCount = 0;

            for (DutyRecord record : dutyRecords) {
                recordCount++;
                canvas.drawText("Record " + recordCount + ":", leftMargin, y, paint);
                y += lineHeight;
                canvas.drawText("Date: " + record.getDate(), leftMargin + 20, y, paint);
                y += lineHeight;
                canvas.drawText("Duty Time: " + record.getDutyFrom() + " - " + record.getDutyTo(), leftMargin + 20, y, paint);
                y += lineHeight;

                if (record.isWeeklyRest() || record.isNationalHoliday() || record.getLeaveStatus().contains("On Leave")) {
                    canvas.drawText("Status: Holiday / Rest / Leave", leftMargin + 20, y, paint);
                    y += lineHeight;
                    if (record.isHolidayAllowancePaid())
                        canvas.drawText("Allowance: Paid", leftMargin + 20, y, paint);
                    else
                        canvas.drawText("Allowance: Not applicable", leftMargin + 20, y, paint);
                } else {
                    canvas.drawText("Total Night Hours: " + String.format("%.2f", record.getTotalNightHours()) + " hrs", leftMargin + 20, y, paint);
                    y += lineHeight;
                    canvas.drawText("Night Duty Allowance: ₹" + decimalFormat.format(record.getNightDutyAllowance()), leftMargin + 20, y, paint);

                    totalNightHours += record.getTotalNightHours();
                    totalDutyHours += record.getTotalDutyHours();
                    totalAllowance += record.getNightDutyAllowance();
                }

                y += lineHeight;

                if (y > 750) {
                    pdfDocument.finishPage(page);
                    pageInfo = new android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, recordCount / 10 + 2).create();
                    page = pdfDocument.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 50;
                }
            }

            y += lineHeight;
            canvas.drawText("SUMMARY:", leftMargin, y, headerPaint);
            y += lineHeight;
            canvas.drawText("Total Night Duty Hours: " + String.format("%.2f", totalNightHours) + " hrs", leftMargin, y, paint);
            y += lineHeight;
            canvas.drawText("Total Night Duty Allowance: ₹" + decimalFormat.format(totalAllowance), leftMargin, y, paint);
            y += lineHeight;
            canvas.drawText("Total Records: " + recordCount, leftMargin, y, paint);

            pdfDocument.finishPage(page);

            File pdfFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "night_duty_report.pdf");
            pdfDocument.writeTo(new FileOutputStream(pdfFile));
            pdfDocument.close();

            Uri pdfUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", pdfFile);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Night Duty Allowance Report");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Please find attached the night duty allowance report.");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Share PDF Report"));
            Toast.makeText(this, "PDF exported successfully", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Toast.makeText(this, "Error exporting PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
