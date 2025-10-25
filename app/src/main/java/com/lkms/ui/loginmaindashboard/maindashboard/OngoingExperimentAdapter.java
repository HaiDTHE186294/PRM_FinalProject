package com.lkms.ui.loginmaindashboard.maindashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lkms.R;
import com.lkms.data.model.java.Experiment;

import java.util.List;

public class OngoingExperimentAdapter extends RecyclerView.Adapter<OngoingExperimentAdapter.ViewHolder> {

    private final List<Experiment> experimentList;

    public OngoingExperimentAdapter(List<Experiment> experimentList) {
        this.experimentList = experimentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_experiment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Experiment experiment = experimentList.get(position);
        holder.tvExperimentName.setText(experiment.getExperimentTitle());
        holder.tvExperimentStatus.setText(experiment.getExperimentStatus());
    }

    @Override
    public int getItemCount() {
        return experimentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvExperimentName, tvExperimentStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExperimentName = itemView.findViewById(R.id.tvExperimentName);
            tvExperimentStatus = itemView.findViewById(R.id.tvExperimentStatus);
        }
    }
}

