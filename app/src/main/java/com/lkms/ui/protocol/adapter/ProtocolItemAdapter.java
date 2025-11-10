package com.lkms.ui.protocol.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.lkms.R;
import com.lkms.ui.protocol.viewmodel.ProtocolDetailViewModel;
import java.util.Objects;

/**
 * Adapter để hiển thị danh sách vật tư đã có chi tiết (tên, đơn vị).
 * Sử dụng ListAdapter với lớp ProtocolItemView từ ViewModel.
 */
public class ProtocolItemAdapter extends ListAdapter<ProtocolDetailViewModel.ProtocolItemView, ProtocolItemAdapter.ItemViewHolder> {

    public ProtocolItemAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_layout, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        // Bây giờ getItem() sẽ trả về một đối tượng ProtocolItemView
        ProtocolDetailViewModel.ProtocolItemView currentItem = getItem(position);
        holder.bind(currentItem);
    }

    /**
     * Lớp ViewHolder cho một item vật tư.
     */
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvItemName;
        private final TextView tvQuantity;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
        }

        public void bind(ProtocolDetailViewModel.ProtocolItemView item) {
            if (item != null) {
                // Tuyệt vời! Giờ chúng ta đã có tên để hiển thị trực tiếp
                tvItemName.setText(item.itemName);

                // Hiển thị số lượng và đơn vị (nếu có)
                String quantityText = "x" + item.quantity;
                if (item.unit != null && !item.unit.isEmpty()) {
                    quantityText += " " + item.unit;
                }
                tvQuantity.setText(quantityText);
            }
        }
    }

    /**
     * DiffUtil.ItemCallback để ListAdapter tính toán sự thay đổi trong danh sách.
     */
    private static final DiffUtil.ItemCallback<ProtocolDetailViewModel.ProtocolItemView> DIFF_CALLBACK = new DiffUtil.ItemCallback<ProtocolDetailViewModel.ProtocolItemView>() {
        @Override
        public boolean areItemsTheSame(@NonNull ProtocolDetailViewModel.ProtocolItemView oldItem, @NonNull ProtocolDetailViewModel.ProtocolItemView newItem) {
            // ID của item vẫn là định danh duy nhất
            return oldItem.itemId == newItem.itemId;
        }

        @Override
        public boolean areContentsTheSame(@NonNull ProtocolDetailViewModel.ProtocolItemView oldItem, @NonNull ProtocolDetailViewModel.ProtocolItemView newItem) {
            // So sánh tất cả các nội dung có thể thay đổi
            return oldItem.quantity.equals(newItem.quantity) &&
                    oldItem.itemName.equals(newItem.itemName) &&
                    Objects.equals(oldItem.unit, newItem.unit);
        }
    };
}
