package com.lkms.ui.project.projectdetail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lkms.R; // Giả định
import com.lkms.data.model.java.Experiment;
import java.util.ArrayList;
import java.util.List;

public class ExperimentAdapter extends RecyclerView.Adapter<ExperimentAdapter.ExperimentViewHolder> {

    private List<Experiment> experiments = new ArrayList<>();
    // Có thể thêm 1 ClickListener ở đây để mở chi tiết Experiment

    public void setExperiments(List<Experiment> newExperiments) {
        this.experiments.clear();
        this.experiments.addAll(newExperiments);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExperimentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false); // Tận dụng layout
        return new ExperimentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExperimentViewHolder holder, int position) {
        holder.bind(experiments.get(position));
    }

    @Override
    public int getItemCount() {
        return experiments.size();
    }

    static class ExperimentViewHolder extends RecyclerView.ViewHolder {
        TextView tvPrimary;
        TextView tvSecondary;

        public ExperimentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPrimary = itemView.findViewById(R.id.tvProjectTitle);
            tvSecondary = itemView.findViewById(R.id.tvProjectLeader);
        }

        public void bind(Experiment experiment) {
            tvPrimary.setText(experiment.getExperimentTitle());
            tvSecondary.setText("Status: " + experiment.getExperimentStatus());
        }
    }
}