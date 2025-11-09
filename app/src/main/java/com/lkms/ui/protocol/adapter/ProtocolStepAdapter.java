
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
import com.lkms.data.model.java.ProtocolStep;

// Kế thừa từ ListAdapter thay vì RecyclerView.Adapter
public class ProtocolStepAdapter extends ListAdapter<ProtocolStep, ProtocolStepAdapter.StepViewHolder> {

    // 1. Khởi tạo Adapter với DiffUtil.ItemCallback
    public ProtocolStepAdapter() {
        super(DIFF_CALLBACK);
    }

    // 2. Tạo ViewHolder
    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.step_item_layout, parent, false);
        return new StepViewHolder(view);
    }

    // 3. Gắn dữ liệu vào ViewHolder
    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        ProtocolStep currentStep = getItem(position);
        holder.bind(currentStep);
    }

    // 4. Lớp ViewHolder
    static class StepViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvStepOrder;
        private final TextView tvInstruction;

        public StepViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStepOrder = itemView.findViewById(R.id.tvStepOrder);
            tvInstruction = itemView.findViewById(R.id.tvInstruction);
        }

        // Hàm bind để gắn dữ liệu từ model vào view
        public void bind(ProtocolStep step) {
            if (step != null) {
                // Hiển thị số thứ tự của bước
                if (step.getStepOrder() != null) {
                    tvStepOrder.setText(String.valueOf(step.getStepOrder()));
                }
                // Hiển thị nội dung hướng dẫn
                tvInstruction.setText(step.getInstruction());
            }
        }
    }

    // 5. DiffUtil.ItemCallback: Bộ não so sánh sự khác biệt
    // Giúp ListAdapter biết item nào đã thay đổi, thêm, hoặc xóa
    private static final DiffUtil.ItemCallback<ProtocolStep> DIFF_CALLBACK = new DiffUtil.ItemCallback<ProtocolStep>() {
        @Override
        public boolean areItemsTheSame(@NonNull ProtocolStep oldItem, @NonNull ProtocolStep newItem) {
            // So sánh dựa trên ID duy nhất. Nếu ID giống nhau, chúng là cùng một item.
            return oldItem.getProtocolStepId().equals(newItem.getProtocolStepId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ProtocolStep oldItem, @NonNull ProtocolStep newItem) {
            // So sánh nội dung. Nếu nội dung khác nhau, item đó cần được vẽ lại.
            // Lombok's @Data đã tự tạo hàm equals() cho chúng ta.
            return oldItem.equals(newItem);
        }
    };
}
