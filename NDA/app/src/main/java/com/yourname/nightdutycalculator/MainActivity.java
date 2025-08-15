package com.yourname.nightdutycalculator;
import androidx.core.content.ContextCompat;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements RecordsAdapter.OnRecordDeleteListener {

	private TextInputEditText etDutyDate, etDutyFrom, etDutyTo, etCeilingLimit, etBasicPay, etDearnessAllowance;
	private CheckBox cbNationalHoliday;
	private Button btnCalculate, btnSave, btnExport, btnClear;
	private LinearLayout llResults;
	private TextView tvCeilingWarning;
	private RecyclerView rvRecords;
	private List<DutyRecord> records = new ArrayList<>();
	private RecordsAdapter adapter;
	private DutyRecord currentCalculation;
	private SharedPreferences sharedPreferences;
	private Gson gson = new Gson();
	private DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
	private Vibrator vibrator;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initViews(); setupListeners(); loadRecords(); setDefaultValues();
	}

	@Override public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 0, "Exit");
		return true;
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == 1) { finishAffinity(); return true; }
		return super.onOptionsItemSelected(item);
	}

	private void initViews() {
		etDutyDate = findViewById(R.id.etDutyDate);
		etDutyFrom = findViewById(R.id.etDutyFrom);
		etDutyTo = findViewById(R.id.etDutyTo);
		etCeilingLimit = findViewById(R.id.etCeilingLimit);
		etBasicPay = findViewById(R.id.etBasicPay);
		etDearnessAllowance = findViewById(R.id.etDearnessAllowance);
		cbNationalHoliday = findViewById(R.id.cbNationalHoliday);
        cbNationalHoliday.setTextColor(ContextCompat.getColor(this, R.color.accent_green));
		btnCalculate = findViewById(R.id.btnCalculate);
		btnSave = findViewById(R.id.btnSave);
		btnExport = findViewById(R.id.btnExport);
		btnClear = findViewById(R.id.btnClear);
		llResults = findViewById(R.id.llResults);
		tvCeilingWarning = findViewById(R.id.tvCeilingWarning);
		rvRecords = findViewById(R.id.rvRecords);
		sharedPreferences = getSharedPreferences("NightDutyRecords", Context.MODE_PRIVATE);
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		adapter = new RecordsAdapter(records, this);
		if (rvRecords != null) {
			rvRecords.setLayoutManager(new LinearLayoutManager(this));
			rvRecords.setAdapter(adapter);
		}
	}

	private void setupListeners() {
		etDutyDate.setOnClickListener(v -> showDatePicker());
		etDutyFrom.setOnClickListener(v -> showTimePicker(etDutyFrom));
		etDutyTo.setOnClickListener(v -> showTimePicker(etDutyTo));
		btnCalculate.setOnClickListener(v -> { vibrate(); calculateNightDuty(); });
		btnSave.setOnClickListener(v -> { vibrate(); saveRecord(); });
		btnExport.setOnClickListener(v -> { vibrate(); shareSummaryText(); });
		btnClear.setOnClickListener(v -> { vibrate(); clearAllRecords(); });
		Button btnExit = findViewById(R.id.btnExit);
		if (btnExit != null) btnExit.setOnClickListener(v -> { vibrate(); finishAffinity(); });

		etBasicPay.addTextChangedListener(new TextWatcher() {
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) { checkCeilingLimit(); }
			@Override public void afterTextChanged(Editable s) {}
		});
		etCeilingLimit.addTextChangedListener(new TextWatcher() {
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) { checkCeilingLimit(); }
			@Override public void afterTextChanged(Editable s) {}
		});
	}

	private void vibrate() { if (vibrator != null && vibrator.hasVibrator()) vibrator.vibrate(50); }

	private void checkCeilingLimit() {
		try { double basicPay = Double.parseDouble(etBasicPay.getText().toString()); double ceilingLimit = Double.parseDouble(etCeilingLimit.getText().toString());
			if (basicPay > ceilingLimit) { if (tvCeilingWarning != null) { tvCeilingWarning.setVisibility(View.VISIBLE); tvCeilingWarning.setText("⚠ Using ceiling limit ₹" + decimalFormat.format(ceilingLimit)); } }
			else if (tvCeilingWarning != null) tvCeilingWarning.setVisibility(View.GONE);
		} catch (Exception e) { if (tvCeilingWarning != null) tvCeilingWarning.setVisibility(View.GONE); }
	}

	private void setDefaultValues() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		etDutyDate.setText(sdf.format(new Date()));
		etDutyFrom.setText("22:00");
		etDutyTo.setText("00:00");
		calculateNightDuty();
	}

	private void showDatePicker() {
		Calendar calendar = Calendar.getInstance();
		DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
			calendar.set(year, month, dayOfMonth);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
			etDutyDate.setText(sdf.format(calendar.getTime()));
		}, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
		datePickerDialog.show();
	}

	private void showTimePicker(TextInputEditText editText) {
		Calendar calendar = Calendar.getInstance();
		TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
			String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
			editText.setText(time);
		}, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
		timePickerDialog.show();
	}

	private void calculateNightDuty() {
		try {
			String dutyDate = etDutyDate.getText().toString();
			String dutyFrom = etDutyFrom.getText().toString();
			String dutyTo = etDutyTo.getText().toString();
			double basicPay = Double.parseDouble(etBasicPay.getText().toString());
			double ceilingLimit = Double.parseDouble(etCeilingLimit.getText().toString());
			double dearnessAllowance = Double.parseDouble(etDearnessAllowance.getText().toString());
			boolean isNationalHoliday = cbNationalHoliday.isChecked();

			if (dutyDate.isEmpty() || dutyFrom.isEmpty() || dutyTo.isEmpty()) {
				Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
				return;
			}

			double effectiveBasicPay = Math.min(basicPay, ceilingLimit);

			Calendar fromCal = Calendar.getInstance();
			Calendar toCal = Calendar.getInstance();

			String[] fromParts = dutyFrom.split(":");
			String[] toParts = dutyTo.split(":");

			fromCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(fromParts[0]));
			fromCal.set(Calendar.MINUTE, Integer.parseInt(fromParts[1]));
			fromCal.set(Calendar.SECOND, 0);

			toCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(toParts[0]));
			toCal.set(Calendar.MINUTE, Integer.parseInt(toParts[1]));
			toCal.set(Calendar.SECOND, 0);

			if (toCal.before(fromCal) || toCal.equals(fromCal)) toCal.add(Calendar.DAY_OF_MONTH, 1);

			long durationMs = toCal.getTimeInMillis() - fromCal.getTimeInMillis();
			double totalDutyHours = durationMs / (1000.0 * 60 * 60);

			double nightHours1 = calculateNightHours(fromCal, toCal, 22, 0, 0, 0);
			double nightHours2 = calculateNightHours(fromCal, toCal, 0, 0, 6, 0);

			double totalNightHours = nightHours1 + nightHours2;
			double nightHoursDivided = totalNightHours / 6.0;

			double nightDutyAllowance = nightHoursDivided * (effectiveBasicPay * (1 + dearnessAllowance/100)) / 200;

			currentCalculation = new DutyRecord();
			currentCalculation.setDate(dutyDate);
			currentCalculation.setDutyFrom(dutyFrom);
			currentCalculation.setDutyTo(dutyTo);
			currentCalculation.setTotalDutyHours(totalDutyHours);
			currentCalculation.setNightHours1(nightHours1);
			currentCalculation.setNightHours2(nightHours2);
			currentCalculation.setTotalNightHours(totalNightHours);
			currentCalculation.setBasicPay(basicPay);
			currentCalculation.setEffectiveBasicPay(effectiveBasicPay);
			currentCalculation.setDearnessAllowance(dearnessAllowance);
			currentCalculation.setNightDutyAllowance(nightDutyAllowance);
			currentCalculation.setNationalHoliday(isNationalHoliday);

			displayResults();
		} catch (NumberFormatException e) {
			Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	private double calculateNightHours(Calendar fromCal, Calendar toCal, int startHour, int startMin, int endHour, int endMin) {
		Calendar nightStart = (Calendar) fromCal.clone();
		Calendar nightEnd = (Calendar) fromCal.clone();

		nightStart.set(Calendar.HOUR_OF_DAY, startHour);
		nightStart.set(Calendar.MINUTE, startMin);
		nightStart.set(Calendar.SECOND, 0);

		nightEnd.set(Calendar.HOUR_OF_DAY, endHour);
		nightEnd.set(Calendar.MINUTE, endMin);
		nightEnd.set(Calendar.SECOND, 0);

		if (endHour < startHour || (endHour == startHour && endMin <= startMin)) nightEnd.add(Calendar.DAY_OF_MONTH, 1);

		Calendar overlapStart = fromCal.after(nightStart) ? fromCal : nightStart;
		Calendar overlapEnd = toCal.before(nightEnd) ? toCal : nightEnd;

		if (overlapStart.before(overlapEnd)) {
			long overlapMs = overlapEnd.getTimeInMillis() - overlapStart.getTimeInMillis();
			return Math.max(0, overlapMs / (1000.0 * 60 * 60));
		}
		return 0;
	}

	private void displayResults() {
		if (llResults != null) llResults.removeAllViews();
		if (currentCalculation == null) return;
		addResultItem("Total Duty Hours:", String.format("%.2f hrs", currentCalculation.getTotalDutyHours()));
		addResultItem("Night Hours (22:00-00:00):", String.format("%.2f hrs", currentCalculation.getNightHours1()));
		addResultItem("Night Hours (00:00-06:00):", String.format("%.2f hrs", currentCalculation.getNightHours2()));
		addResultItem("Total Night Hours:", String.format("%.2f hrs", currentCalculation.getTotalNightHours()));
		addResultItem("Night Allowance:", "₹" + decimalFormat.format(currentCalculation.getNightDutyAllowance()));
	}

	private void addResultItem(String label, String value) {
		TextView tv = new TextView(this);
		tv.setText(label + " " + value);
		tv.setTextSize(16);
		if (llResults != null) llResults.addView(tv);
	}

	private void saveRecord() {
		if (currentCalculation == null) { Toast.makeText(this, "Please calculate first", Toast.LENGTH_SHORT).show(); return; }
		boolean exists = false;
		for (int i = 0; i < records.size(); i++) { if (records.get(i).getDate().equals(currentCalculation.getDate())) { records.set(i, currentCalculation); exists = true; break; } }
		if (!exists) records.add(currentCalculation);
		saveRecordsToPrefs();
		if (adapter != null) adapter.notifyDataSetChanged();
		Toast.makeText(this, "Record saved successfully!", Toast.LENGTH_SHORT).show();
	}

	private void shareSummaryText() {
		if (records.isEmpty()) { Toast.makeText(this, "No records to share", Toast.LENGTH_SHORT).show(); return; }
		StringBuilder builder = new StringBuilder();
		builder.append("Night Duty Allowance Report\n\n");
		double totalAllowance = 0; double totalHours = 0; int holidayCount = 0; int regularCount = 0;
		for (DutyRecord record : records) {
			totalAllowance += record.getNightDutyAllowance();
			totalHours += record.getTotalNightHours();
			if (record.isNationalHoliday()) holidayCount++; else regularCount++;
			builder.append(String.format(Locale.getDefault(), "%s  %s-%s  Hours: %.1f  Allowance: ₹%s  %s\n",
				record.getDate(), record.getDutyFrom(), record.getDutyTo(), record.getTotalNightHours(), decimalFormat.format(record.getNightDutyAllowance()), record.isNationalHoliday()?"Holiday":"Regular"));
		}
		builder.append(String.format(Locale.getDefault(), "\nTotals: %d records, Regular: %d, Holidays: %d, Hours: %.2f, Allowance: ₹%s\n",
			records.size(), regularCount, holidayCount, totalHours, decimalFormat.format(totalAllowance)));

		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, builder.toString());
		startActivity(Intent.createChooser(intent, "Share summary"));
	}

	private void clearAllRecords() {
		new AlertDialog.Builder(this).setTitle("Clear All Records").setMessage("Are you sure you want to clear all records? This cannot be undone.").setPositiveButton("Yes", (dialog, which) -> {
			records.clear(); saveRecordsToPrefs(); if (adapter!=null) adapter.notifyDataSetChanged(); if (llResults!=null) llResults.removeAllViews(); currentCalculation = null; Toast.makeText(this, "All records cleared", Toast.LENGTH_SHORT).show();
		}).setNegativeButton("No", null).show();
	}

	@Override public void onRecordDelete(DutyRecord record, int position) {
		new AlertDialog.Builder(this).setTitle("Delete Record").setMessage("Are you sure you want to delete this record?").setPositiveButton("Yes", (dialog, which) -> {
			records.remove(position); saveRecordsToPrefs(); if (adapter!=null) adapter.notifyItemRemoved(position); Toast.makeText(this, "Record deleted", Toast.LENGTH_SHORT).show();
		}).setNegativeButton("No", null).show();
	}

	private void loadRecords() {
		String recordsJson = sharedPreferences.getString("records", "[]"); Type listType = new TypeToken<List<DutyRecord>>(){}.getType(); List<DutyRecord> loadedRecords = gson.fromJson(recordsJson, listType);
		if (loadedRecords != null) { records.clear(); records.addAll(loadedRecords); Collections.sort(records, (a, b) -> b.getDate().compareTo(a.getDate())); if (adapter!=null) adapter.notifyDataSetChanged(); }
	}

	private void saveRecordsToPrefs() { String recordsJson = gson.toJson(records); sharedPreferences.edit().putString("records", recordsJson).apply(); }
}
