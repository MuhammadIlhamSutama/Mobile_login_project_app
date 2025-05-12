package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final List<String> historyList;

    public HistoryAdapter(List<String> historyList) {
        this.historyList = historyList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textMessage, textDate;

        public ViewHolder(View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textDate = itemView.findViewById(R.id.textDate);
        }
    }

    @NonNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.ViewHolder holder, int position) {
        String date = historyList.get(position);
        holder.textTitle.setText("Password Has Been Changed");
        holder.textDate.setText(date);  // From historyList
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }
}
