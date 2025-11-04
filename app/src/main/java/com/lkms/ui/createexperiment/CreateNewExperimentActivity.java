package com.lkms.ui.createexperiment;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.lkms.R;
import com.lkms.data.model.java.Project;
import com.lkms.util.AuthHelper;

import java.util.ArrayList;
import java.util.List;

public class CreateNewExperimentActivity extends AppCompatActivity {

    private CreateNewExperimentViewModel viewModel;
    private EditText etExperimentTitle, etExperimentObjective;
    private TextView tvBasedOnProtocol;
    private Spinner spinnerProject;
    private Button btnCreate;
    private ProgressBar progressBar;
    private int selectedProtocolId = -1;
    private List<Project> projectList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_experiment);

        selectedProtocolId = getIntent().getIntExtra("SELECTED_PROTOCOL_ID", -1);
        if (selectedProtocolId == -1) {
            Toast.makeText(this, "Lỗi: Không nhận được Protocol ID.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        CreateNewExperimentViewModelFactory factory = new CreateNewExperimentViewModelFactory();
        viewModel = new ViewModelProvider(this, factory).get(CreateNewExperimentViewModel.class);

        setupViews();
        observeViewModel();

        viewModel.loadInitialData(selectedProtocolId);
    }

    private void setupViews() {
        etExperimentTitle = findViewById(R.id.etExperimentTitle);
        etExperimentObjective = findViewById(R.id.etExperimentObjective);
        tvBasedOnProtocol = findViewById(R.id.tvBasedOnProtocol);
        spinnerProject = findViewById(R.id.spinnerProject);
        btnCreate = findViewById(R.id.btnCreate);
        progressBar = findViewById(R.id.progressBar);

        ArrayAdapter<String> projectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        projectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProject.setAdapter(projectAdapter);

        btnCreate.setOnClickListener(v -> {
            String title = etExperimentTitle.getText().toString();
            String objective = etExperimentObjective.getText().toString();

            int selectedPosition = spinnerProject.getSelectedItemPosition();

            if (selectedPosition < 0 || projectList.isEmpty() || selectedPosition >= projectList.size()) {
                Toast.makeText(this, "Vui lòng chọn một dự án", Toast.LENGTH_SHORT).show();
                return;
            }

            Project selectedProject = projectList.get(selectedPosition);

            int userId = AuthHelper.getLoggedInUserId(this);

            viewModel.createExperiment(title, objective, selectedProject, userId);
        });
    }

    private void observeViewModel() {
        viewModel.isLoading.observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnCreate.setEnabled(!isLoading);
        });

        viewModel.error.observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.creationSuccess.observe(this, isSuccess -> {
            if (isSuccess) {
                Toast.makeText(this, "Tạo thí nghiệm thành công!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        viewModel.protocol.observe(this, protocol -> {
            if (protocol != null) {
                // Sửa ở đây: Dùng getProtocolTitle() cho Protocol
                tvBasedOnProtocol.setText(protocol.getProtocolTitle());
                etExperimentTitle.setText("Thí nghiệm dựa trên: " + protocol.getProtocolTitle());
            }
        });

        viewModel.projects.observe(this, projects -> {
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerProject.getAdapter();
            adapter.clear();

            if (projects != null && !projects.isEmpty()) {
                this.projectList = projects;

                List<String> projectTitles = new ArrayList<>();
                for (Project project : projects) {
                    // ======================= SỬA Ở ĐÂY =======================
                    //   Sử dụng getProjectTitle() thay vì getTitle()
                    projectTitles.add(project.getProjectTitle());
                    // ========================================================
                }

                adapter.addAll(projectTitles);

            } else {
                this.projectList.clear();
            }

            adapter.notifyDataSetChanged();
        });
    }
}
