package com.yourname.nightdutycalculator;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class LeaveManagementActivity extends AppCompatActivity {

    private EditText editTextLeaveDays;
    private Spinner spinnerLeaveType;
    private Button buttonAddLeave, buttonExportPdf;
    private RecyclerView recyclerViewLeaves;
    private LeaveAdapter leaveAdapter;
    private ArrayList<LeaveModel> leaveList = new ArrayList<>();
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave_management);

        editTextLeaveDays = findViewById(R.id.editTextLeaveDays);
        spinnerLeaveType = findViewById(R.id.spinnerLeaveType);
        buttonAddLeave = findViewById(R.id.buttonAddLeave);
        buttonExportPdf = findViewById(R.id.buttonExportPdf);
        recyclerViewLeaves = findViewById(R.id.recyclerViewLeaves);

        dbHelper = new DBHelper(this);

        // Spinner with leave types
        String[] leaveTypes = {"Sick Leave", "Casual Leave", "Earned Leave"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, leaveTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLeaveType.setAdapter(adapter);

        // RecyclerView setup
        leaveAdapter = new LeaveAdapter(leaveList);
        recyclerViewLeaves.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewLeaves.setAdapter(leaveAdapter);

        loadLeavesFromDB();

        buttonAddLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addLeaveEntry();
            }
        });

        buttonExportPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportReportPdf();
            }
        });
    }

    private void addLeaveEntry() {
        String daysStr = editTextLeaveDays.getText().toString().trim();
        String type = spinnerLeaveType.getSelectedItem().toString();

        if (daysStr.isEmpty()) {
            Toast.makeText(this, "Enter leave days", Toast.LENGTH_SHORT).show();
            return;
        }

        int days = Integer.parseInt(daysStr);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("leave_days", days);
        values.put("leave_type", type);

        long result = db.insert("leaves", null, values);

        if (result != -1) {
            Toast.makeText(this, "Leave added", Toast.LENGTH_SHORT).show();
            leaveList.add(new LeaveModel(days, type));
            leaveAdapter.notifyDataSetChanged();
            editTextLeaveDays.setText("");
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
                int days = cursor.getInt(cursor.getColumnIndexOrThrow("leave_days"));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("leave_type"));
                leaveList.add(new LeaveModel(days, type));
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
                    int days = leaveCursor.getInt(leaveCursor.getColumnIndexOrThrow("leave_days"));
                    String type = leaveCursor.getString(leaveCursor.getColumnIndexOrThrow("leave_type"));
                    document.add(new Paragraph(type + " | Days: " + days));
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


