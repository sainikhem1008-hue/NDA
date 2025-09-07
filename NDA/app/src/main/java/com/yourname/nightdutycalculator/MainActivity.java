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
        loadLeaveRecords(); // ✅ Ensure leave records loaded

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

    // ✅ New method: load leave records safely
    private void loadLeaveRecords() {
        SharedPreferences prefs = getSharedPreferences("LeaveRecords", Context.MODE_PRIVATE);
        String json = prefs.getString("leave_records", "[]");
        Type t = new TypeToken<List<LeaveRecord>>(){}.getType();
        leaveRecords = gson.fromJson(json, t);
        if (leaveRecords == null) leaveRecords = new ArrayList<>();
    }

// part 2 start 
    private void calculateAndDisplay() {
        try {
            double basicPay = Double.parseDouble(etBasicPay.getText().toString());
            double da = Double.parseDouble(etDearnessAllowance.getText().toString());
            double ceiling = Double.parseDouble(etCeilingLimit.getText().toString());

            double hourlyRate = (basicPay + da) / 200;

            String from = etDutyFrom.getText().toString();
            String to = etDutyTo.getText().toString();
            double nightHours = calculateNightHours(from, to);

            double nda = (nightHours / 6) * hourlyRate;
            if (nda > ceiling) nda = ceiling;

            currentCalculation = new DutyRecord(
                    etDutyDate.getText().toString(),
                    from,
                    to,
                    nightHours,
                    nda,
                    cbNationalHoliday.isChecked(),
                    cbWeeklyRest.isChecked(),
                    cbDualDuty.isChecked(),
                    cbDutyOnWeeklyRest.isChecked()
            );

            StringBuilder sb = new StringBuilder();
            sb.append("Date: ").append(currentCalculation.getDate()).append("\n");
            sb.append("Duty: ").append(currentCalculation.getDutyFrom())
                    .append(" - ").append(currentCalculation.getDutyTo()).append("\n");
            sb.append("Night Hours: ").append(String.format(Locale.getDefault(), "%.2f", nightHours)).append("\n");
            sb.append("NDA: ₹").append(decimalFormat.format(nda)).append("\n");

            tvResults.setText(sb.toString());
            llResults.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
        }
    }

    private double calculateNightHours(String from, String to) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date fromDate = sdf.parse(from);
            Date toDate = sdf.parse(to);

            Calendar c1 = Calendar.getInstance();
            Calendar c2 = Calendar.getInstance();

            c1.setTime(fromDate);
            c2.setTime(toDate);
            if (c2.before(c1)) c2.add(Calendar.DAY_OF_MONTH, 1);

            long diffMillis = c2.getTimeInMillis() - c1.getTimeInMillis();
            return diffMillis / (1000.0 * 60 * 60);

        } catch (Exception e) {
            return 0;
        }
    }

    private void saveRecord() {
        if (currentCalculation != null) {
            dutyRecords.add(currentCalculation);
            persistDutyRecords();
            Toast.makeText(this, "Record saved", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Calculate first", Toast.LENGTH_SHORT).show();
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

        // ----- DUTY RECORDS -----
        if (dutyRecords != null && !dutyRecords.isEmpty()) {
            canvas.drawText("Duty Records:", x, y, paint);
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

        // ----- LEAVE RECORDS -----
        if (leaveRecords != null && !leaveRecords.isEmpty()) {
            canvas.drawText("Leave Records:", x, y, paint);
            y += 20;

            canvas.drawText("From", x, y, paint);
            canvas.drawText("To", x + 90, y, paint);
            canvas.drawText("Type", x + 180, y, paint);
            canvas.drawText("Status", x + 300, y, paint);
            canvas.drawText("Notes", x + 420, y, paint);
            y += 18;

            for (LeaveRecord lr : leaveRecords) {
                canvas.drawText(lr.getLeaveFrom(), x, y, paint);
                canvas.drawText(lr.getLeaveTo(), x + 90, y, paint);
                canvas.drawText(lr.getLeaveType(), x + 180, y, paint);
                canvas.drawText(lr.getStatus(), x + 300, y, paint);
                canvas.drawText(lr.getNotes() != null ? lr.getNotes() : "-", x + 420, y, paint);
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
    
