package com.yourname.nightdutycalculator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LeaveRecordsAdapter extends RecyclerView.Adapter<LeaveRecordsAdapter.ViewHolder> {

    private List<LeaveRecord> leaveRecords;
    private Context context;
    private SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public LeaveRecordsAdapter(Context context, List<LeaveRecord> leaveRecords) {
        this.context = context;
        this.leaveRecords = leaveRecords;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_leave_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        LeaveRecord record = leaveRecords.get(position);

        try {
            Date fromDate = inputFormat.parse(record.getLeaveFrom());
            Date toDate = inputFormat.parse(record.getLeaveTo());

            String formattedPeriod = displayFormat.format(fromDate) + " - " + displayFormat.format(toDate);
            holder.tvLeavePeriod.setText(formattedPeriod);
        } catch (Exception e) {
            holder.tvLeavePeriod.setText(record.getLeaveFrom() + " - " + record.getLeaveTo());
        }

        holder.tvLeaveType.setText(record.getLeaveType());
        holder.tvLeaveStatus.setText(record.getStatus());

        // Handle notes safely
        if (record.getNotes() != null && !record.getNotes().trim().isEmpty()) {
            holder.tvLeaveNotes.setText(record.getNotes());
        } else {
            holder.tvLeaveNotes.setText("No notes");
        }
    }

    @Override
    public int getItemCount() {
        return leaveRecords.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLeavePeriod, tvLeaveType, tvLeaveStatus, tvLeaveNotes;

        public ViewHolder(View itemView) {
            super(itemView);
            tvLeavePeriod = itemView.findViewById(R.id.tvLeavePeriod);
            tvLeaveType = itemView.findViewById(R.id.tvLeaveType);
            tvLeaveStatus = itemView.findViewById(R.id.tvLeaveStatus);
            tvLeaveNotes = itemView.findViewById(R.id.tvLeaveNotes);
        }
    }
}
