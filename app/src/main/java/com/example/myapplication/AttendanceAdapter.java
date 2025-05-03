package com.example.myapplication;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {
    private List<Attendance> attendanceList;

    public AttendanceAdapter(List<Attendance> list) {
        this.attendanceList = list;
    }

    @Override
    public AttendanceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.attendance_item, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AttendanceViewHolder holder, int position) {
        Attendance item = attendanceList.get(position);

        // Nomor otomatis dari 1, 2, 3, ...
        holder.textId.setText(String.valueOf(position + 1));

        holder.textdate.setText("" + item.date);
        holder.textStatus.setText("" + item.status);
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    public static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        TextView textId, textdate, textStatus;

        public AttendanceViewHolder(View itemView) {
            super(itemView);
            textId = itemView.findViewById(R.id.textNo);
            textdate = itemView.findViewById(R.id.textDate);
            textStatus = itemView.findViewById(R.id.textStatus);
        }
    }
}
