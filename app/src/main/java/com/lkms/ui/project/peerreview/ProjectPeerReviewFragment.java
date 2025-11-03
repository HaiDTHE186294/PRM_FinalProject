package com.lkms.ui.project.peerreview;

// Thêm các import cần thiết cho DatePicker và Calendar
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.widget.EditText;
import com.lkms.data.model.java.PeerReview;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lkms.R;
import com.lkms.ui.project.projectdetail.ProjectDetailActivity;
import com.lkms.ui.project.projectmanage.ProjectViewModel; // Dùng chung ViewModel

// Import cho việc định dạng ngày
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ProjectPeerReviewFragment extends Fragment {

    private ProjectViewModel viewModel;
    private int projectId;

    private RecyclerView rvPeerReviews;
    private PeerReviewAdapter peerReviewAdapter;
    private Button btnAddNewPeerReview;

    public static ProjectPeerReviewFragment newInstance(int projectId) {
        ProjectPeerReviewFragment fragment = new ProjectPeerReviewFragment();
        Bundle args = new Bundle();
        args.putInt(ProjectDetailActivity.PROJECT_ID_KEY, projectId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectId = getArguments().getInt(ProjectDetailActivity.PROJECT_ID_KEY);
        }
        // Lấy ViewModel của Activity cha
        viewModel = new ViewModelProvider(requireActivity()).get(ProjectViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_peer_review, container, false);
        setupViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupClickListeners();
        setupObservers();

        // Yêu cầu ViewModel tải dữ liệu cho tab này
        viewModel.loadPeerReviews(projectId);
    }

    private void setupViews(View view) {
        rvPeerReviews = view.findViewById(R.id.recyclerViewPeerReviews);
        rvPeerReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        peerReviewAdapter = new PeerReviewAdapter();
        rvPeerReviews.setAdapter(peerReviewAdapter);

        btnAddNewPeerReview = view.findViewById(R.id.btnAddNewPeerReview);
    }

    private void setupClickListeners() {
        // UC 16: "Button dẫn đến form tạo mới lịch họp"
        btnAddNewPeerReview.setOnClickListener(v -> {
            showCreatePeerReviewDialog();
        });
    }

    private void setupObservers() {
        // UC 16: "Recycle view hiển thị danh sách lịch họp"
        viewModel.peerReviews.observe(getViewLifecycleOwner(), reviews -> {
            if (reviews != null) {
                peerReviewAdapter.setReviews(reviews);
            }
        });

        // Lắng nghe khi tạo mới thành công
        viewModel.peerReviewCreated.observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) { // Thêm kiểm tra null
                Toast.makeText(getContext(), "Create peer review success", Toast.LENGTH_SHORT).show();
                // Tải lại danh sách
                viewModel.loadPeerReviews(projectId);
            }
        });

        // Lắng nghe lỗi (từ ViewModel cha)
        viewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                // Hiển thị lỗi nếu cần
            }
        });
    }

    /**
     * Hiển thị Dialog (form) cho UC 16
     * (Đã cập nhật để thêm DatePickerDialog)
     */
    private void showCreatePeerReviewDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_peer_review, null);
        final EditText etDetail = dialogView.findViewById(R.id.etPeerReviewDetail);
        final EditText etLink = dialogView.findViewById(R.id.etPeerReviewLink);
        final EditText etStartTime = dialogView.findViewById(R.id.etPeerReviewStartTime);
        final EditText etEndTime = dialogView.findViewById(R.id.etPeerReviewEndTime);

        // --- GẮN LOGIC CHỌN NGÀY ---
        etStartTime.setOnClickListener(v -> showDatePickerDialog(etStartTime));
        etEndTime.setOnClickListener(v -> showDatePickerDialog(etEndTime));
        // --- KẾT THÚC ---

        new AlertDialog.Builder(getContext())
                .setTitle("Create peer review")
                .setView(dialogView)
                .setPositiveButton("Create", (dialog, which) -> {
                    String detail = etDetail.getText().toString().trim();
                    String link = etLink.getText().toString().trim();
                    String startTime = etStartTime.getText().toString().trim();
                    String endTime = etEndTime.getText().toString().trim();

                    // Validation (Thời gian kết thúc có thể tùy chọn)
                    if (detail.isEmpty() || startTime.isEmpty()) {
                        Toast.makeText(getContext(), "Content and start time do not be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Ghép nội dung và link vào 1 trường duy nhất (như yêu cầu)
                    String combinedDetail = detail;
                    if (!link.isEmpty()) {
                        combinedDetail += "\nLink: " + link;
                    }

                    // Tạo model
                    PeerReview newReview = new PeerReview(
                            null, // reviewId (tự tăng)
                            projectId,
                            startTime, // startTime
                            endTime.isEmpty() ? null : endTime, // endTime có thể null
                            combinedDetail // Nội dung đã ghép
                    );

                    // Gọi ViewModel
                    viewModel.createPeerReview(newReview);
                })
                .setNegativeButton("カンセール", null)
                .show();
    }

    /**
     * Hàm trợ giúp (helper) để hiển thị DatePickerDialog
     * @param targetEditText Ô EditText để điền ngày
     */
    private void showDatePickerDialog(EditText targetEditText) {
        final Calendar calendar = Calendar.getInstance();

        // Định nghĩa listener khi người dùng chọn xong ngày
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // Định dạng ngày và đặt text cho EditText
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            targetEditText.setText(sdf.format(calendar.getTime()));
        };

        // Hiển thị dialog
        // Đảm bảo getContext() không null
        if(getContext() == null) return;

        new DatePickerDialog(getContext(), dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }
}