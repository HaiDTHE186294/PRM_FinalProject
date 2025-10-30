package com.lkms.ui.protocol.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import com.lkms.R;
import com.lkms.data.model.java.ProtocolStep;
import java.util.List;

public class StepsAdapter extends RecyclerView.Adapter<StepsAdapter.StepViewHolder> {
    private final List<ProtocolStep> localStepsList;

    public StepsAdapter(List<ProtocolStep> stepsList) {
        this.localStepsList = stepsList;
    }

    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_create_step, parent, false);
        return new StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        holder.bind(localStepsList.get(position));
    }

    @Override
    public int getItemCount() {
        return localStepsList.size();
    }

    class StepViewHolder extends RecyclerView.ViewHolder {
        TextView stepOrderText;
        TextInputEditText stepInstructionEdit;
        ImageButton removeStepButton;

        StepViewHolder(@NonNull View itemView) {
            super(itemView);
            stepOrderText = itemView.findViewById(R.id.text_view_step_order);
            stepInstructionEdit = itemView.findViewById(R.id.edit_text_step_instruction);
            removeStepButton = itemView.findViewById(R.id.button_remove_step);

            removeStepButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    localStepsList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, localStepsList.size());
                }
            });

            stepInstructionEdit.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        localStepsList.get(position).setInstruction(s.toString());
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        void bind(ProtocolStep step) {
            stepOrderText.setText(String.format("%d.", getAdapterPosition() + 1));
            //step.setStepOrder(getAdapterPosition() + 1); // Loại bỏ dòng này để việc gán stepOrder chỉ diễn ra khi lưu
            stepInstructionEdit.setText(step.getInstruction());
        }
    }
}
