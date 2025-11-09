package com.lkms.ui.addmember;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lkms.R;
import com.lkms.data.model.java.User;

import java.util.HashSet;
import java.util.Set;

public class AddMemberActivity extends AppCompatActivity {

    private AddMemberViewModel viewModel;
    private EditText etSearchUser;
    private RecyclerView rvSearchResults;
    private ProgressBar progressBar;
    private Button btnAddSelectedMembers;
    private UserSearchAdapter adapter;

    private int experimentId;
    private final Set<User> selectedUsers = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member);

        // Lấy experimentId từ Intent
        experimentId = getIntent().getIntExtra("EXPERIMENT_ID", -1);
        if (experimentId == -1) {
            Toast.makeText(this, "Error: Experiment ID is missing", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this, new AddMemberViewModelFactory()).get(AddMemberViewModel.class);

        // Ánh xạ và cài đặt các View
        setupViews();
        setupRecyclerView();
        observeViewModel();
        setupListeners();

        // Tải danh sách người dùng ban đầu
        loadInitialUsers();
    }

    private void setupViews() {
        etSearchUser = findViewById(R.id.etSearchUser);
        rvSearchResults = findViewById(R.id.rvSearchResults);
        progressBar = findViewById(R.id.progressBar);
        btnAddSelectedMembers = findViewById(R.id.btnAddSelectedMembers);
    }

    private void setupRecyclerView() {
        adapter = new UserSearchAdapter((user, isNowSelected) -> {
            // Khôi phục logic quan trọng đã bị xóa nhầm
            // 1. Ra lệnh cho Adapter tự cập nhật trạng thái lựa chọn nội bộ của nó
            adapter.toggleSelection(user);

            // 2. Cập nhật danh sách các user đã chọn trong Activity
            if (isNowSelected) {
                selectedUsers.add(user);
            } else {
                selectedUsers.remove(user);
            }

            // 3. Yêu cầu adapter vẽ lại CHỈ item vừa được click để cập nhật UI (checkbox)
            int position = adapter.getUserList().indexOf(user);
            if (position != -1) {
                adapter.notifyItemChanged(position);
            }

            // 4. Cập nhật lại trạng thái và chữ trên nút "Add"
            updateAddButtonState();
        });

        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResults.setAdapter(adapter);
    }

    private void observeViewModel() {
        // Gọi qua phương thức getter để truy cập LiveData
        viewModel.getSearchResults().observe(this, users -> {
            progressBar.setVisibility(View.GONE);
            adapter.setUsers(users); // Hiển thị danh sách đã được lọc
        });

        viewModel.getError().observe(this, error -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });

        viewModel.getAddMembersSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) { // Kiểm tra an toàn hơn
                Toast.makeText(this, "Members added successfully!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK); // Gửi tín hiệu thành công về cho màn hình trước
                finish(); // Đóng màn hình
            }
        });

        viewModel.getAddMembersError().observe(this, error -> {
            progressBar.setVisibility(View.GONE); // Ẩn loading khi thêm lỗi
            Toast.makeText(this, "Error adding members: " + error, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupListeners() {
        // Sự kiện khi người dùng gõ vào ô tìm kiếm
        etSearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Thêm comment để giải quyết cảnh báo SonarQube
                // Không cần xử lý ở đây.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                progressBar.setVisibility(View.VISIBLE);
                viewModel.findAvailableUsers(query, experimentId);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Thêm comment để giải quyết cảnh báo SonarQube
                // Không cần xử lý ở đây.
            }
        });

        // Sự kiện khi nhấn nút Add
        btnAddSelectedMembers.setOnClickListener(v -> {
            if (!selectedUsers.isEmpty()) {
                progressBar.setVisibility(View.VISIBLE); // Hiển thị loading khi nhấn Add
                viewModel.addMembersToTeam(experimentId, selectedUsers);
            }
        });
    }

    // Tách logic cập nhật nút ra hàm riêng cho rõ ràng
    private void updateAddButtonState() {
        boolean hasSelection = !selectedUsers.isEmpty();
        btnAddSelectedMembers.setEnabled(hasSelection);
        if (hasSelection) {
            btnAddSelectedMembers.setText("Add " + selectedUsers.size() + " Member(s)");
        } else {
            btnAddSelectedMembers.setText("Add Member");
        }
    }

    /**
     * Tải danh sách người dùng ban đầu (chưa có trong team).
     */
    private void loadInitialUsers() {
        progressBar.setVisibility(View.VISIBLE);
        viewModel.findAvailableUsers("", experimentId);
    }
}

