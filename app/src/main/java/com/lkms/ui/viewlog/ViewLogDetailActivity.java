package com.lkms.ui.viewlog;

import android.os.Bundle;
import android.util.Log;
import android.view.View; // MỚI
import android.widget.TextView; // MỚI
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentContainerView; // MỚI
import androidx.lifecycle.ViewModelProvider;

import com.lkms.R;
import com.lkms.data.model.java.LogEntry; // MỚI (Giả định import)
import com.lkms.data.repository.IExperimentRepository;
import com.lkms.data.repository.implement.java.ExperimentRepositoryImplJava;

import java.text.SimpleDateFormat; // MỚI
import java.util.Date; // MỚI
import java.util.Locale; // MỚI

public class ViewLogDetailActivity extends AppCompatActivity {

    private int logId;
    private ViewLogDetailViewModel viewModel;
    private IExperimentRepository repository;

    // Khai báo View
    private TextView textViewLogTime;
    private TextView textViewContent;
    private FragmentContainerView fragmentContainerFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_log_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ánh xạ View
        textViewLogTime = findViewById(R.id.textViewLogTime);
        textViewContent = findViewById(R.id.textViewContent);
        fragmentContainerFiles = findViewById(R.id.fragmentContainerFiles);

        // Khởi tạo Repository và ViewModel (Code của bạn đã có)
        repository = new ExperimentRepositoryImplJava();
        ViewLogDetailViewModelFactory factory = new ViewLogDetailViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(ViewLogDetailViewModel.class);

        // Lấy logId từ Intent
        if (getIntent() != null && getIntent().hasExtra("logId")) {
            logId = getIntent().getIntExtra("logId", -1);
        }

        // Thiết lập Observer cho ViewModel
        observeViewModel();

        // Tải dữ liệu nếu logId hợp lệ
        if (logId != -1) {
            // Ra lệnh cho ViewModel tải dữ liệu
            viewModel.loadLogData(logId);
        } else {
            Log.e("ViewLogDetailActivity", "Không tìm thấy logId trong intent");
            Toast.makeText(this, "Lỗi: Không tìm thấy Log ID.", Toast.LENGTH_LONG).show();
            finish(); // Đóng Activity nếu không có ID
        }
    }

    /**
     * Lắng nghe các thay đổi trạng thái từ ViewModel
     */
    private void observeViewModel() {
        viewModel.uiState.observe(this, state -> {
            if (state instanceof LogUiState.Loading) {
                // Hiển thị trạng thái đang tải
                textViewLogTime.setText("Đang tải...");
                textViewContent.setText("Đang tải...");
                fragmentContainerFiles.setVisibility(View.GONE); // Ẩn container file

            } else if (state instanceof LogUiState.Success) {
                // Lấy dữ liệu thành công
                LogEntry log = ((LogUiState.Success) state).logEntry;
                String fileName = ((LogUiState.Success) state).fileName;

                // Cập nhật UI
                textViewLogTime.setText("Log Time: " + log.getLogTime());
                textViewContent.setText(log.getContent());

                // Xử lý logic hiển thị file
                if (fileName != null) {
                    // CÓ FILE: Hiển thị Fragment container
                    fragmentContainerFiles.setVisibility(View.VISIBLE);
                    if (getSupportFragmentManager().findFragmentById(R.id.fragmentContainerFiles) == null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragmentContainerFiles, new ViewFileFragment())
                                .commit();
                    }
                } else {
                    // KHÔNG CÓ FILE: Ẩn Fragment container và ghi log
                    fragmentContainerFiles.setVisibility(View.GONE);
                    Log.d("Activity Debug", "Log này không có File");
                }

            } else if (state instanceof LogUiState.Error) {
                // Hiển thị lỗi
                String errorMsg = ((LogUiState.Error) state).message;
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                textViewLogTime.setText("Lỗi");
                textViewContent.setText(errorMsg);
                fragmentContainerFiles.setVisibility(View.GONE);
            }
        });
    }

}