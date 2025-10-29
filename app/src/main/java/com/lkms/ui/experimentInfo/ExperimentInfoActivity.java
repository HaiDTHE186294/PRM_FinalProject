package com.lkms.ui.experimentInfo;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.lkms.R;
import com.lkms.data.repository.implement.java.ExperimentRepositoryImplJava;
import com.lkms.data.repository.implement.java.ProtocolRepositoryImplJava;
import com.lkms.data.repository.implement.java.UserRepositoryImplJava;
import com.lkms.domain.experimentdetail.GetExperimentDetailUseCase;

public class ExperimentInfoActivity extends AppCompatActivity {

    private ExperimentInfoViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_experiment_info);

        int experimentId = getIntent().getIntExtra("EXPERIMENT_ID", -1);

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

        viewModel.getExperiment().observe(this, experiment -> {
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
}
