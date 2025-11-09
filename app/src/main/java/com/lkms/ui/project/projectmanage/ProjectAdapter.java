package com.lkms.ui.project.projectmanage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lkms.R; // Giả định bạn có R
import com.lkms.data.model.java.Project;
import java.util.ArrayList;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    private List<Project> projects = new ArrayList<>();
    private final OnProjectClickListener listener;

    public interface OnProjectClickListener {
        void onProjectClick(Project project);
    }

    public ProjectAdapter(OnProjectClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projects.get(position);
        holder.bind(project, listener);
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    public void setProjects(List<Project> newProjects) {
        this.projects.clear();
        this.projects.addAll(newProjects);
        notifyDataSetChanged();
    }

    // --- ViewHolder ---
    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvProjectTitle;
        TextView tvProjectLeader;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProjectTitle = itemView.findViewById(R.id.tvProjectTitle);
            tvProjectLeader = itemView.findViewById(R.id.tvProjectLeader);
        }

        public void bind(Project project, OnProjectClickListener listener) {
            tvProjectTitle.setText(project.getProjectTitle());
            tvProjectLeader.setText("Leader ID: " + project.getProjectLeaderId());

            itemView.setOnClickListener(v -> listener.onProjectClick(project));
        }
    }
}