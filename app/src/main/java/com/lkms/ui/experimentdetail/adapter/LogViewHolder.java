package com.lkms.ui.experimentdetail.adapter;

import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.lkms.R; // Đảm bảo import R đúng

public class LogViewHolder extends RecyclerView.ViewHolder {

    // Ánh xạ TextView từ item_log.xml
    private final TextView logEntry; // TextView hiển thị nội dung Log

    public LogViewHolder(View itemView) {
        super(itemView);
        logEntry = itemView.findViewById(R.id.logEntry);
    }

    public void bind(LogItemWrapper item) {
        // Ví dụ: Gán nội dung Log
        logEntry.setText(item.getContent());
    }
}