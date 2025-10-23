package com.lkms.ui.equipmentBooking;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lkms.data.model.java.Equipment;
import com.lkms.R;

import java.util.List;

public class EquipmentAdapter extends RecyclerView.Adapter<EquipmentAdapter.EquipmentViewHolder> {

    private final List<Equipment> equipmentList;
    private final OnEquipmentClickListener listener;

    public interface OnEquipmentClickListener {
        void onClick(Equipment equipment);
    }

    public EquipmentAdapter(List<Equipment> list, OnEquipmentClickListener listener) {
        this.equipmentList = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EquipmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_equipment, parent, false);
        return new EquipmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EquipmentViewHolder holder, int position) {
        Equipment eq = equipmentList.get(position);

        holder.tvName.setText(eq.getEquipmentName());
        holder.tvSerial.setText("Serial: " + eq.getSerialNumber());
        holder.tvLocation.setText("Location: " + eq.getModel());
        holder.tvStatus.setText("Status: " + eq.getAvailability());

        holder.itemView.setOnClickListener(v -> {
            listener.onClick(eq);

            Intent intent = new Intent(holder.itemView.getContext(), EquipmentDetailActivity.class);
            intent.putExtra(EquipmentDetailActivity.EXTRA_EQUIPMENT_ID, eq.getEquipmentId());
            intent.putExtra(EquipmentDetailActivity.EXTRA_EQUIPMENT_NAME, eq.getEquipmentName());
            holder.itemView.getContext().startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return equipmentList.size();
    }

    static class EquipmentViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSerial, tvLocation, tvStatus;
        public EquipmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvSerial = itemView.findViewById(R.id.tvSerial);
            tvLocation = itemView.findViewById(R.id.tvModel);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
