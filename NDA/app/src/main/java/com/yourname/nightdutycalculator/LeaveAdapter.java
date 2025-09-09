package com.yourname.nightdutycalculator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class LeaveAdapter extends RecyclerView.Adapter<LeaveAdapter.LeaveViewHolder> {
    private ArrayList<LeaveModel> leaveList;

    public LeaveAdapter(ArrayList<LeaveModel> leaveList) {
        this.leaveList = leaveList;
    }

    @Override
    public LeaveViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new LeaveViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LeaveViewHolder holder, int position) {
        LeaveModel leave = leaveList.get(position);
        holder.text1.setText(leave.getDays() + " days");
        holder.text2.setText(leave.getType());
    }

    @Override
    public int getItemCount() {
        return leaveList.size();
    }

    static class LeaveViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;
        public LeaveViewHolder(View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}
