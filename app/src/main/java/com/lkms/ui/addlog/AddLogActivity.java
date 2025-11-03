// Đặt trong package: com.lkms.ui.addlog
package com.lkms.ui.addlog;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider; // Import quan trọng

import com.lkms.R;
import com.lkms.data.repository.IExperimentRepository;
import com.lkms.data.repository.implement.java.ExperimentRepositoryImplJava;
import com.lkms.databinding.ActivityAddLogBinding;
import com.lkms.domain.logentry.AddLogUseCase;

import java.io.File;

public class AddLogActivity extends AppCompatActivity {

    private ActivityAddLogBinding binding;
    private AddLogViewModel viewModel; // Thay vì UseCase
    private ActivityResultLauncher<String> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAddLogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // (Code xử lý WindowInsets)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- 1. Lấy dữ liệu Intent ---
        int stepId = getIntent().getIntExtra("stepId", -1);
        if (stepId == -1) {
            Toast.makeText(this, "Invalid Step ID. Cannot proceed.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String instruction = getIntent().getStringExtra("instruction");
        binding.tvProtocolInstruction.setText(instruction);


        // --- 2. Khởi tạo Dependencies ---
        IExperimentRepository repository = new ExperimentRepositoryImplJava();
        AddLogUseCase addLogUseCase = new AddLogUseCase(repository);

        // --- 3. Khởi tạo ViewModel ---
        AddLogViewModelFactory factory = new AddLogViewModelFactory(
                getApplication(),
                addLogUseCase
        );
        viewModel = new ViewModelProvider(this, factory).get(AddLogViewModel.class);

        // Gửi stepId cho ViewModel
        viewModel.setStepId(stepId);

        // --- 4. Khởi tạo UI Components ---
        initFilePickerLauncher();
        initListeners();
        initObservers(); // Quan trọng: Bắt đầu quan sát ViewModel
    }

    /**
     * Khởi tạo trình chọn file.
     * Khi có kết quả, chỉ cần báo cho ViewModel.
     */
    private void initFilePickerLauncher() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        viewModel.onFileSelected(uri);
                    }
                }
        );
    }

    /**
     * Gán sự kiện click cho các button.
     * Chỉ cần báo cho ViewModel.
     */
    private void initListeners() {
        binding.btnAttachFile.setOnClickListener(v -> {
            filePickerLauncher.launch("*/*");
        });

        binding.btnUpload.setOnClickListener(v -> {
            String content = binding.etLogEntryContent.getText().toString().trim();
            viewModel.onUploadClicked(content);
        });
    }

    /**
     * Quan sát (observe) các LiveData từ ViewModel
     * để cập nhật UI tương ứng.
     */
    private void initObservers() {
        // 1. Quan sát file đã chọn
        viewModel.getSelectedFile().observe(this, file -> {
            if (file != null) {
                binding.tvSelectedFileName.setText(file.getName());
                binding.tvSelectedFileName.setVisibility(View.VISIBLE);
            } else {
                binding.tvSelectedFileName.setVisibility(View.GONE);
            }
        });

        // 2. Quan sát trạng thái upload
        viewModel.getUploadState().observe(this, state -> {
            if (state instanceof UploadState.Loading) {
                // Đang tải
                binding.btnUpload.setEnabled(false);
                binding.btnUpload.setText("Uploading...");
                binding.etLogEntryContent.setError(null);

            } else if (state instanceof UploadState.Success) {
                // Thành công
                int newId = ((UploadState.Success) state).getNewLogId();
                Toast.makeText(this, "Log added successfully! ID: " + newId, Toast.LENGTH_LONG).show();
                setResult(RESULT_OK);
                finish();

            } else if (state instanceof UploadState.Error) {
                // Lỗi
                String error = ((UploadState.Error) state).getMessage();
                // Phân biệt lỗi do validation hay lỗi upload
                if (error.equals("Content is required")) {
                    binding.etLogEntryContent.setError(error);
                } else {
                    Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
                }
                binding.btnUpload.setEnabled(true);
                binding.btnUpload.setText("UPLOAD");
                // Đặt lại trạng thái để người dùng có thể thử lại
                viewModel.resetStateToIdle();

            } else {
                // Trạng thái Idle
                binding.btnUpload.setEnabled(true);
                binding.btnUpload.setText("UPLOAD");
            }
        });
    }
}