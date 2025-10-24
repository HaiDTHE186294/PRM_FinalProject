// File: ProtocolDetailActivity.java
package com.lkms.ui.protocol;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat; // Dùng để lấy màu từ file colors.xml một cách an toàn.
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums.ProtocolApproveStatus; // Để code có thể "hiểu" được Enum ProtocolApproveStatus là gì.


// ✅ BƯỚC 1: THÊM IMPORT CHO FLOATINGACTIONBUTTON
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lkms.R;
// Giả sử bạn sẽ tạo một Activity tên là CreateExperimentActivity
// import com.lkms.ui.experiment.CreateExperimentActivity;
import com.lkms.data.model.java.Protocol;
import com.lkms.ui.protocol.adapter.ProtocolItemAdapter;
import com.lkms.ui.protocol.adapter.ProtocolStepAdapter;
import com.lkms.ui.protocol.viewmodel.ProtocolDetailViewModel;

public class ProtocolDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PROTOCOL_ID = "extra_protocol_id";
    private static final int INVALID_ID = -1;

    private ProtocolDetailViewModel viewModel;
    private TextView txtTitle, txtVersion, txtIntro, txtWarning, txtStatus;
    private RecyclerView recyclerSteps, recyclerItems;
    private ProgressBar progressBar;

    private ProtocolStepAdapter stepAdapter;
    private ProtocolItemAdapter itemAdapter;

    // ✅ BƯỚC 2: KHAI BÁO BIẾN CHO NÚT MỚI
    private FloatingActionButton fabCreateExperiment;

    // ✅ BƯỚC 3: TẠO BIẾN LƯU ID CỦA PROTOCOL HIỆN TẠI
    private int currentProtocolId = INVALID_ID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protocol_detail);

        // Lấy ID từ Intent và lưu vào biến của lớp
        currentProtocolId = getIntent().getIntExtra(EXTRA_PROTOCOL_ID, INVALID_ID);
        if (currentProtocolId == INVALID_ID) {
            Toast.makeText(this, "Protocol ID không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo các thành phần
        viewModel = new ViewModelProvider(this).get(ProtocolDetailViewModel.class);
        setupViews();
        setupRecyclerViews();

        // Quan sát dữ liệu từ ViewModel
        observeViewModel();

        // Yêu cầu tải dữ liệu
        viewModel.loadProtocolDetails(currentProtocolId);

        // ✅ BƯỚC 6: GỌI HÀM SETUP CHO NÚT MỚI
        setupFab();
    }

    private void setupViews() {
        // Ánh xạ các view cũ
        txtTitle = findViewById(R.id.txtTitle);
        txtVersion = findViewById(R.id.txtVersion);
        txtIntro = findViewById(R.id.txtIntroduction);
        txtWarning = findViewById(R.id.txtSafetyWarning);
        txtStatus = findViewById(R.id.txtStatus);
        recyclerSteps = findViewById(R.id.recyclerSteps);
        recyclerItems = findViewById(R.id.recyclerItems);
        progressBar = findViewById(R.id.progressBar);

        // ✅ BƯỚC 4: ÁNH XẠ NÚT FAB TỪ LAYOUT
        fabCreateExperiment = findViewById(R.id.fab_create_experiment);
    }

    private void setupRecyclerViews() {
        stepAdapter = new ProtocolStepAdapter();
        recyclerSteps.setLayoutManager(new LinearLayoutManager(this));
        recyclerSteps.setAdapter(stepAdapter);

        itemAdapter = new ProtocolItemAdapter();
        recyclerItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerItems.setAdapter(itemAdapter);
    }

    /**
     * ✅ BƯỚC 5: TẠO HÀM MỚI ĐỂ XỬ LÝ SỰ KIỆN CHO NÚT FAB
     * Cài đặt sự kiện click cho nút "Tạo Experiment".
     */
    private void setupFab() {
        fabCreateExperiment.setOnClickListener(view -> {
            // Kiểm tra lại ID để chắc chắn
            if (currentProtocolId != INVALID_ID) {
                Toast.makeText(this, "Chuẩn bị tạo experiment từ Protocol ID: " + currentProtocolId, Toast.LENGTH_SHORT).show();

                // TODO: Tạo Activity mới `CreateExperimentActivity` và bỏ comment các dòng dưới đây
                // Intent intent = new Intent(ProtocolDetailActivity.this, CreateExperimentActivity.class);
                // intent.putExtra(CreateExperimentActivity.EXTRA_PROTOCOL_ID_FOR_EXPERIMENT, currentProtocolId);
                // startActivity(intent);
            } else {
                Toast.makeText(this, "Không thể tạo experiment, protocol ID không hợp lệ.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Tập trung toàn bộ việc lắng nghe và cập nhật UI tại một nơi duy nhất.
     */
    private void observeViewModel() {
        viewModel.isLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getProtocol().observe(this, protocol -> {
            if (protocol != null) {
                updateProtocolInfo(protocol);
            }
        });

        viewModel.getSteps().observe(this, steps -> {
            if (steps != null) {
                stepAdapter.submitList(steps);
            }
        });

        viewModel.getItems().observe(this, items -> {
            if (items != null) {
                itemAdapter.submitList(items);
            }
        });
    }

    private void updateProtocolInfo(Protocol protocol) {
        txtTitle.setText(protocol.getProtocolTitle());
        txtVersion.setText("Version: " + protocol.getVersionNumber());
        txtIntro.setText(protocol.getIntroduction());
        txtWarning.setText(protocol.getSafetyWarning());

        // 1. Lấy Enum trạng thái từ protocol
        ProtocolApproveStatus statusEnum = protocol.getApproveStatus();
        if (statusEnum == null) return; // Kiểm tra an toàn

        // 2. Hiển thị tên của Enum (ví dụ: "PENDING", "APPROVED")
        txtStatus.setText(statusEnum.name());

        // 3. Gọi hàm mới để cập nhật màu nền
        updateStatusColor(statusEnum);
    }

    private void updateStatusColor(ProtocolApproveStatus approveStatus) {
        // Lấy background của TextView
        GradientDrawable background = (GradientDrawable) txtStatus.getBackground().mutate();

        int colorResId;
        // Sử dụng switch-case với Enum để an toàn và rõ ràng
        switch (approveStatus) {
            case APPROVED:
                colorResId = R.color.status_approved;
                break;
            case REJECTED:
                colorResId = R.color.status_rejected;
                break;
            case PENDING:
            default:
                colorResId = R.color.status_pending;
                break;
        }

        // Lấy màu từ file colors.xml và gán vào background
        background.setColor(ContextCompat.getColor(this, colorResId));
    }

}
