package com.lkms.ui.loginmaindashboard.maindashboard;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lkms.R;
import com.lkms.data.model.java.Experiment;
import com.lkms.ui.experimentInfo.ExperimentInfoActivity;

import java.util.List;

public class OngoingExperimentAdapter extends RecyclerView.Adapter<OngoingExperimentAdapter.ViewHolder> {

    private final List<Experiment> experimentList;
    private final Context context;

    public OngoingExperimentAdapter(Context context, List<Experiment> experimentList) {
        this.context = context;
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

        // 🟢 Xử lý khi click vào 1 item
        holder.itemView.setOnClickListener(v -> {
            int experimentId = experiment.getExperimentId();

            Log.d("EXPERIMENT_CLICK", "Clicked Experiment ID: " + experimentId);

            if (experimentId != -1) {
                Intent intent = new Intent(context, ExperimentInfoActivity.class);
                intent.putExtra("experimentId", experimentId);
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Experiment ID không hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        });
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

