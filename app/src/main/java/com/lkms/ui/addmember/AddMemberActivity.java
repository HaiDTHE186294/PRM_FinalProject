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
            // ⭐ SỬA Ở ĐÂY: Thêm dòng này để ra lệnh cho Adapter tự cập nhật trạng thái ⭐
            adapter.toggleSelection(user);

            // 1. Cập nhật danh sách các user đã chọn trong Activity
            if (isNowSelected) {
                selectedUsers.add(user);
            } else {
                selectedUsers.remove(user);
            }

            // 2. Yêu cầu adapter vẽ lại CHỈ item vừa được click
            int position = adapter.getUserList().indexOf(user);
            if (position != -1) {
                adapter.notifyItemChanged(position);
            }

            // 3. Cập nhật lại trạng thái và chữ trên nút "Add"
            btnAddSelectedMembers.setEnabled(!selectedUsers.isEmpty());
            int size = selectedUsers.size();
            btnAddSelectedMembers.setText(size > 0 ? "Add " + size + " Member(s)" : "Add Member");
        });

        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResults.setAdapter(adapter);
    }

    private void observeViewModel() {
        // Lắng nghe kết quả tìm kiếm đã được lọc
        viewModel.searchResults.observe(this, users -> {
            progressBar.setVisibility(View.GONE);
            adapter.setUsers(users); // Hiển thị danh sách đã được lọc
        });

        // Lắng nghe lỗi
        viewModel.error.observe(this, error -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });

        // Lắng nghe sự kiện thêm thành công
        viewModel.addMembersSuccess.observe(this, success -> {
            if (success) {
                Toast.makeText(this, "Members added successfully!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK); // Gửi tín hiệu thành công về cho màn hình trước
                finish(); // Đóng màn hình
            }
        });

        // Lắng nghe lỗi khi thêm thành viên
        viewModel.addMembersError.observe(this, error -> {
            Toast.makeText(this, "Error adding members: " + error, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupListeners() {
        // Sự kiện khi người dùng gõ vào ô tìm kiếm
        etSearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                progressBar.setVisibility(View.VISIBLE);
                viewModel.findAvailableUsers(query, experimentId);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Sự kiện khi nhấn nút Add
        btnAddSelectedMembers.setOnClickListener(v -> {
            if (!selectedUsers.isEmpty()) {
                viewModel.addMembersToTeam(experimentId, selectedUsers);
            }
        });
    }

    /**
     * Tải danh sách người dùng ban đầu (chưa có trong team).
     */
    private void loadInitialUsers() {
        progressBar.setVisibility(View.VISIBLE);
        viewModel.findAvailableUsers("", experimentId);
    }
}
