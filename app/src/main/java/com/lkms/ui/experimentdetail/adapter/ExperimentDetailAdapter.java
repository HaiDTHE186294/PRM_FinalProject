package com.lkms.ui.experimentdetail.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.lkms.R;


import com.lkms.data.model.java.*;

import java.util.ArrayList;
import java.util.List;

public class ExperimentDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Danh sách phẳng, sẽ được cập nhật từ ViewModel
    private List<AdapterItem> mItems;

    // Interface để giao tiếp với Activity/ViewModel
    private final OnStepClickListener stepClickListener;

    // --- TOÀN BỘ UseCase, Repository, LiveData, mOriginalSteps ĐÃ BỊ XÓA ---

    /**
     * Interface callback để thông báo cho View khi một Step cần được mở rộng.
     */
    public interface OnStepClickListener {
        void onStepExpandClicked(int stepId, int adapterPosition);
    }

    /**
     * Constructor "Dumb" MỚI của Adapter.
     * Chỉ nhận listener và khởi tạo một danh sách rỗng.
     * Nó không còn tự tải dữ liệu nữa.
     */
    public ExperimentDetailAdapter(OnStepClickListener listener) {
        this.stepClickListener = listener;
        this.mItems = new ArrayList<>(); // Khởi tạo list rỗng
    }

    /**
     * Phương thức PUBLIC MỚI.
     * ViewModel sẽ gọi hàm này để đẩy dữ liệu (đã xử lý xong) vào Adapter.
     */
    public void submitList(List<AdapterItem> newItems) {
        if (newItems == null) {
            newItems = new ArrayList<>();
        }

        Log.d("AdapterDebug", "submitList được gọi. Size mới: " + newItems.size() + " | Size cũ: " + this.mItems.size());
        this.mItems.clear();
        this.mItems.addAll(newItems);

        // Hàm này được gọi từ Activity/Fragment (trên Main Thread)
        // nên việc gọi notifyDataSetChanged() là an toàn.
        notifyDataSetChanged();
    }

    // --- TOÀN BỘ HÀM initializeFlatList VÀ FlatListCallback ĐÃ BỊ XÓA ---
    // (Vì chúng đã được chuyển sang ViewModel)

    @Override
    public int getItemCount() {
        // Thêm kiểm tra null để an toàn hơn
        return mItems == null ? 0 : mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getViewType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == AdapterConstants.TYPE_STEP) {
            View view = inflater.inflate(R.layout.item_step, parent, false);
            return new StepViewHolder(view);
        } else { // TYPE_LOG
            View view = inflater.inflate(R.layout.item_log, parent, false);
            return new LogViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AdapterItem item = mItems.get(position);

        if (item.getViewType() == AdapterConstants.TYPE_STEP) {
            StepViewHolder stepHolder = (StepViewHolder) holder;
            StepItemWrapper stepWrapper = (StepItemWrapper) item;
            stepHolder.bind(stepWrapper);

            stepHolder.itemView.setOnClickListener(v -> {
                int stepId = stepWrapper.getExperimentStep().getExperimentStepId();
                if (stepWrapper.isExpanded()) {
                    toggleStepCollapse(holder.getBindingAdapterPosition(), stepWrapper);
                } else {
                    if (stepClickListener != null) {
                        stepClickListener.onStepExpandClicked(stepId, holder.getBindingAdapterPosition());
                    }
                }
            });

        } else { // TYPE_LOG
            LogViewHolder logHolder = (LogViewHolder) holder;
            LogItemWrapper logWrapper = (LogItemWrapper) item;
            logHolder.bind(logWrapper);
        }
    }

    /**
     * Phương thức NỘI BỘ: Thu gọn một Step.
     * (Giữ nguyên, không thay đổi)
     */
    private void toggleStepCollapse(int position, StepItemWrapper stepWrapper) {
        List<LogEntry> logs = stepWrapper.getDownloadLog();

        if (logs != null && !logs.isEmpty()) {
            int logsToRemoveCount = logs.size();
            for (int i = 0; i < logsToRemoveCount; i++) {
                mItems.remove(position + 1);
            }
            stepWrapper.setExpanded(false);
            notifyItemChanged(position);
            notifyItemRangeRemoved(position + 1, logsToRemoveCount);
        }
    }

    /**
     * Phương thức PUBLIC: Chèn Logs vào Step.
     * (Giữ nguyên, không thay đổi)
     */
    public void insertLogsForStep(List<LogEntry> downloadedLogs, int stepPosition) {
        if (downloadedLogs == null || downloadedLogs.isEmpty()) return;

        StepItemWrapper stepWrapper = (StepItemWrapper) mItems.get(stepPosition);
        stepWrapper.setDownloadLog(downloadedLogs);

        List<AdapterItem> logsToInsert = new ArrayList<>();
        for (LogEntry entry : downloadedLogs) {
            logsToInsert.add(new LogItemWrapper(entry));
        }

        mItems.addAll(stepPosition + 1, logsToInsert);
        stepWrapper.setExpanded(true);

        notifyItemChanged(stepPosition);
        notifyItemRangeInserted(stepPosition + 1, logsToInsert.size());
    }
}