package com.lkms.ui.project.projectmanage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.TextView; // 👈 THÊM: Để xử lý Empty State

import androidx.appcompat.app.AppCompatActivity;
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
    private TextView tvEmptyState; // 👈 THÊM: Để hiển thị khi danh sách trống

    private int currentUserId;
    private int currentUserRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        currentUserId = AuthHelper.getLoggedInUserId(getApplicationContext());
        currentUserRole = AuthHelper.getLoggedInUserRole(getApplicationContext());

        // 🔥 SỬA LỖI 2: KIỂM TRA ĐĂNG NHẬP
        if (currentUserId == -1) {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        ProjectViewModelFactory factory = new ProjectViewModelFactory();
        viewModel = new ViewModelProvider(this, factory).get(ProjectViewModel.class);

        initViews(); // Đổi tên thành initViews để dễ đọc hơn
        setupViews();
        setupObservers();
        setupClickListeners();

        viewModel.loadMyProjects(currentUserId);
    }

    // Đổi tên thành initViews để rõ ràng hơn
    private void initViews() {
        progressBar = findViewById(R.id.progressBar);
        fabAddProject = findViewById(R.id.fabAddProject);
        recyclerView = findViewById(R.id.recyclerViewProjects);
        tvEmptyState = findViewById(R.id.tvEmptyState);
    }

    private void setupViews() {
        // Khởi tạo Adapter và RecyclerView
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
        // ... (Giữ nguyên logic tạo Dialog)
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

            // 🔥 SỬA LỖI 3: Xử lý Trạng thái Trống (Empty State)
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
                // Tải lại danh sách để hiển thị dự án mới
                viewModel.loadMyProjects(currentUserId);
            }
        });
    }
}