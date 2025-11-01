package com.lkms.ui.project.projectmanage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.app.AlertDialog;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lkms.R; // Giả định
import com.lkms.ui.project.projectdetail.ProjectDetailActivity;

// import com.lkms.ui.projectdetail.ProjectDetailActivity; // Màn hình chi tiết (chưa tạo)

/**
 * Màn hình chính cho UC 18: Manage Project.
 * Hiển thị danh sách dự án của người dùng.
 */
public class ProjectActivity extends AppCompatActivity {

    private ProjectViewModel viewModel;
    private RecyclerView recyclerView;
    private ProjectAdapter projectAdapter;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddProject;

    // Giả định User ID và Role được truyền qua Intent khi đăng nhập
    private int currentUserId = 1; // TODO: Lấy ID thật từ Intent
    private String currentUserRole = "Lab Manager"; // TODO: Lấy Role thật từ Intent (Dùng Enum)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        // 1. Khởi tạo ViewModel bằng Factory (để inject UseCase)
        ProjectViewModelFactory factory = new ProjectViewModelFactory();
        viewModel = new ViewModelProvider(this, factory).get(ProjectViewModel.class);

        // 2. Setup View
        setupViews();

        // 3. Setup Observers để lắng nghe LiveData từ ViewModel
        setupObservers();

        // 4. Setup Click Listeners
        setupClickListeners();

        // 5. Yêu cầu ViewModel tải dữ liệu ban đầu
        viewModel.loadMyProjects(currentUserId);
    }

    private void setupViews() {
        progressBar = findViewById(R.id.progressBar);
        fabAddProject = findViewById(R.id.fabAddProject);
        recyclerView = findViewById(R.id.recyclerViewProjects);

        // Setup Adapter
        projectAdapter = new ProjectAdapter(project -> {
            // UC 18: "ấn vào chuyển sang màn Project Detail"
             Intent intent = new Intent(ProjectActivity.this, ProjectDetailActivity.class);
             intent.putExtra("PROJECT_ID", project.getProjectId());
             startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(projectAdapter);

        // UC 18: "Lab manager có thêm nút để khởi tạo Project mới"
        if ("Lab Manager".equals(currentUserRole)) {
            fabAddProject.setVisibility(View.VISIBLE);
        } else {
            fabAddProject.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        fabAddProject.setOnClickListener(v -> {
            // "hiển thị form tạo Project"
            showCreateProjectDialog();
        });
    }

    /**
     * Hiển thị AlertDialog (form) để tạo dự án mới (UC 18)
     */
    private void showCreateProjectDialog() {
        // 1. Inflate layout (nạp layout dialog_create_project.xml)
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_project, null);
        final EditText input = dialogView.findViewById(R.id.etProjectTitle);

        // 2. Xây dựng AlertDialog
        new AlertDialog.Builder(ProjectActivity.this)
                .setTitle("Tạo Dự án mới")
                .setView(dialogView) // Gắn layout vào dialog
                .setPositiveButton("Tạo", (dialog, which) -> {
                    // Xử lý khi người dùng nhấn "Tạo"
                    String newProjectTitle = input.getText().toString().trim();

                    // Validation (Kiểm tra)
                    if (newProjectTitle.isEmpty()) {
                        Toast.makeText(ProjectActivity.this, "Tiêu đề không được để trống", Toast.LENGTH_SHORT).show();
                    } else {
                        // Gọi ViewModel để tạo
                        viewModel.createProject(newProjectTitle, currentUserId);
                    }
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    dialog.cancel();
                })
                .show();
    }

    private void setupObservers() {
        // Lắng nghe trạng thái loading
        viewModel.isLoading.observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Lắng nghe lỗi
        viewModel.errorMessage.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        });

        // UC 18: "Hiển thị những project bản thân tham gia"
        viewModel.myProjects.observe(this, projects -> {
            if (projects != null) {
                projectAdapter.setProjects(projects);
            }
        });

        // Lắng nghe khi project mới được tạo thành công
        viewModel.newProjectId.observe(this, newId -> {
            if (newId != null) {
                Toast.makeText(this, "Tạo thành công Project ID: " + newId, Toast.LENGTH_SHORT).show();
                viewModel.loadMyProjects(currentUserId);
            }
        });
    }
}