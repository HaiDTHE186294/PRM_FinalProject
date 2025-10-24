// StepViewHolder.java
package com.lkms.ui.experimentdetail.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.lkms.R;

import org.jetbrains.annotations.Contract;

public class StepViewHolder extends RecyclerView.ViewHolder {

    private final TextView placeholder_tvStepTitle;
    private final ImageView placeholder_ivArrow; // ImageView cho mũi tên

    public StepViewHolder(View itemView) {
        super(itemView);
        // Thay thế bằng ID từ item_step.xml của bạn
        placeholder_tvStepTitle = itemView.findViewById(R.id.placeholder_tvStepTitle);
        placeholder_ivArrow = itemView.findViewById(R.id.placeholder_ivArrow);
    }


    public void bind(StepItemWrapper item) {
        // Gán tiêu đề Step
        placeholder_tvStepTitle.setText(String.valueOf(item.getProtocolStep().getStepOrder()));

        // Cập nhật trạng thái icon mũi tên (Tùy thuộc vào trạng thái mở rộng)
        if (item.isExpanded()) {
            placeholder_ivArrow.setImageResource(R.drawable.ic_arrow_down); // Biểu tượng mũi tên xuống
            // Có thể thêm logic xoay icon ở đây nếu muốn Animation mượt hơn
        } else {
            placeholder_ivArrow.setImageResource(R.drawable.ic_arrow_right); // Biểu tượng mũi tên sang phải
        }
    }
}