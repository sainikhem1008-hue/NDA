package com.yourname.nightdutycalculator;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class LeaveManagementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave_management);

        // Temporary text (replace later with real leave form UI)
        TextView tv = new TextView(this);
        tv.setText("Leave Management Screen (Work in Progress)");
        tv.setTextSize(18);
        tv.setPadding(40, 80, 40, 80);
        setContentView(tv);
    }
}
