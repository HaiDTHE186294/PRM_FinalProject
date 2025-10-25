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
import com.lkms.data.model.java.ProtocolItem;
import java.util.Objects;

/**
 * Adapter để hiển thị danh sách vật tư (ProtocolItem).
 * Sử dụng ListAdapter với DiffUtil để có hiệu năng cao và xử lý khóa chính phức hợp.
 */
public class ProtocolItemAdapter extends ListAdapter<ProtocolItem, ProtocolItemAdapter.ItemViewHolder> {

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
        ProtocolItem currentItem = getItem(position);
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

        public void bind(ProtocolItem item) {
            if (item != null) {
                // Do model ProtocolItem không có tên, ta tạm thời hiển thị ID.
                // Sau này, bạn cần có cơ chế để lấy tên từ itemId.
                if (item.getItemId() != null) {
                    String nameText = "Vật tư ID: " + item.getItemId();
                    tvItemName.setText(nameText);
                }

                // Hiển thị số lượng
                if (item.getQuantity() != null) {
                    String quantityText = "x" + item.getQuantity();
                    tvQuantity.setText(quantityText);
                } else {
                    tvQuantity.setText(""); // Ẩn nếu không có số lượng
                }
            }
        }
    }

    /**
     * DiffUtil.ItemCallback để ListAdapter tính toán sự thay đổi trong danh sách.
     */
    private static final DiffUtil.ItemCallback<ProtocolItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<ProtocolItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull ProtocolItem oldItem, @NonNull ProtocolItem newItem) {
            // Vì có khóa chính phức hợp (protocolId, itemId), ta phải so sánh cả hai ID
            // để xác định xem chúng có phải là cùng một item hay không.
            // Objects.equals an toàn với các giá trị có thể là null.
            return Objects.equals(oldItem.getProtocolId(), newItem.getProtocolId()) &&
                    Objects.equals(oldItem.getItemId(), newItem.getItemId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ProtocolItem oldItem, @NonNull ProtocolItem newItem) {
            // Lombok's @Data đã tự tạo ra hàm equals() hoàn hảo để so sánh nội dung (quantity).
            // Hàm này sẽ kiểm tra xem trường quantity có thay đổi hay không.
            return oldItem.equals(newItem);
        }
    };
}

