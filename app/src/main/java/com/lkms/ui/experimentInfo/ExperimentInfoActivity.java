package com.lkms.ui.experimentInfo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.lkms.R;
import com.lkms.data.model.java.combine.ExperimentUserProjectProtocol;
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums;
import com.lkms.data.repository.implement.java.ExperimentRepositoryImplJava;
import com.lkms.data.repository.implement.java.ProtocolRepositoryImplJava;
import com.lkms.data.repository.implement.java.UserRepositoryImplJava;
import com.lkms.domain.experimentdetail.GetExperimentDetailUseCase;
import com.lkms.domain.report.CompleteExperimentUseCase;
import com.lkms.domain.report.GetExperimentReportUseCase;
import com.lkms.ui.addmember.AddMemberActivity;
import com.lkms.ui.experimentdetail.ExperimentDetailActivity;
import com.lkms.util.PdfGenerator;

public class ExperimentInfoActivity extends AppCompatActivity {

    private ExperimentInfoViewModel viewModel;
    private int experimentId = -1;
    private ExperimentUserProjectProtocol mExperimentData;
    private PdfGenerator pdfGenerator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_experiment_info);

        experimentId = getIntent().getIntExtra("experimentId", -1);
        Log.d("ExperimentInfoActivity", "experimentId: " + experimentId);

        GetExperimentDetailUseCase useCase = new GetExperimentDetailUseCase(new ExperimentRepositoryImplJava(), new UserRepositoryImplJava(), new ProtocolRepositoryImplJava());
        CompleteExperimentUseCase completeUseCase = new CompleteExperimentUseCase(new ExperimentRepositoryImplJava());
        GetExperimentReportUseCase reportUseCase = new GetExperimentReportUseCase(new ExperimentRepositoryImplJava());

        viewModel = new ViewModelProvider(this, new ExperimentInfoViewModelFactory(useCase, completeUseCase,reportUseCase))
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

            if (LKMSConstantEnums.ExperimentStatus.COMPLETED.toString()
                    .equals(experiment.getExperiment().getExperimentStatus())) {

                setButtonToExportPdf(btnCompleteExperiment);

            } else {
                // Nếu chưa, nó là nút "Complete"
                btnCompleteExperiment.setText("Complete Experiment"); // Đảm bảo tên đúng
                btnCompleteExperiment.setOnClickListener(v -> onCompleteExperimentClicked());
            }
        });

        viewModel.getError().observe(this, error ->
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        );

        viewModel.getCompletionSuccess().observe(this, isSuccess -> {
            if (isSuccess) {
                // Chỉ hiển thị thông báo và đổi nút
                // KHÔNG finish() Activity!
                Toast.makeText(this, "Experiment completed successfully!", Toast.LENGTH_SHORT).show();

                // Cập nhật lại dữ liệu local (nếu cần)
                if (mExperimentData != null) {
                    mExperimentData.getExperiment()
                            .setExperimentStatus(LKMSConstantEnums.ExperimentStatus.COMPLETED.toString());
                }

                // Gọi hàm đổi nút
                setButtonToExportPdf(btnCompleteExperiment);
            }
        });

        viewModel.loadExperiment(experimentId);
    }

    /**
     * Được gọi khi người dùng nhấn nút "Add Member"
     */
    private void onAddMemberClicked() {
        // 1. Kiểm tra để chắc chắn rằng chúng ta đã có experimentId hợp lệ.
        //    Biến 'experimentId' đã được bạn lấy từ Intent ở trong onCreate.
        if (experimentId != -1) {
            // 2. Tạo một Intent để mở AddMemberActivity.
            Intent intent = new Intent(ExperimentInfoActivity.this, AddMemberActivity.class);

            // 3. Đính kèm experimentId vào Intent.
            //    Key "EXPERIMENT_ID" phải khớp với key mà bạn dùng để nhận ở AddMemberActivity.
            intent.putExtra("EXPERIMENT_ID", experimentId);

            // 4. Khởi chạy Activity mới.
            startActivity(intent);
        } else {
            // 5. Thông báo lỗi nếu không tìm thấy experimentId.
            Toast.makeText(this, "Error: Could not find Experiment ID to add members.", Toast.LENGTH_SHORT).show();
        }
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
        // 1. Kiểm tra experimentId như cũ
        experimentId = getIntent().getIntExtra("experimentId", -1);
        if (experimentId == -1) {
            Toast.makeText(this, "Invalid experiment ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Tạo layout cho CheckBox
        // Inflate layout tùy chỉnh
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_confirm_complete, null);
        CheckBox cbConfirm = dialogView.findViewById(R.id.cb_confirm_complete);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Complete Experiment?");
        builder.setMessage("Are you sure you want to complete this experiment? This action cannot be undone.");

        // Gắn layout CheckBox vào dialog
        builder.setView(dialogView);

        // 4. Nút "Cancel" (Nút tiêu cực)
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // Chỉ cần đóng dialog
            dialog.dismiss();
        });

        // 5. Nút "Complete" (Nút tích cực)
        builder.setPositiveButton("Complete", (dialog, which) -> {
            // --- CHỈ CHẠY KHI NGƯỜI DÙNG NHẤN "COMPLETE" ---
            // Gọi ViewModel
            viewModel.completeExperiment(experimentId);
            // --- HẾT LOGIC XỬ LÝ ---
        });

        // 6. Tạo và hiển thị Dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // 7. Vô hiệu hóa (Disable) nút "Complete" LÚC ĐẦU
        // Nút này chỉ được bật (enable) khi CheckBox được tick
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setEnabled(false);

        // 8. Thêm Listener cho CheckBox
        cbConfirm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Bật/tắt nút "Complete" dựa theo trạng thái của CheckBox
            positiveButton.setEnabled(isChecked);
        });
    }

    private void setButtonToExportPdf(Button button) {
        button.setText("Export to PDF");
        // Đặt OnClickListener mới
        button.setOnClickListener(v -> onExportPdfClicked());
    }

    private void onExportPdfClicked() {
        experimentId = getIntent().getIntExtra("experimentId", -1);
        if (experimentId == -1) {
            Toast.makeText(this, "Invalid experiment ID", Toast.LENGTH_SHORT).show();
            return;
        }
        // Thực hiện hành động xuất PDF
        Log.d("ExperimentInfoActivity", "Start Exporting PDF for experiment ID: " + experimentId);
        viewModel.generateReport(experimentId);
    }

    private void observeViewModel() {
        // 1. Lắng nghe dữ liệu thành công
        viewModel.reportData.observe(this, reportData -> {
            if (reportData != null) {
                Toast.makeText(this, "Đã lấy dữ liệu thành công, đang tạo PDF...", Toast.LENGTH_SHORT).show();

                new Thread(() -> {
                    pdfGenerator.createPdfReport(reportData);
                }).start();
            }
        });

        // 2. Lắng nghe lỗi
        viewModel.error.observe(this, error -> {
            if (error != null) {
                Log.e("ExperimentInfoActivity", "Error: " + error);
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}

