package com.lkms.ui.loginmaindashboard.maindashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lkms.R;
import com.lkms.data.model.java.Item;

import java.util.List;

public class InventoryAlertAdapter extends RecyclerView.Adapter<InventoryAlertAdapter.ViewHolder> {

    private final List<Item> alertList;

    public InventoryAlertAdapter(List<Item> alertList) {
        this.alertList = alertList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory_alert, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item alert = alertList.get(position);
        holder.tvAlertName.setText(alert.getItemName());

        String expirationText = alert.getExpirationDate();
        int daysLeft = alert.getDaysLeft();

        // Nếu có số ngày còn lại thì hiển thị thêm
        if (daysLeft != -999) {
            if (daysLeft > 0) {
                expirationText += "  (" + daysLeft + " ngày còn lại)";
            } else if (daysLeft == 0) {
                expirationText += "  (Hết hạn hôm nay)";
            } else {
                expirationText += "  (Đã hết hạn " + Math.abs(daysLeft) + " ngày)";
            }
        } else {
            expirationText += "  (Không xác định)";
        }

        holder.tvAlertDate.setText(expirationText);
    }

    @Override
    public int getItemCount() {
        return alertList != null ? alertList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAlertName, tvAlertDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAlertName = itemView.findViewById(R.id.tvAlertName);
            tvAlertDate = itemView.findViewById(R.id.tvAlertDate);
        }
    }
}
