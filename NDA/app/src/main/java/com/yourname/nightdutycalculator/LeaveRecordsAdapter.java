package com.yourname.nightdutycalculator;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LeaveRecordsAdapter extends RecyclerView.Adapter<LeaveRecordsAdapter.ViewHolder> {
    private List<LeaveRecord> leaveRecords;
    private OnLeaveDeleteListener deleteListener;

    public interface OnLeaveDeleteListener {
        void onLeaveDelete(LeaveRecord record, int position);
    }

    public LeaveRecordsAdapter(List<LeaveRecord> leaveRecords, OnLeaveDeleteListener deleteListener) {
        this.leaveRecords = leaveRecords;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leave_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaveRecord record = leaveRecords.get(position);
        
        // Format leave period
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            
            Date fromDate = inputFormat.parse(record.getLeaveFrom());
            Date toDate = inputFormat.parse(record.getLeaveTo());
            
            String periodText = outputFormat.format(fromDate) + " - " + outputFormat.format(toDate);
            holder.tvLeavePeriod.setText(periodText);
        } catch (Exception e) {
            holder.tvLeavePeriod.setText(record.getLeaveFrom() + " - " + record.getLeaveTo());
        }
        
        // Set leave type
        holder.tvLeaveType.setText(record.getLeaveType());
        
        // Set status with color
        holder.tvLeaveStatus.setText(record.getStatus());
        holder.tvLeaveStatus.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
        
        // Show notes if available
        if (record.getNotes() != null && !record.getNotes().isEmpty()) {
            holder.tvLeaveNotes.setText(record.getNotes());
            holder.tvLeaveNotes.setVisibility(View.VISIBLE);
        } else {
            holder.tvLeaveNotes.setVisibility(View.GONE);
        }
        
        // Set delete button listener
        holder.btnDeleteLeave.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onLeaveDelete(record, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return leaveRecords.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLeavePeriod, tvLeaveType, tvLeaveNotes, tvLeaveStatus;
        MaterialButton btnDeleteLeave;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLeavePeriod = itemView.findViewById(R.id.tvLeavePeriod);
            tvLeaveType = itemView.findViewById(R.id.tvLeaveType);
            tvLeaveNotes = itemView.findViewById(R.id.tvLeaveNotes);
            tvLeaveStatus = itemView.findViewById(R.id.tvLeaveStatus);
            btnDeleteLeave = itemView.findViewById(R.id.btnDeleteLeave);
        }
    }
}
