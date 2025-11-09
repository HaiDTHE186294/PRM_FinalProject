// StepViewHolder.java
package com.lkms.ui.experimentdetail.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.lkms.R;

import org.jetbrains.annotations.Contract;

public class StepViewHolder extends RecyclerView.ViewHolder {

    private final TextView tvStepTitle;
    private final ImageView ivArrow; // ImageView cho mũi tên
    final ImageView ivAdd;

    public StepViewHolder(View itemView) {
        super(itemView);
        // Thay thế bằng ID từ item_step.xml của bạn
        tvStepTitle = itemView.findViewById(R.id.step_instruction_textview);
        ivArrow = itemView.findViewById(R.id.placeholder_ivArrow);
        ivAdd = itemView.findViewById(R.id.placeholder_ivAdd);
    }


    public void bind(StepItemWrapper item) {
        // Gán tiêu đề Step
        tvStepTitle.setText(String.valueOf(item.getProtocolStep().getInstruction()));

        // Cập nhật trạng thái icon mũi tên (Tùy thuộc vào trạng thái mở rộng)
        if (item.isExpanded()) {
            ivArrow.setImageResource(R.drawable.ic_arrow_down); // Biểu tượng mũi tên xuống
        } else {
            ivArrow.setImageResource(R.drawable.ic_arrow_right); // Biểu tượng mũi tên sang phải
        }
    }
}