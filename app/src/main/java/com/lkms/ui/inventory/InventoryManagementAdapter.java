package com.lkms.ui.inventory;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.lkms.R;
import com.lkms.data.model.java.Item;
import java.util.List;

public class InventoryManagementAdapter extends RecyclerView.Adapter<InventoryManagementAdapter.InventoryViewHolder> {
    private final List<Item> itemList;
    public InventoryManagementAdapter(List<Item> itemList) {
        this.itemList = itemList;
    }

    @Override
    public InventoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(InventoryViewHolder holder, int position) {
        // Lấy item tại vị trí hiện tại và gán vào ViewHolder
        Item item = itemList.get(position);
        holder.tvItemName.setText(item.getItemName());
        holder.tvItemCas.setText("CAS: " + item.getCasNumber());

        // Set click listener on the item
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ItemDetailActivity.class);
            intent.putExtra("ITEM_ID", item.getItemId());  // Assuming itemId is a method in Item class
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // ViewHolder để ánh xạ các view trong layout
    public static class InventoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName;
        TextView tvItemCas;
        public InventoryViewHolder(View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tv_item_name);
            tvItemCas = itemView.findViewById(R.id.tv_item_cas);
        }
    }
}
