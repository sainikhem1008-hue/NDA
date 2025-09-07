package com.yourname.nightdutycalculator;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.TextAlignment;

import java.io.File;
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
    private MaterialButton btnApplyLeave, btnExportPDF;
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
        btnExportPDF = findViewById(R.id.btnExportPDF);
        rvLeaveRecords = findViewById(R.id.rvLeaveRecords);

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
        btnExportPDF.setOnClickListener(v -> exportToPdf());
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

        new AlertDialog.Builder(this)
                .setTitle("Select Type of Leave")
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

    // ==============================
    // PDF Export (with Summary)
    // ==============================
    private void exportToPdf() {
        try {
            File pdfFile = new File(getExternalFilesDir(null), "NDA_Report.pdf");
            PdfWriter writer = new PdfWriter(pdfFile);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            // Title
            document.add(new Paragraph("Night Duty Allowance Report")
                    .setBold().setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER));

            // ================= Duty Records =================
            SharedPreferences dutyPrefs = getSharedPreferences("DutyRecords", Context.MODE_PRIVATE);
            String dutyJson = dutyPrefs.getString("duty_records", "[]");
            Type dutyType = new TypeToken<List<DutyRecord>>(){}.getType();
            List<DutyRecord> dutyRecords = gson.fromJson(dutyJson, dutyType);

            double totalDutyHours = 0;
            double totalNightHours = 0;

            if (dutyRecords != null && !dutyRecords.isEmpty()) {
                document.add(new Paragraph("\nDuty Records:\n").setBold());

                for (DutyRecord record : dutyRecords) {
                    totalDutyHours += record.getTotalHours();
                    totalNightHours += record.getNightHours();

                    String dutyDetails = "Date: " + record.getDate() +
                            "\nStart: " + record.getStartTime() +
                            "  End: " + record.getEndTime() +
                            "\nTotal Hours: " + record.getTotalHours() +
                            "\nNight Duty Hours: " + record.getNightHours() +
                            "\n----------------------";
                    document.add(new Paragraph(dutyDetails));
                }
            } else {
                document.add(new Paragraph("\nNo Duty Records Found.\n"));
            }

            // ================= Leave Records =================
            int totalLeaves = 0;
            if (leaveRecords != null && !leaveRecords.isEmpty()) {
                document.add(new Paragraph("\nLeave Records:\n").setBold());

                for (LeaveRecord record : leaveRecords) {
                    totalLeaves++;
                    String leaveDetails = "From: " + record.getLeaveFrom() +
                            "  To: " + record.getLeaveTo() +
                            "\nType: " + record.getLeaveType() +
                            "\nNotes: " + record.getNotes() +
                            "\nApplied On: " + record.getAppliedDate() +
                            "\nStatus: " + record.getStatus() +
                            "\n----------------------";
                    document.add(new Paragraph(leaveDetails));
                }
            } else {
                document.add(new Paragraph("\nNo Leave Records Found.\n"));
            }

            // ================= Summary =================
            document.add(new Paragraph("\nSummary:\n").setBold());
            document.add(new Paragraph("Total Duty Hours: " + totalDutyHours));
            document.add(new Paragraph("Total Night Duty Hours: " + totalNightHours));
            document.add(new Paragraph("Total Leaves Taken: " + totalLeaves));

            document.close();

            Uri pdfUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", pdfFile);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, pdfUri);
            startActivity(Intent.createChooser(intent, "Share PDF"));

            Toast.makeText(this, "PDF Exported with Summary!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error exporting PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
