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
        values.put(DBHelper.COLUMN_DAYS, days);
        values.put(DBHelper.COLUMN_TYPE, type);

        long result = db.insert(DBHelper.TABLE_LEAVES, null, values);

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
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_LEAVES, null);

        if (cursor.moveToFirst()) {
            do {
                int days = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_DAYS));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_TYPE));
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
            Cursor dutyCursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_DUTY, null);
            if (dutyCursor.moveToFirst()) {
                do {
                    // For now, duty table only has ID. Expand later with real columns.
                    int id = dutyCursor.getInt(dutyCursor.getColumnIndexOrThrow("id"));
                    document.add(new Paragraph("Duty record #" + id));
                } while (dutyCursor.moveToNext());
            } else {
                document.add(new Paragraph("No duty records found."));
            }
            dutyCursor.close();

            document.add(new Paragraph("\n"));

            // ---- Leave Records ----
            document.add(new Paragraph("Leave Records:\n"));
            Cursor leaveCursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_LEAVES, null);
            if (leaveCursor.moveToFirst()) {
                do {
                    int days = leaveCursor.getInt(leaveCursor.getColumnIndexOrThrow(DBHelper.COLUMN_DAYS));
                    String type = leaveCursor.getString(leaveCursor.getColumnIndexOrThrow(DBHelper.COLUMN_TYPE));
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
