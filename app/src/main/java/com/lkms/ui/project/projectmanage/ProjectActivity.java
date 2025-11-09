package com.lkms.ui.project.projectmanage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.TextView; // 👈 THÊM: Để xử lý Empty State

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lkms.R;
import com.lkms.ui.project.projectdetail.ProjectDetailActivity;
import com.lkms.util.AuthHelper;

public class ProjectActivity extends AppCompatActivity {

    private ProjectViewModel viewModel;
    private RecyclerView recyclerView;
    private ProjectAdapter projectAdapter;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddProject;
    private TextView tvEmptyState;
    private int currentUserId;
    private int currentUserRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, Math.max(systemBars.bottom, ime.bottom));
            return insets;
        });

        currentUserId = AuthHelper.getLoggedInUserId(getApplicationContext());
        currentUserRole = AuthHelper.getLoggedInUserRole(getApplicationContext());

        if (currentUserId == -1) {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        ProjectViewModelFactory factory = new ProjectViewModelFactory();
        viewModel = new ViewModelProvider(this, factory).get(ProjectViewModel.class);

        initViews();
        setupViews();
        setupObservers();
        setupClickListeners();

        viewModel.loadMyProjects(currentUserId);
    }

    private void initViews() {
        progressBar = findViewById(R.id.progressBar);
        fabAddProject = findViewById(R.id.fabAddProject);
        recyclerView = findViewById(R.id.recyclerViewProjects);
        tvEmptyState = findViewById(R.id.tvEmptyState);
    }

    private void setupViews() {
        projectAdapter = new ProjectAdapter(project -> {
            Intent intent = new Intent(ProjectActivity.this, ProjectDetailActivity.class);
            intent.putExtra("PROJECT_ID", project.getProjectId());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(projectAdapter);

        if (currentUserRole == 0 || currentUserRole == 1) {
            fabAddProject.setVisibility(View.VISIBLE);
        } else {
            fabAddProject.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        fabAddProject.setOnClickListener(v -> {
            showCreateProjectDialog();
        });
    }

    private void showCreateProjectDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_project, null);
        final EditText input = dialogView.findViewById(R.id.etProjectTitle);

        new AlertDialog.Builder(ProjectActivity.this)
                .setTitle("Create Project")
                .setView(dialogView)
                .setPositiveButton("Create", (dialog, which) -> {
                    String newProjectTitle = input.getText().toString().trim();

                    if (newProjectTitle.isEmpty()) {
                        Toast.makeText(ProjectActivity.this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
                    } else {
                        viewModel.createProject(newProjectTitle, currentUserId);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.cancel();
                })
                .show();
    }

    private void setupObservers() {
        viewModel.isLoading.observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.errorMessage.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.myProjects.observe(this, projects -> {
            boolean isEmpty = projects == null || projects.isEmpty();

            // Cập nhật Adapter
            projectAdapter.setProjects(projects);
            if (isEmpty) {
                recyclerView.setVisibility(View.GONE);
                tvEmptyState.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                tvEmptyState.setVisibility(View.GONE);
            }
        });

        viewModel.newProjectId.observe(this, newId -> {
            if (newId != null) {
                Toast.makeText(this, "Create success with Project ID: " + newId, Toast.LENGTH_SHORT).show();
                viewModel.loadMyProjects(currentUserId);
            }
        });
    }
}