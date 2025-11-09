package com.lkms.ui.experimentdetail;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
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

    public static final String EXTRA_EXPERIMENT_ID = "experimentId";

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

        String statusText = getIntent().getStringExtra("status");

        // 1. Khởi tạo Adapter CHỈ MỘT LẦN
        adapter = new ExperimentDetailAdapter(this, this, statusText);

        // 2. Setup RecyclerView
        recyclerView = findViewById(RECYCLER_VIEW_ID);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        TextView tilte = findViewById(R.id.tilte);
        TextView status = findViewById(R.id.status);
        TextView objectiveText = findViewById(R.id.objectiveText);

        // 3. Khởi tạo ViewModel
        ExperimentDetailViewModelFactory factory = new ExperimentDetailViewModelFactory();

        viewModel = new ViewModelProvider(this, factory).get(ExperimentDetailViewModel.class);

        // 4. Lắng nghe ViewModel
        observeViewModel();

        int  experimentId = getIntent().getIntExtra(EXTRA_EXPERIMENT_ID, -1);
        if (experimentId == -1) {
            Toast.makeText(this, "Không tìm thấy experimentId", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // 5. Gọi hàm MỚI của ViewModel để tải dữ liệu
        viewModel.loadExperimentData(experimentId);

        tilte.setText(getIntent().getStringExtra("title"));
        status.setText(statusText);
        objectiveText.setText(getIntent().getStringExtra("objective"));
    }

    private void observeViewModel() {

        // THAY ĐỔI 1: Lắng nghe 'adapterItems' (thay vì 'steps')
        viewModel.adapterItems.observe(this, adapterItems -> {
            // Dữ liệu về!
            // 'adapterItems' là List<AdapterItem> đã được xử lý
            if (adapterItems != null) {
                Log.d("ActivityDebug", "Observer nhận được adapterItems. Size: " + adapterItems.size());
                adapter.submitList(adapterItems); // Cập nhật adapter
            } else {
                Log.d("ActivityDebug", "Observer nhận được adapterItems = null");
            }
        });

        // Giữ nguyên: Lắng nghe lỗi
        viewModel.error.observe(this, errorMsg -> {
            Log.d("ActivityDebug", "Observer nhận được LỖI: " + errorMsg);
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
        });

        // Lắng nghe sự kiện "CHÈN LOG"
        viewModel.logInsertWrapper.observe(this, event -> {
            if (event != null) {
                adapter.insertLogsForStep(event.logs, event.adapterPosition);
            }
        });
    }

    @Override
    public void onStepExpandClicked(int stepId, int adapterPosition) {
        viewModel.loadLogFromStep(stepId, adapterPosition);
    }

}