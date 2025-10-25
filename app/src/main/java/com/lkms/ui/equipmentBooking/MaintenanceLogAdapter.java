package com.lkms.ui.equipmentBooking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lkms.R;
import com.lkms.data.model.java.MaintenanceLog;

import java.util.List;

public class MaintenanceLogAdapter extends RecyclerView.Adapter<MaintenanceLogAdapter.LogViewHolder> {

    private List<MaintenanceLog> logList;

    public MaintenanceLogAdapter(List<MaintenanceLog> logList) {
        this.logList = logList;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_maintenance_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        MaintenanceLog log = logList.get(position);
        holder.tvType.setText(log.getMaintenanceType());
        holder.tvDate.setText(log.getMaintenanceTime());
        holder.tvDetail.setText(log.getDetail());
    }

    @Override
    public int getItemCount() {
        return logList != null ? logList.size() : 0;
    }

    public void updateList(List<MaintenanceLog> newList) {
        this.logList = newList;
        notifyDataSetChanged();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {

        TextView tvType, tvDate, tvDetail;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvMaintenanceType);
            tvDate = itemView.findViewById(R.id.tvMaintenanceDate);
            tvDetail = itemView.findViewById(R.id.tvMaintenanceDetail);
        }
    }
}