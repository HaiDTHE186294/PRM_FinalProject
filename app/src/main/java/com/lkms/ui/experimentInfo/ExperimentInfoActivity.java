package com.lkms.ui.experimentInfo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.lkms.R;
import com.lkms.data.model.java.combine.ExperimentUserProjectProtocol;
import com.lkms.data.repository.implement.java.ExperimentRepositoryImplJava;
import com.lkms.data.repository.implement.java.ProtocolRepositoryImplJava;
import com.lkms.data.repository.implement.java.UserRepositoryImplJava;
import com.lkms.domain.experimentdetail.GetExperimentDetailUseCase;
import com.lkms.ui.experimentdetail.ExperimentDetailActivity;

public class ExperimentInfoActivity extends AppCompatActivity {

    private ExperimentInfoViewModel viewModel;
    private int experimentId = -1;
    private ExperimentUserProjectProtocol mExperimentData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_experiment_info);

        experimentId = getIntent().getIntExtra("experimentId", -1);
        Log.d("ExperimentInfoActivity", "experimentId: " + experimentId);

        GetExperimentDetailUseCase useCase = new GetExperimentDetailUseCase(new ExperimentRepositoryImplJava(), new UserRepositoryImplJava(), new ProtocolRepositoryImplJava());
        viewModel = new ViewModelProvider(this, new ExperimentInfoViewModelFactory(useCase))
                .get(ExperimentInfoViewModel.class);

        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvObjective = findViewById(R.id.tvObjective);
        TextView tvProject = findViewById(R.id.tvProject);
        TextView tvCreator = findViewById(R.id.tvCreator);
        TextView tvProtocol = findViewById(R.id.tvProtocolTitle);
        TextView tvStartDate = findViewById(R.id.tvStartDate);
        TextView tvFinishDate = findViewById(R.id.tvFinishDate);

        // Khai báo Buttons
        Button btnAddMember = findViewById(R.id.btnAddMember);
        Button btnViewSteps = findViewById(R.id.btnViewSteps);
        Button btnCompleteExperiment = findViewById(R.id.btnCompleteExperiment);

        // Thiết lập OnClickListener
        btnAddMember.setOnClickListener(v -> onAddMemberClicked());
        btnViewSteps.setOnClickListener(v -> onViewStepsClicked());
        btnCompleteExperiment.setOnClickListener(v -> onCompleteExperimentClicked());

        viewModel.getExperiment().observe(this, experiment -> {
            this.mExperimentData = experiment;

            tvTitle.setText(experiment.getExperiment().getExperimentTitle());
            tvObjective.setText(experiment.getExperiment().getObjective());
            tvProject.setText(experiment.getProject().getProjectTitle());
            tvCreator.setText(experiment.getUser().getName());
            tvProtocol.setText(experiment.getProtocol().getProtocolTitle());
            tvStartDate.setText(experiment.getExperiment().getStartDate());
            tvFinishDate.setText(experiment.getExperiment().getFinishDate());
        });

        viewModel.getError().observe(this, error ->
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        );

        viewModel.loadExperiment(experimentId);
    }

    /**
     * Được gọi khi người dùng nhấn nút "Add Member"
     */
    private void onAddMemberClicked() {
        // TODO: Hiển thị dialog/activity để chọn và thêm thành viên
        Toast.makeText(this, "Add Member Clicked", Toast.LENGTH_SHORT).show();
    }

    /**
     * Được gọi khi người dùng nhấn nút "View Steps"
     */
    private void onViewStepsClicked() {

        if (mExperimentData == null) {
            // You can show a Toast message here if you want
            Toast.makeText(this, "Data not loaded yet...", Toast.LENGTH_SHORT).show();
            return;
        }
        experimentId = getIntent().getIntExtra("experimentId", -1);
        ExperimentUserProjectProtocol experiment = (ExperimentUserProjectProtocol) mExperimentData;
        Intent intent = new Intent(this, ExperimentDetailActivity.class);
        intent.putExtra("experimentId", experimentId);
        intent.putExtra("title", experiment.getExperiment().getExperimentTitle());
        intent.putExtra("status", experiment.getExperiment().getExperimentStatus());
        intent.putExtra("objective", experiment.getExperiment().getObjective());
        startActivity(intent);
    }

    /**
     * Được gọi khi người dùng nhấn nút "Complete Experiment"
     */
    private void onCompleteExperimentClicked() {
        // TODO: Hiển thị dialog xác nhận. Nếu đồng ý, gọi viewModel.completeExperiment()
        Toast.makeText(this, "Complete Experiment", Toast.LENGTH_SHORT).show();

    }
}

