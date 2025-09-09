package com.yourname.nightdutycalculator;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LeaveManagementActivity extends AppCompatActivity {

    private Button btnExportPDF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave_management);

        btnExportPDF = findViewById(R.id.btnExportPDF);

        btnExportPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportCombinedReportPDF();
            }
        });
    }

    /**
     * Export merged Night Duty + Leave & Duty Report as a single PDF
     */
    private void exportCombinedReportPDF() {
        Document document = new Document();

        try {
            // Save file in Downloads folder
            String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    + "/NightDutyAndLeaveReport.pdf";

            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // -------- NIGHT DUTY REPORT --------
            document.add(new Paragraph("=== Night Duty Report ===\n\n"));

            int totalNightDutyHours = 48; // Example value, replace with actual calculation
            document.add(new Paragraph("Total Night Duty Hours: " + totalNightDutyHours + " hours\n"));

            double basicPay = 50000; // Example
            double dearnessAllowance = 10000; // Example
            double hourlyRate = (basicPay + dearnessAllowance) / 200.0;
            double nda = hourlyRate * (totalNightDutyHours / 6.0);

            document.add(new Paragraph("Basic Pay: " + basicPay));
            document.add(new Paragraph("Dearness Allowance: " + dearnessAllowance));
            document.add(new Paragraph("Hourly NDA Rate: " + hourlyRate));
            document.add(new Paragraph("NDA Calculation: " + nda + "\n\n"));

            // -------- LEAVE AND DUTY REPORT --------
            document.add(new Paragraph("=== Leave & Duty Report ===\n\n"));

            // Example leave summary
            Map<String, Integer> leaveSummary = new HashMap<>();
            leaveSummary.put("Sick Leave", 3);
            leaveSummary.put("Casual Leave", 5);
            leaveSummary.put("Earned Leave", 2);

            for (Map.Entry<String, Integer> entry : leaveSummary.entrySet()) {
                document.add(new Paragraph(entry.getKey() + " = " + entry.getValue() + " days"));
            }

            document.add(new Paragraph("\nReport Generated Successfully."));

            document.close();

            Toast.makeText(this, "PDF Exported to Downloads folder", Toast.LENGTH_LONG).show();

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error exporting PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
