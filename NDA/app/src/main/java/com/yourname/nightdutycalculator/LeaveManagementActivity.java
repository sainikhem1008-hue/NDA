package com.yourname.nightdutycalculator;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class LeaveManagementActivity extends AppCompatActivity {

    private TextInputEditText etLeaveFromDate, etLeaveToDate, etLeaveNotes;
    private Spinner spinnerLeaveTypeSelection;
    private MaterialButton btnApplyLeave;
    private RecyclerView rvLeaveRecords;
    private LeaveAdapter leaveAdapter;
    private ArrayList<LeaveModel> leaveList = new ArrayList<>();
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave_management);

        // Views
        etLeaveFromDate = findViewById(R.id.etLeaveFromDate);
        etLeaveToDate = findViewById(R.id.etLeaveToDate);
        etLeaveNotes = findViewById(R.id.etLeaveNotes);
        spinnerLeaveTypeSelection = findViewById(R.id.spinnerLeaveTypeSelection);
        btnApplyLeave = findViewById(R.id.btnApplyLeave);
        rvLeaveRecords = findViewById(R.id.rvLeaveRecords);

        dbHelper = new DBHelper(this);

        // Spinner setup
        String[] leaveTypes = {"Sick Leave", "Casual Leave", "Earned Leave"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, leaveTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLeaveTypeSelection.setAdapter(adapter);

        // RecyclerView setup
        leaveAdapter = new LeaveAdapter(leaveList);
        rvLeaveRecords.setLayoutManager(new LinearLayoutManager(this));
        rvLeaveRecords.setAdapter(leaveAdapter);

        loadLeavesFromDB();

        // Apply leave button
        btnApplyLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addLeaveEntry();
            }
        });

        // Export PDF
        findViewById(R.id.buttonExportPdf).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportReportPdf();
            }
        });
    }

    private void addLeaveEntry() {
        String fromDate = etLeaveFromDate.getText().toString().trim();
        String toDate = etLeaveToDate.getText().toString().trim();
        String type = spinnerLeaveTypeSelection.getSelectedItem().toString();
        String notes = etLeaveNotes.getText().toString().trim();

        if (fromDate.isEmpty() || toDate.isEmpty()) {
            Toast.makeText(this, "Select From and To dates", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("from_date", fromDate);
        values.put("to_date", toDate);
        values.put("leave_type", type);
        values.put("notes", notes);

        long result = db.insert("leaves", null, values);

        if (result != -1) {
            Toast.makeText(this, "Leave added", Toast.LENGTH_SHORT).show();
            leaveList.add(new LeaveModel(fromDate, toDate, type, notes));
            leaveAdapter.notifyDataSetChanged();
            etLeaveFromDate.setText("");
            etLeaveToDate.setText("");
            etLeaveNotes.setText("");
        } else {
            Toast.makeText(this, "Error saving leave", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadLeavesFromDB() {
        leaveList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM leaves", null);

        if (cursor.moveToFirst()) {
            do {
                String fromDate = cursor.getString(cursor.getColumnIndexOrThrow("from_date"));
                String toDate = cursor.getString(cursor.getColumnIndexOrThrow("to_date"));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("leave_type"));
                String notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));
                leaveList.add(new LeaveModel(fromDate, toDate, type, notes));
            } while (cursor.moveToNext());
        }
        cursor.close();
        leaveAdapter.notifyDataSetChanged();
    }

    private void exportReportPdf() {
        Document document = new Document();

        try {
            File pdfFile = new File(getExternalFilesDir(null), "Duty_Leave_Report.pdf");
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();

            document.add(new Paragraph("Duty and Leave Report\n\n"));

            // ---- Duty Records ----
            document.add(new Paragraph("Duty Records:\n"));
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor dutyCursor = db.rawQuery("SELECT * FROM duty", null);
            if (dutyCursor.moveToFirst()) {
                do {
                    String date = dutyCursor.getString(dutyCursor.getColumnIndexOrThrow("date"));
                    String start = dutyCursor.getString(dutyCursor.getColumnIndexOrThrow("start_time"));
                    String end = dutyCursor.getString(dutyCursor.getColumnIndexOrThrow("end_time"));
                    int hours = dutyCursor.getInt(dutyCursor.getColumnIndexOrThrow("hours"));
                    document.add(new Paragraph(date + " | " + start + " - " + end + " | Hours: " + hours));
                } while (dutyCursor.moveToNext());
            } else {
                document.add(new Paragraph("No duty records found."));
            }
            dutyCursor.close();

            document.add(new Paragraph("\n"));

            // ---- Leave Records ----
            document.add(new Paragraph("Leave Records:\n"));
            Cursor leaveCursor = db.rawQuery("SELECT * FROM leaves", null);
            if (leaveCursor.moveToFirst()) {
                do {
                    String fromDate = leaveCursor.getString(leaveCursor.getColumnIndexOrThrow("from_date"));
                    String toDate = leaveCursor.getString(leaveCursor.getColumnIndexOrThrow("to_date"));
                    String type = leaveCursor.getString(leaveCursor.getColumnIndexOrThrow("leave_type"));
                    String notes = leaveCursor.getString(leaveCursor.getColumnIndexOrThrow("notes"));
                    document.add(new Paragraph(type + " | From: " + fromDate + " To: " + toDate + " | Notes: " + notes));
                } while (leaveCursor.moveToNext());
            } else {
                document.add(new Paragraph("No leave records found."));
            }
            leaveCursor.close();

            document.close();
            Toast.makeText(this, "PDF saved: " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating PDF", Toast.LENGTH_SHORT).show();
        }
    }
                        }
