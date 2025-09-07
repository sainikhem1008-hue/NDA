package com.yourname.nightdutycalculator;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LeaveManagementActivity extends AppCompatActivity implements LeaveRecordsAdapter.OnLeaveDeleteListener {

    private TextInputEditText etLeaveFromDate, etLeaveToDate, etLeaveTypeSelection, etLeaveNotes;
    private MaterialButton btnApplyLeave;
    private MaterialButton btnExportPDF; // optional - only if layout provides it
    private RecyclerView rvLeaveRecords;
    private List<LeaveRecord> leaveRecords = new ArrayList<>();
    private LeaveRecordsAdapter adapter;
    private SharedPreferences sharedPreferences;
    private Gson gson = new Gson();
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave_management);

        initViews();
        setupListeners();
        loadLeaveRecords();
    }

    private void initViews() {
        etLeaveFromDate = findViewById(R.id.etLeaveFromDate);
        etLeaveToDate = findViewById(R.id.etLeaveToDate);
        etLeaveTypeSelection = findViewById(R.id.etLeaveTypeSelection);
        etLeaveNotes = findViewById(R.id.etLeaveNotes);
        btnApplyLeave = findViewById(R.id.btnApplyLeave);
        rvLeaveRecords = findViewById(R.id.rvLeaveRecords);

        // optional export button - safe if not present in layout
        View maybeExport = findViewById(R.id.btnExportPDF);
        if (maybeExport instanceof MaterialButton) {
            btnExportPDF = (MaterialButton) maybeExport;
        } else {
            btnExportPDF = null;
        }

        sharedPreferences = getSharedPreferences("LeaveRecords", Context.MODE_PRIVATE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        adapter = new LeaveRecordsAdapter(leaveRecords, this);
        rvLeaveRecords.setLayoutManager(new LinearLayoutManager(this));
        rvLeaveRecords.setAdapter(adapter);
    }

    private void setupListeners() {
        etLeaveFromDate.setOnClickListener(v -> showDatePicker(etLeaveFromDate));
        etLeaveToDate.setOnClickListener(v -> showDatePicker(etLeaveToDate));
        etLeaveTypeSelection.setOnClickListener(v -> showLeaveTypeDialog());
        btnApplyLeave.setOnClickListener(v -> {
            vibrate();
            applyLeave();
        });

        if (btnExportPDF != null) {
            btnExportPDF.setOnClickListener(v -> exportLeaveAndDutyPDF());
        }
    }

    private void showDatePicker(TextInputEditText editText) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            editText.setText(sdf.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showLeaveTypeDialog() {
        String[] leaveTypes = {
            getString(R.string.casual_leave),
            getString(R.string.sick_leave),
            getString(R.string.earned_leave),
            getString(R.string.compensatory_off),
            getString(R.string.other_leave)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Type of Leave")
               .setItems(leaveTypes, (dialog, which) -> etLeaveTypeSelection.setText(leaveTypes[which]))
               .setNegativeButton("Cancel", null)
               .show();
    }

    private void applyLeave() {
        String leaveFrom = etLeaveFromDate.getText().toString();
        String leaveTo = etLeaveToDate.getText().toString();
        String leaveType = etLeaveTypeSelection.getText().toString();
        String notes = etLeaveNotes.getText().toString();

        if (leaveFrom.isEmpty() || leaveTo.isEmpty() || leaveType.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        LeaveRecord leaveRecord = new LeaveRecord();
        leaveRecord.setLeaveFrom(leaveFrom);
        leaveRecord.setLeaveTo(leaveTo);
        leaveRecord.setLeaveType(leaveType);
        leaveRecord.setNotes(notes);
        leaveRecord.setAppliedDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

        leaveRecords.add(leaveRecord);
        Collections.sort(leaveRecords, (a, b) -> b.getAppliedDate().compareTo(a.getAppliedDate()));
        saveLeaveRecords();

        if (adapter != null) adapter.notifyDataSetChanged();

        etLeaveFromDate.setText("");
        etLeaveToDate.setText("");
        etLeaveTypeSelection.setText("");
        etLeaveNotes.setText("");

        Toast.makeText(this, "Leave application submitted successfully!", Toast.LENGTH_SHORT).show();
    }

    private void loadLeaveRecords() {
        String recordsJson = sharedPreferences.getString("leave_records", "[]");
        Type listType = new TypeToken<List<LeaveRecord>>(){}.getType();
        List<LeaveRecord> loadedRecords = gson.fromJson(recordsJson, listType);

        if (loadedRecords != null) {
            leaveRecords.clear();
            leaveRecords.addAll(loadedRecords);
            Collections.sort(leaveRecords, (a, b) -> b.getAppliedDate().compareTo(a.getAppliedDate()));
            if (adapter != null) adapter.notifyDataSetChanged();
        }
    }

    private void saveLeaveRecords() {
        String recordsJson = gson.toJson(leaveRecords);
        sharedPreferences.edit().putString("leave_records", recordsJson).apply();
    }

    @Override
    public void onLeaveDelete(LeaveRecord record, int position) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Leave Application")
            .setMessage("Are you sure you want to delete this leave application?")
            .setPositiveButton("Yes", (dialog, which) -> {
                leaveRecords.remove(position);
                saveLeaveRecords();
                if (adapter != null) adapter.notifyItemRemoved(position);
                Toast.makeText(this, "Leave application deleted", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("No", null)
            .show();
    }

    private void vibrate() {
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(50);
        }
    }

    // ---------- Export leave + duty PDF ----------
    // ---------- Export leave + duty PDF ----------
    private void exportLeaveAndDutyPDF() {
    // load duty records from shared prefs the same way main activity stores them
    SharedPreferences dutyPrefs = getSharedPreferences("DutyRecords", Context.MODE_PRIVATE);
    String dutyJson = dutyPrefs.getString("duty_records", "[]");
    Type dutyListType = new TypeToken<List<DutyRecord>>(){}.getType();
    List<DutyRecord> dutyList = gson.fromJson(dutyJson, dutyListType);
    if (dutyList == null) dutyList = new ArrayList<>();

    if ((dutyList.isEmpty()) && (leaveRecords.isEmpty())) {
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
    canvas.drawText("LEAVE & DUTY REPORT", x, y, paint);
    y += 22;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    canvas.drawText("Generated: " + sdf.format(new Date()), x, y, paint);
    y += 26;

    // Duty section
    if (!dutyList.isEmpty()) {
        canvas.drawText("---- Duty Records ----", x, y, paint);
        y += 18;
        canvas.drawText("Date", x, y, paint);
        canvas.drawText("Duty", x + 100, y, paint);
        canvas.drawText("TotalHrs", x + 260, y, paint);
        canvas.drawText("NightHrs", x + 330, y, paint);
        canvas.drawText("NDA", x + 410, y, paint);
        y += 16;

        for (DutyRecord r : dutyList) {
            if (y > 760) {
                pdfDoc.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pdfDoc.getPages().size() + 1).create();
                page = pdfDoc.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 40;
            }
            canvas.drawText(r.getDate(), x, y, paint);
            canvas.drawText((r.getDutyFrom() == null ? "-" : r.getDutyFrom()) + " - " + (r.getDutyTo() == null ? "-" : r.getDutyTo()), x + 100, y, paint);
            canvas.drawText(String.format(Locale.getDefault(), "%.2f", r.getTotalDutyHours()), x + 260, y, paint);
            canvas.drawText(String.format(Locale.getDefault(), "%.2f", r.getTotalNightHours()), x + 330, y, paint);
            canvas.drawText("₹" + (r.getNightDutyAllowance() == 0.0 ? "0.00" : String.format(Locale.getDefault(), "%.2f", r.getNightDutyAllowance())), x + 410, y, paint);
            y += 16;
        }
        y += 12;
    }

    // Leave section
    if (!leaveRecords.isEmpty()) {
        if (y > 700) {
            pdfDoc.finishPage(page);
            pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pdfDoc.getPages().size() + 1).create();
            page = pdfDoc.startPage(pageInfo);
            canvas = page.getCanvas();
            y = 40;
        }

        canvas.drawText("---- Leave Records ----", x, y, paint);
        y += 18;
        canvas.drawText("From - To", x, y, paint);
        canvas.drawText("Type", x + 160, y, paint);
        canvas.drawText("Status", x + 300, y, paint);
        canvas.drawText("Notes", x + 380, y, paint);
        y += 16;

        // Count leaves by type
        java.util.Map<String, Integer> leaveTypeDays = new java.util.HashMap<>();
        SimpleDateFormat sdfDay = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (LeaveRecord lr : leaveRecords) {
            try {
                Date start = sdfDay.parse(lr.getLeaveFrom());
                Date end = sdfDay.parse(lr.getLeaveTo());
                long diff = end.getTime() - start.getTime();
                int days = (int)(diff / (1000 * 60 * 60 * 24)) + 1; // include end day
                leaveTypeDays.put(lr.getLeaveType(), leaveTypeDays.getOrDefault(lr.getLeaveType(), 0) + days);
            } catch (Exception e) {
                e.printStackTrace();
            }

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
        }
        y += 12;

        // Display total leaves by type
        canvas.drawText("Total Leaves:", x, y, paint);
        y += 16;
        for (String type : leaveTypeDays.keySet()) {
            canvas.drawText(leaveTypeDays.get(type) + " days " + type, x, y, paint);
            y += 16;
        }
    }

    pdfDoc.finishPage(page);

    try {
        File file = new File(getExternalFilesDir(null), "leave_and_duty_report.pdf");
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
}
