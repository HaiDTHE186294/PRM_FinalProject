package com.lkms.ui.protocol.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.lkms.R;
import com.lkms.data.model.java.Item;
import com.lkms.data.model.java.ProtocolItem;
import java.util.List;

public class ItemsDisplayAdapter extends RecyclerView.Adapter<ItemsDisplayAdapter.ItemViewHolder> {
    private final List<ProtocolItem> localItemsList;
    private final List<Item> localAvailableItems;

    public ItemsDisplayAdapter(List<ProtocolItem> itemsList, List<Item> availableItems) {
        this.localItemsList = itemsList;
        this.localAvailableItems = availableItems;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_create_item_display, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        ProtocolItem protocolItem = localItemsList.get(position);
        String itemName = "Unknown Item (ID: " + protocolItem.getItemId() + ")";
        for (Item availableItem : localAvailableItems) {
            if (availableItem.getItemId().equals(protocolItem.getItemId())) {
                itemName = availableItem.getItemName();
                break;
            }
        }
        holder.bind(itemName, protocolItem);
    }

    @Override
    public int getItemCount() {
        return localItemsList.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameText;
        TextView itemQuantityText;
        ImageButton removeItemButton;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNameText = itemView.findViewById(R.id.text_view_item_name);
            itemQuantityText = itemView.findViewById(R.id.text_view_item_quantity);
            removeItemButton = itemView.findViewById(R.id.button_remove_display_item);

            removeItemButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    localItemsList.remove(position);
                    notifyItemRemoved(position);
                }
            });
        }

        void bind(String name, ProtocolItem item) {
            itemNameText.setText(name);
            itemQuantityText.setText("x " + item.getQuantity());
        }
    }
}
