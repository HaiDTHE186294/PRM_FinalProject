package com.lkms.ui.project.projectdetail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lkms.R;
import com.lkms.data.model.java.Experiment;
import java.util.ArrayList;
import java.util.List;

public class ExperimentAdapter extends RecyclerView.Adapter<ExperimentAdapter.ExperimentViewHolder> {

    private List<Experiment> experiments = new ArrayList<>();
    private final OnExperimentClickListener listener;

    public interface OnExperimentClickListener {
        void onExperimentClick(Experiment experiment);
    }

    public ExperimentAdapter(OnExperimentClickListener listener) {
        this.listener = listener;
    }

    public void setExperiments(List<Experiment> newExperiments) {
        this.experiments.clear();
        this.experiments.addAll(newExperiments);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExperimentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
        return new ExperimentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExperimentViewHolder holder, int position) {
        holder.bind(experiments.get(position), listener);
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

        public void bind(Experiment experiment, OnExperimentClickListener listener) {
            tvPrimary.setText(experiment.getExperimentTitle());
            tvSecondary.setText("Status: " + experiment.getExperimentStatus());
            itemView.setOnClickListener(v -> listener.onExperimentClick(experiment));
        }
    }
}