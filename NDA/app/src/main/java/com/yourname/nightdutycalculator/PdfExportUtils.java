package com.yourname.nightdutycalculator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class PdfExportUtils {

    public static void exportLeaveAndDutyPDF(Context context) {
        Gson gson = new Gson();

        // Load duty records
        SharedPreferences dutyPrefs = context.getSharedPreferences("DutyRecords", Context.MODE_PRIVATE);
        String dutyJson = dutyPrefs.getString("duty_records", "[]");
        Type dutyListType = new TypeToken<List<DutyRecord>>() {}.getType();
        List<DutyRecord> dutyList = gson.fromJson(dutyJson, dutyListType);
        if (dutyList == null) dutyList = new ArrayList<>();

        // Load leave records
        SharedPreferences leavePrefs = context.getSharedPreferences("LeaveRecords", Context.MODE_PRIVATE);
        String leaveJson = leavePrefs.getString("leave_records", "[]");
        Type leaveListType = new TypeToken<List<LeaveRecord>>() {}.getType();
        List<LeaveRecord> leaveRecords = gson.fromJson(leaveJson, leaveListType);
        if (leaveRecords == null) leaveRecords = new ArrayList<>();

        if (dutyList.isEmpty() && leaveRecords.isEmpty()) {
            Toast.makeText(context, "No records to export", Toast.LENGTH_SHORT).show();
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
        canvas.drawText("NIGHT DUTY & LEAVE REPORT", x, y, paint);
        y += 22;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        canvas.drawText("Generated: " + sdf.format(new Date()), x, y, paint);
        y += 26;

        // ---------- Duty Records ----------
        if (!dutyList.isEmpty()) {
            canvas.drawText("---- Night Duty Records ----", x, y, paint);
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
                canvas.drawText((r.getDutyFrom() == null ? "-" : r.getDutyFrom()) + " - " +
                                (r.getDutyTo() == null ? "-" : r.getDutyTo()), x + 100, y, paint);
                canvas.drawText(String.format(Locale.getDefault(), "%.2f", r.getTotalDutyHours()), x + 260, y, paint);
                canvas.drawText(String.format(Locale.getDefault(), "%.2f", r.getTotalNightHours()), x + 330, y, paint);
                canvas.drawText("₹" + (r.getNightDutyAllowance() == 0.0 ? "0.00" :
                                String.format(Locale.getDefault(), "%.2f", r.getNightDutyAllowance())), x + 410, y, paint);
                y += 16;
            }
            y += 12;
        }

        // ---------- Leave Records ----------
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
            Map<String, Integer> leaveTypeDays = new HashMap<>();
            SimpleDateFormat sdfDay = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            for (LeaveRecord lr : leaveRecords) {
                try {
                    Date start = sdfDay.parse(lr.getLeaveFrom());
                    Date end = sdfDay.parse(lr.getLeaveTo());
                    int days = (int) ((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24)) + 1;
                    leaveTypeDays.put(lr.getLeaveType(),
                            leaveTypeDays.getOrDefault(lr.getLeaveType(), 0) + days);
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

            // Display total leaves grouped by type
            canvas.drawText("Total Leaves by Type:", x, y, paint);
            y += 16;
            for (String type : leaveTypeDays.keySet()) {
                canvas.drawText(leaveTypeDays.get(type) + " days of " + type, x, y, paint);
                y += 16;
            }
        }

        pdfDoc.finishPage(page);

        try {
            File file = new File(context.getExternalFilesDir(null), "night_duty_and_leave_report.pdf");
            FileOutputStream fos = new FileOutputStream(file);
            pdfDoc.writeTo(fos);
            pdfDoc.close();
            fos.close();

            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("application/pdf");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(share, "Share PDF"));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "PDF export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
          }
