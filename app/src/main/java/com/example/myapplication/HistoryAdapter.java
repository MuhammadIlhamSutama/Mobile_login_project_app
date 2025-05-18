package com.example.myapplication;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import data.model.Foto;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private Context context;
    private List<Map.Entry<String, List<Foto>>> groupedFotos;

    public HistoryAdapter(Context context, List<Map.Entry<String, List<Foto>>> groupedFotos) {
        this.context = context;
        this.groupedFotos = groupedFotos;
    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HistoryViewHolder holder, int position) {
        Map.Entry<String, List<Foto>> entry = groupedFotos.get(position);
        String tanggal = entry.getKey();
        List<Foto> fotoList = entry.getValue();

        holder.tvDate.setText(tanggal);

        if (fotoList.size() >= 1) {
            Glide.with(context).load(fotoList.get(0).url).into(holder.imageView1);
            holder.tvCheckIn.setText("Check-In : " + fotoList.get(0).jam);
            holder.imageView1.setOnClickListener(v -> showImageDialog(fotoList.get(0).url));
        } else {
            holder.imageView1.setImageResource(R.drawable.ic_image_placeholder);
            holder.tvCheckIn.setText("Check-In : -");
            holder.imageView1.setOnClickListener(null);
        }

        if (fotoList.size() >= 2) {
            Glide.with(context).load(fotoList.get(1).url).into(holder.imageView2);
            holder.tvCheckOut.setText("Check-Out : " + fotoList.get(1).jam);
            holder.imageView2.setOnClickListener(v -> showImageDialog(fotoList.get(1).url));
        } else {
            holder.imageView2.setImageResource(R.drawable.ic_image_placeholder);
            holder.tvCheckOut.setText("Check-Out : -");
            holder.imageView2.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return groupedFotos.size();
    }

    public void addData(List<Map.Entry<String, List<Foto>>> newData) {
        int start = groupedFotos.size();
        groupedFotos.addAll(newData);
        notifyItemRangeInserted(start, newData.size());
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvCheckIn, tvCheckOut;
        ImageView imageView1, imageView2;

        public HistoryViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvCheckIn = itemView.findViewById(R.id.tvCheckIn);
            tvCheckOut = itemView.findViewById(R.id.tvCheckOut);
            imageView1 = itemView.findViewById(R.id.imageView1);
            imageView2 = itemView.findViewById(R.id.imageView2);
        }
    }

    private void showImageDialog(String imageUrl) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_image);
        ImageView ivLargeImage = dialog.findViewById(R.id.ivLargeImage);
        Glide.with(context).load(imageUrl).into(ivLargeImage);
        dialog.show();
    }
}
