
package com.lkms.ui.protocol;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.lkms.R;
import com.lkms.data.model.java.Protocol;
import com.lkms.ui.protocol.adapter.ProtocolAdapter;
import com.lkms.ui.protocol.viewmodel.ProtocolListViewModel;


public class ProtocolListActivity extends AppCompatActivity implements ProtocolAdapter.OnItemClickListener {

    // Khai báo các thành phần UI và logic
    private ProtocolListViewModel viewModel;
    private ProtocolAdapter protocolAdapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private SearchView searchView;
    private EditText creatorIdInput;
    private EditText versionNumberInput;
    private Button filterButton;

    // KHAI BÁO BIẾN CHO NÚT MỚI
    private FloatingActionButton fabNewProtocol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protocol_list);

        // Gọi các hàm helper để code gọn gàng
        initViews();
        setupRecyclerView();
        setupSearchView();
        setupViewModel();
        setupFilterControls();

        //GỌI HÀM SETUP CHO NÚT MỚI
        setupFab();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadLatestApprovedLibrary();
    }

    /**
     * Ánh xạ các view từ file layout.
     */
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewProtocols);
        progressBar = findViewById(R.id.progressBar);
        searchView = findViewById(R.id.searchView);
        creatorIdInput = findViewById(R.id.edittext_creator_id);
        versionNumberInput = findViewById(R.id.edittext_version_number);
        filterButton = findViewById(R.id.button_filter);

        // ÁNH XẠ NÚT MỚI TỪ LAYOUT
        fabNewProtocol = findViewById(R.id.fab_new_protocol);
    }

    /**
     * Cài đặt cho RecyclerView và Adapter.
     */
    private void setupRecyclerView() {
        // Khởi tạo adapter và truyền "this" (Activity) làm listener
        protocolAdapter = new ProtocolAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(protocolAdapter);
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            /**
             * Được gọi khi người dùng nhấn nút "submit" (hoàn thành) trên bàn phím.
             * Chúng ta có thể để trống hoặc gọi tìm kiếm ở đây.
             */
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Gọi tìm kiếm để chắc chắn rằng kết quả cuối cùng được cập nhật
                viewModel.searchProtocols(query);
                // Ẩn bàn phím đi cho gọn gàng
                searchView.clearFocus();
                return true;
            }

            /**
             * Được gọi mỗi khi có bất kỳ thay đổi nào trong ô tìm kiếm (thêm/xóa ký tự).
             * Đây là nơi chúng ta sẽ thực hiện tìm kiếm real-time.
             */
            @Override
            public boolean onQueryTextChange(String newText) {
                // ======================= BẮT ĐẦU SỬA ĐỔI =======================
                // `trim()` sẽ loại bỏ các dấu cách thừa ở đầu và cuối chuỗi.
                String trimmedText = newText.trim();

                if (trimmedText.isEmpty()) {
                    // Nếu sau khi bỏ dấu cách, chuỗi rỗng thì tải lại danh sách gốc.
                    viewModel.loadLatestApprovedLibrary();
                } else {
                    // Ngược lại, thực hiện tìm kiếm với nội dung người dùng đang gõ.
                    // Việc này sẽ xử lý được cả các chuỗi có chứa dấu cách ở giữa.
                    viewModel.searchProtocols(trimmedText);
                }
                // ======================== KẾT THÚC SỬA ĐỔI ========================
                return true;
            }
        });
    }

    /**
     * Cài đặt sự kiện cho các control của chức năng lọc.
     */
    private void setupFilterControls() {
        filterButton.setOnClickListener(v -> {
            String creatorIdText = creatorIdInput.getText().toString();
            String versionNumberText = versionNumberInput.getText().toString();

            Integer creatorId = null;
            if (!creatorIdText.isEmpty()) {
                try {
                    creatorId = Integer.parseInt(creatorIdText);
                } catch (NumberFormatException e) {
                    creatorIdInput.setError("ID phải là số");
                    return;
                }
            }

            viewModel.filterProtocols(creatorId, versionNumberText);
        });
    }

    /**
     * HÀM MỚI ĐỂ XỬ LÝ SỰ KIỆN CHO NÚT FAB
     * Cài đặt sự kiện click cho nút FloatingActionButton.
     */
    private void setupFab() {fabNewProtocol.setOnClickListener(view -> {
        Intent intent = new Intent(ProtocolListActivity.this, CreateProtocolActivity.class);
        startActivity(intent);
        });
    }


    /**
     * Khởi tạo ViewModel và quan sát (observe) LiveData.
     */
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ProtocolListViewModel.class);

        viewModel.getProtocols().observe(this, protocols -> {
            if (protocols != null) {

                protocolAdapter.submitList(protocols);
            }
        });

        viewModel.isLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getError().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, "Lỗi: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.loadLatestApprovedLibrary();
    }

    /**
     * Xử lý sự kiện click vào một item trong RecyclerView.
     * LƯU Ý: Nếu bạn đã làm theo các hướng dẫn về Enum trước đó, hàm này cần được sửa thành:
     * public void onItemClick(Protocol clickedProtocol)
     */
    @Override
    public void onItemClick(int position) {
        // Lấy protocol từ danh sách hiện tại của adapter
        Protocol clickedProtocol = protocolAdapter.getCurrentList().get(position);
        if (clickedProtocol == null) return;

        Intent intent = new Intent(this, ProtocolDetailActivity.class);
        intent.putExtra(ProtocolDetailActivity.EXTRA_PROTOCOL_ID, clickedProtocol.getProtocolId());
        startActivity(intent);
    }
}
