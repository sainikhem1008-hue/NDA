package com.khem.nightdutycalculator;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText editTextBasicPay, editTextDA;
    private TextView textViewResult;
    private Button buttonSave, buttonView, buttonExport, buttonLeave, buttonQuickMorning, buttonQuickEvening, buttonQuickNight;
    private Calendar selectedDate;
    private int startHour, startMinute, endHour, endMinute;
    private CheckBox checkBoxDualDuty, checkBoxWeeklyRest;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        // UI setup
        editTextBasicPay = findViewById(R.id.editTextBasicPay);
        editTextDA = findViewById(R.id.editTextDA);
        textViewResult = findViewById(R.id.textViewResult);

        buttonSave = findViewById(R.id.buttonSave);
        buttonView = findViewById(R.id.buttonView);
        buttonExport = findViewById(R.id.buttonExport);
        buttonLeave = findViewById(R.id.buttonLeave);

        buttonQuickMorning = findViewById(R.id.buttonQuickMorning);
        buttonQuickEvening = findViewById(R.id.buttonQuickEvening);
        buttonQuickNight = findViewById(R.id.buttonQuickNight);

        checkBoxDualDuty = findViewById(R.id.checkBoxDualDuty);
        checkBoxWeeklyRest = findViewById(R.id.checkBoxWeeklyRest);

        selectedDate = Calendar.getInstance();

        // Calendar for selecting duty date
        CalendarView calendarView = findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                selectedDate.set(year, month, dayOfMonth);
            }
        });
        // Select duty start time
        Button buttonStartTime = findViewById(R.id.buttonStartTime);
        buttonStartTime.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(MainActivity.this,
                    (TimePicker view, int hourOfDay, int minute) -> {
                        startHour = hourOfDay;
                        startMinute = minute;
                        Toast.makeText(MainActivity.this, "Start: " + hourOfDay + ":" + minute, Toast.LENGTH_SHORT).show();
                    }, 0, 0, true);
            timePicker.show();
        });

        // Select duty end time
        Button buttonEndTime = findViewById(R.id.buttonEndTime);
        buttonEndTime.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(MainActivity.this,
                    (TimePicker view, int hourOfDay, int minute) -> {
                        endHour = hourOfDay;
                        endMinute = minute;
                        Toast.makeText(MainActivity.this, "End: " + hourOfDay + ":" + minute, Toast.LENGTH_SHORT).show();
                    }, 0, 0, true);
            timePicker.show();
        });

        // Quick Duty Buttons
        buttonQuickMorning.setOnClickListener(v -> {
            startHour = 8; startMinute = 0;
            endHour = 16; endMinute = 0;
            Toast.makeText(MainActivity.this, "Morning Duty (08:00–16:00)", Toast.LENGTH_SHORT).show();
        });

        buttonQuickEvening.setOnClickListener(v -> {
            startHour = 16; startMinute = 0;
            endHour = 0; endMinute = 0;
            Toast.makeText(MainActivity.this, "Evening Duty (16:00–00:00)", Toast.LENGTH_SHORT).show();
        });

        buttonQuickNight.setOnClickListener(v -> {
            startHour = 0; startMinute = 0;
            endHour = 8; endMinute = 0;
            Toast.makeText(MainActivity.this, "Night Duty (00:00–08:00)", Toast.LENGTH_SHORT).show();
        });

        // Save Record
        buttonSave.setOnClickListener(v -> saveRecord());

        // View Records
        buttonView.setOnClickListener(v -> viewRecords());

        // Export PDF
        buttonExport.setOnClickListener(v -> exportToPDF());

        // Leave Management
        buttonLeave.setOnClickListener(v -> manageLeave());
    }

    private void saveRecord() {
    

    // ✅ Save to SQLite Database
    boolean inserted = dbHelper.insertDuty(date, start, end, basicPay, da, nda, dutyType);

    if (inserted) {
        Toast.makeText(this, "Record saved to database!", Toast.LENGTH_SHORT).show();
    } else {
        Toast.makeText(this, "Failed to save record!", Toast.LENGTH_SHORT).show();
    }

    // ✅ Also keep in memory (for export/records dialog)
    dutyRecords.add(currentCalculation);
    persistDutyRecords();
    }

        double basicPay = Double.parseDouble(basicPayStr);
        double da = Double.parseDouble(daStr);

        // Total duty hours
        int dutyHours = calculateDutyHours(startprivate void saveRecord() {
    if (currentCalculation == null) {
        Toast.makeText(this, "Please calculate before saving!", Toast.LENGTH_SHORT).show();
        return;
    }

    String date = etDutyDate.getText().toString();
    String start = etDutyFrom.getText().toString();
    String end = etDutyTo.getText().toString();

    double basicPay = 0.0;
    double da = 0.0;
    try {
        basicPay = Double.parseDouble(etBasicPay.getText().toString());
    } catch (NumberFormatException ignored) {}
    try {
        da = Double.parseDouble(etDearnessAllowance.getText().toString());
    } catch (NumberFormatException ignored) {}

    double nda = currentCalculation.getNdaAmount();

    String dutyType = "";
    if (cbDualDuty.isChecked()) {
        dutyType = "Dual Duty (CR Pending)";
    } else if (cbWeeklyRestDuty.isChecked()) {
        dutyType = "Weekly Rest Duty (CR Pending)";
    } else if (cbNationalHoliday.isChecked()) {
        dutyType = "National Holiday Duty";
    } else {
        dutyType = "Normal Duty";
    }

    // ✅ Save to SQLite Database
    boolean inserted = dbHelper.insertDuty(date, start, end, basicPay, da, nda, dutyType);

    if (inserted) {
        Toast.makeText(this, "Record saved to database!", Toast.LENGTH_SHORT).show();
    } else {
        Toast.makeText(this, "Failed to save record!", Toast.LENGTH_SHORT).show();
    }

    // ✅ Also keep in memory (for export/records dialog)
    dutyRecords.add(currentCalculation);
    persistDutyRecords();
        }Hour, startMinute, endHour, endMinute);

        // Night duty hours
        int nightDutyHours = calculateNightDutyHours(startHour, startMinute, endHour, endMinute);

        // NDA Hourly Rate
        double hourlyRate = (basicPay + da) / 200.0;

        // NDA Calculation
        double nda = hourlyRate * (nightDutyHours / 6);

        // Dual Duty / Weekly Rest flags
        String remarks = "";
        if (checkBoxDualDuty.isChecked()) {
            remarks += "Dual Duty – CR Pending. ";
            if (nightDutyHours > 0) {
                remarks += "NDA also applicable. ";
            }
        }
        if (checkBoxWeeklyRest.isChecked()) {
            remarks += "Duty on Weekly Rest – CR Pending. ";
            if (nightDutyHours > 0) {
                remarks += "NDA also applicable. ";
            }
        }

        // Save to database
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("date", new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(selectedDate.getTime()));
        values.put("start_time", startHour + ":" + String.format("%02d", startMinute));
        values.put("end_time", endHour + ":" + String.format("%02d", endMinute));
        values.put("duty_hours", dutyHours);
        values.put("night_hours", nightDutyHours);
        values.put("nda_amount", nda);
        values.put("remarks", remarks);

        db.insert("duty_records", null, values);
        db.close();

        textViewResult.setText("Saved!\nDuty Hours: " + dutyHours +
                "\nNight Duty Hours: " + nightDutyHours +
                "\nNDA: ₹" + String.format("%.2f", nda) +
                "\n" + remarks);
    }
    // Calculate total duty hours
    private int calculateDutyHours(int sh, int sm, int eh, int em) {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, sh);
        start.set(Calendar.MINUTE, sm);

        Calendar end = Calendar.getInstance();
        end.set(Calendar.HOUR_OF_DAY, eh);
        end.set(Calendar.MINUTE, em);

        if (end.before(start)) {
            end.add(Calendar.DATE, 1); // cross midnight
        }

        long diffMillis = end.getTimeInMillis() - start.getTimeInMillis();
        return (int) (diffMillis / (1000 * 60 * 60));
    }

    // Calculate night duty hours (22:00–06:00 window)
    private int calculateNightDutyHours(int sh, int sm, int eh, int em) {
        Calendar dutyStart = Calendar.getInstance();
        dutyStart.set(Calendar.HOUR_OF_DAY, sh);
        dutyStart.set(Calendar.MINUTE, sm);

        Calendar dutyEnd = Calendar.getInstance();
        dutyEnd.set(Calendar.HOUR_OF_DAY, eh);
        dutyEnd.set(Calendar.MINUTE, em);
        if (dutyEnd.before(dutyStart)) {
            dutyEnd.add(Calendar.DATE, 1);
        }

        // Night period 22:00 → 06:00
        Calendar nightStart = Calendar.getInstance();
        nightStart.set(Calendar.HOUR_OF_DAY, 22);
        nightStart.set(Calendar.MINUTE, 0);

        Calendar nightEnd = Calendar.getInstance();
        nightEnd.add(Calendar.DATE, 1);
        nightEnd.set(Calendar.HOUR_OF_DAY, 6);
        nightEnd.set(Calendar.MINUTE, 0);

        long overlap = Math.max(0,
                Math.min(dutyEnd.getTimeInMillis(), nightEnd.getTimeInMillis()) -
                Math.max(dutyStart.getTimeInMillis(), nightStart.getTimeInMillis()));

        return (int) (overlap / (1000 * 60 * 60));
    }

    // View Records
    private void viewRecords() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM duty_records ORDER BY id DESC", null);

        StringBuilder sb = new StringBuilder();
        while (cursor.moveToNext()) {
            sb.append("Date: ").append(cursor.getString(cursor.getColumnIndex("date")))
              .append("\nDuty: ").append(cursor.getString(cursor.getColumnIndex("start_time")))
              .append(" - ").append(cursor.getString(cursor.getColumnIndex("end_time")))
              .append("\nDuty Hours: ").append(cursor.getInt(cursor.getColumnIndex("duty_hours")))
              .append(" | Night: ").append(cursor.getInt(cursor.getColumnIndex("night_hours")))
              .append("\nNDA: ₹").append(cursor.getDouble(cursor.getColumnIndex("nda_amount")))
              .append("\nRemarks: ").append(cursor.getString(cursor.getColumnIndex("remarks")))
              .append("\n\n");
        }
        cursor.close();
        db.close();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Duty Records");
        builder.setMessage(sb.toString().isEmpty() ? "No records found." : sb.toString());
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    // Export PDF
    private void exportToPDF() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM duty_records ORDER BY id", null);

        PdfDocument pdf = new PdfDocument();
        Paint paint = new Paint();

        int pageNumber = 1;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
        PdfDocument.Page page = pdf.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        int y = 50;

        paint.setTextSize(14);
        paint.setFakeBoldText(true);
        canvas.drawText("Night Duty Allowance Records", 200, y, paint);
        y += 40;

        paint.setTextSize(12);
        paint.setFakeBoldText(false);

        while (cursor.moveToNext()) {
            String record = cursor.getString(cursor.getColumnIndex("date")) + " | " +
                    cursor.getString(cursor.getColumnIndex("start_time")) + "-" +
                    cursor.getString(cursor.getColumnIndex("end_time")) +
                    " | NDA: ₹" + cursor.getDouble(cursor.getColumnIndex("nda_amount"));
            canvas.drawText(record, 50, y, paint);
            y += 20;

            if (y > 780) {
                pdf.finishPage(page);
                pageNumber++;
                pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
                page = pdf.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 50;
            }
        }
        cursor.close();
        db.close();

        pdf.finishPage(page);

        try {
            File file = new File(getExternalFilesDir(null), "NDA_Records.pdf");
            pdf.writeTo(new FileOutputStream(file));
            pdf.close();

            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);

        } catch (IOException e) {
            Toast.makeText(this, "Error exporting PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Leave Management (simple navigation stub)
    private void manageLeave() {
        Toast.makeText(this, "Leave Management coming soon!", Toast.LENGTH_SHORT).show();
    }
}
    
