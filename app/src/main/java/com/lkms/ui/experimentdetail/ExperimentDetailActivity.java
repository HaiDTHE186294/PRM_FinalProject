package com.lkms.ui.experimentdetail;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lkms.R;
import com.lkms.ui.experimentdetail.adapter.ExperimentDetailAdapter;


public class ExperimentDetailActivity extends AppCompatActivity implements ExperimentDetailAdapter.OnStepClickListener {

    public static final String EXTRA_EXPERIMENT_ID = "EXTRA_EXPERIMENT_ID";
    private static final int MOCK_EXPERIMENT_ID = 1;

    private RecyclerView recyclerView;
    private ExperimentDetailAdapter adapter; // Adapter
    private ExperimentDetailViewModel viewModel; // ViewModel

    // Giữ nguyên ID của bạn
    private static final int ROOT_VIEW_ID = R.id.main_layout; // (Sử dụng placeholder theo quy tắc)
    private static final int RECYCLER_VIEW_ID = R.id.recyclerView; // (Sử dụng placeholder theo quy tắc)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_experiment_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(ROOT_VIEW_ID), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- BẮT ĐẦU SỬA ---

        // 1. Khởi tạo Adapter CHỈ MỘT LẦN
        //    Adapter này là "dumb adapter" (adapter "ngu ngốc")
        adapter = new ExperimentDetailAdapter(this);

        // 2. Setup RecyclerView
        recyclerView = findViewById(RECYCLER_VIEW_ID);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter); // Gán adapter rỗng vào

        // 3. Khởi tạo ViewModel (Giả sử Factory đã được sửa)
        // [CẢNH BÁO] Đảm bảo ExperimentDetailViewModelFactory của bạn cung cấp CẢ 2 UseCase!
        ExperimentDetailViewModelFactory factory = new ExperimentDetailViewModelFactory(
                // Bạn cần khởi tạo và truyền 2 UseCase vào đây
                // ví dụ: new GetExperimentStepUseCase(...),
                //        new GetProtocolStepBasedOnExperimentStepUseCase(...)
        );
        viewModel = new ViewModelProvider(this, factory).get(ExperimentDetailViewModel.class);

        // 4. Lắng nghe ViewModel
        observeViewModel();

        // 5. Gọi hàm MỚI của ViewModel để tải dữ liệu
        viewModel.loadExperimentData(getExperimentId());

        // --- KẾT THÚC SỬA ---
    }

    private void observeViewModel() {

        // THAY ĐỔI 1: Lắng nghe 'adapterItems' (thay vì 'steps')
        viewModel.adapterItems.observe(this, adapterItems -> {
            // Dữ liệu về!
            // 'adapterItems' là List<AdapterItem> đã được xử lý
            if (adapterItems != null) {
                adapter.submitList(adapterItems); // Cập nhật adapter
            }
        });

        // Giữ nguyên: Lắng nghe lỗi
        viewModel.error.observe(this, errorMsg -> {
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
        });
    }

    private int getExperimentId() {
        String experimentIdStr = getIntent().getStringExtra(EXTRA_EXPERIMENT_ID);
        if (experimentIdStr == null || experimentIdStr.isEmpty()) {
            Toast.makeText(this, "Using MOCK Experiment ID: " + MOCK_EXPERIMENT_ID, Toast.LENGTH_SHORT).show();
            return MOCK_EXPERIMENT_ID;
        }
        try {
            return Integer.parseInt(experimentIdStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Error: Invalid ID format. Using MOCK ID.", Toast.LENGTH_LONG).show();
            return MOCK_EXPERIMENT_ID;
        }
    }


    @Override
    public void onStepExpandClicked(int stepId, int adapterPosition) {
        Toast.makeText(this, "Clicked: " + stepId, Toast.LENGTH_SHORT).show();
        // TODO: Gọi logic expand/collapse
        // Ví dụ: viewModel.loadLogsForStep(stepId, adapterPosition);
        // Sau đó, khi có kết quả, gọi: adapter.insertLogsForStep(logList, adapterPosition);
    }
}