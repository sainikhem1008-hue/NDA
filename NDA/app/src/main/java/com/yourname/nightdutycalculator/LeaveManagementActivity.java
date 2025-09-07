package com.yourname.nightdutycalculator;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaveManagementActivity extends AppCompatActivity {

    private EditText etLeaveDate, etLeaveType, etLeaveReason;
    private Button btnSaveLeave, btnExportPDF;
    private DatabaseHelper db;

    private static final int STORAGE_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave_management);

        etLeaveDate = findViewById(R.id.etLeaveDate);
        etLeaveType = findViewById(R.id.etLeaveType);
        etLeaveReason = findViewById(R.id.etLeaveReason);
        btnSaveLeave = findViewById(R.id.btnSaveLeave);
        btnExportPDF = findViewById(R.id.btnExportPDF);

        db = new DatabaseHelper(this);

        btnSaveLeave.setOnClickListener(v -> saveLeave());

        btnExportPDF.setOnClickListener(v -> {
            if (checkPermission()) {
                exportLeaveAndDutyPDF();
            } else {
                requestPermission();
            }
        });
    }

    private void saveLeave() {
        String date = etLeaveDate.getText().toString();
        String type = etLeaveType.getText().toString();
        String reason = etLeaveReason.getText().toString();

        if (date.isEmpty() || type.isEmpty()) {
            Toast.makeText(this, "Please enter date and type", Toast.LENGTH_SHORT).show();
            return;
        }

        db.addLeave(new LeaveRecord(date, type, reason));
        Toast.makeText(this, "Leave Saved", Toast.LENGTH_SHORT).show();
        etLeaveDate.setText("");
        etLeaveType.setText("");
        etLeaveReason.setText("");
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    private void exportLeaveAndDutyPDF() {
        Document document = new Document();

        try {
            String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    + "/LeaveAndDutyReport.pdf";
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // Night Duty Report Section
            document.add(new Paragraph("Night Duty Report\n\n"));

            List<DutyRecord> dutyRecords = db.getAllDutyRecords();
            int totalNightHours = 0;
            for (DutyRecord record : dutyRecords) {
                document.add(new Paragraph("Date: " + record.getDate() +
                        " | Start: " + record.getStartTime() +
                        " | End: " + record.getEndTime() +
                        " | Night Hours: " + record.getNightHours()));
                totalNightHours += record.getNightHours();
            }
            document.add(new Paragraph("\nTotal Night Duty Hours: " + totalNightHours));
            document.add(new Paragraph("\n----------------------------------------\n"));

            // Leave & Duty Report Section
            document.add(new Paragraph("Leave & Duty Report\n\n"));

            List<LeaveRecord> leaveRecords = db.getAllLeaves();
            Map<String, Integer> leaveSummary = new HashMap<>();

            for (LeaveRecord leave : leaveRecords) {
                document.add(new Paragraph("Date: " + leave.getDate() +
                        " | Type: " + leave.getType() +
                        " | Reason: " + leave.getReason()));
                int count = leaveSummary.getOrDefault(leave.getType(), 0);
                leaveSummary.put(leave.getType(), count + 1);
            }

            document.add(new Paragraph("\nLeave Summary:\n"));
            for (Map.Entry<String, Integer> entry : leaveSummary.entrySet()) {
                document.add(new Paragraph(entry.getValue() + " days of " + entry.getKey()));
            }

            document.close();
            Toast.makeText(this, "PDF Exported to Downloads", Toast.LENGTH_LONG).show();

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error exporting PDF", Toast.LENGTH_LONG).show();
        }
    }
}
