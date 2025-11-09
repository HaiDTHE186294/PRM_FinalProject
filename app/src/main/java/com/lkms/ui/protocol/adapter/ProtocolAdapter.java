package com.lkms.ui.protocol.adapter;


import android.content.Context;
import androidx.core.content.ContextCompat;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums.ProtocolApproveStatus;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.lkms.R;
import com.lkms.data.model.java.Protocol;

import java.util.Objects;

public class ProtocolAdapter extends ListAdapter<Protocol, ProtocolAdapter.ProtocolViewHolder> {

    //Interface để xử lý sự kiện click
    private final OnItemClickListener listener;

    // Hàm khởi tạo nhận vào listener
    public ProtocolAdapter(OnItemClickListener listener) {
        super(DIFF_CALLBACK); // Cung cấp DiffUtil cho ListAdapter
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProtocolViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_protocol, parent, false);
        return new ProtocolViewHolder(view, listener); // Truyền listener vào ViewHolder
    }

    @Override
    public void onBindViewHolder(@NonNull ProtocolViewHolder holder, int position) {
        Protocol currentProtocol = getItem(position); // Lấy item từ ListAdapter
        holder.bind(currentProtocol); // Gắn dữ liệu
    }

    // Lớp ViewHolder được nâng cấp
    static class ProtocolViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView version;
        TextView status;
        Context context;

        public ProtocolViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            this.context = itemView.getContext();
            title = itemView.findViewById(R.id.txtProtocolTitle);
            version = itemView.findViewById(R.id.txtProtocolVersion);
            status = itemView.findViewById(R.id.txtProtocolStatus);

            // Gán sự kiện click một lần duy nhất ở đây, hiệu quả hơn gán trong onBindViewHolder
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    // Gọi ra ngoài Activity thông qua interface
                    listener.onItemClick(position);
                }
            });
        }

        // Hàm bind để gán dữ liệu và xử lý logic hiển thị
        public void bind(final Protocol protocol) {
            title.setText(protocol.getProtocolTitle());
            version.setText("Version: " + protocol.getVersionNumber());

            //LÀM VIỆC VỚI ENUM
            ProtocolApproveStatus approveStatusEnum = protocol.getApproveStatus();

            // Hiển thị tên của Enum (ví dụ: "PENDING", "APPROVED")
            status.setText(approveStatusEnum.name());

            // Cập nhật màu sắc dựa trên Enum
            updateStatusColor(approveStatusEnum);
        }

        // CẬP NHẬT HÀM UPDATE MÀU, NHẬN VÀO ENUM
        private void updateStatusColor(ProtocolApproveStatus approveStatus) {
            GradientDrawable background = (GradientDrawable) status.getBackground().mutate();
            if (approveStatus == null) return;

            int colorResId; // Dùng ID của màu thay vì hard-code chuỗi màu

            // Dùng switch với Enum, an toàn hơn nhiều so với String
            switch (approveStatus) {
                case APPROVED:
                    colorResId = R.color.status_approved; // Màu xanh lá
                    break;
                case REJECTED:
                    colorResId = R.color.status_rejected; // Màu đỏ
                    break;
                case PENDING:
                default:
                    colorResId = R.color.status_pending;  // Màu cam
                    break;
            }
            // Lấy màu từ file colors.xml, cách làm chuẩn trong Android
            background.setColor(ContextCompat.getColor(context, colorResId));
        }

    }

    // DiffUtil để tăng hiệu năng cho ListAdapter
    // Nó giúp ListAdapter biết item nào đã thay đổi để cập nhật một cách thông minh
    private static final DiffUtil.ItemCallback<Protocol> DIFF_CALLBACK = new DiffUtil.ItemCallback<Protocol>() {
        @Override
        public boolean areItemsTheSame(@NonNull Protocol oldItem, @NonNull Protocol newItem) {
            // So sánh ID để biết 2 item có phải là một không
            return Objects.equals(oldItem.getProtocolId(), newItem.getProtocolId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Protocol oldItem, @NonNull Protocol newItem) {
            // So sánh nội dung để biết item có cần vẽ lại không
            return oldItem.equals(newItem);
        }
    };

    // Định nghĩa interface cho sự kiện click
    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
