package com.yourname.nightdutycalculator;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.ViewHolder> {
    private List<DutyRecord> records;
    private OnRecordDeleteListener deleteListener;
    private DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");

    public interface OnRecordDeleteListener {
        void onRecordDelete(DutyRecord record, int position);
    }

    public RecordsAdapter(List<DutyRecord> records, OnRecordDeleteListener deleteListener) {
        this.records = records;
        this.deleteListener = deleteListener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_record, parent, false);
        return new ViewHolder(view);
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DutyRecord record = records.get(position);

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(record.getDate());
            holder.tvDate.setText(outputFormat.format(date));
        } catch (Exception e) { holder.tvDate.setText(record.getDate()); }

        holder.tvTime.setText(record.getDutyFrom() + " - " + record.getDutyTo());
        holder.tvHours.setText(String.format("%.1f hrs", record.getTotalNightHours()));

        // Set duty type text and color with dual duty and weekly rest CR indications
        String typeText = record.isNationalHoliday() ? "🎉 Holiday" : "📅 Regular";
        int typeColor = record.isNationalHoliday() ? Color.parseColor("#27AE60") : Color.BLACK;

        if(record.isDualDuty()) {
            typeText += " | Dual Duty (CR pending)";
            typeColor = Color.parseColor("#D32F2F"); // Red color for CR warning
        }
        if(record.isDutyOnWeeklyRest()) {
            typeText += " | Duty on Weekly Rest (CR pending)";
            typeColor = Color.parseColor("#D32F2F");
        }

        holder.tvType.setText(typeText);
        holder.tvType.setTextColor(typeColor);

        holder.tvAllowance.setText("₹" + decimalFormat.format(record.getNightDutyAllowance()));

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onRecordDelete(record, position);
        });
    }

    @Override public int getItemCount() { return records.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTime, tvHours, tvType, tvAllowance;
        Button btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvHours = itemView.findViewById(R.id.tvHours);
            tvType = itemView.findViewById(R.id.tvType);
            tvAllowance = itemView.findViewById(R.id.tvAllowance);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
