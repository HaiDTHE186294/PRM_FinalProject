package com.lkms.ui.project.projectdetail; // Đặt chung package với Activity

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
import com.lkms.ui.project.projectmanage.ProjectViewModel;

public class ProjectInfoFragment extends Fragment {

    private ProjectViewModel viewModel; // Dùng chung ViewModel của Activity
    private int projectId;

    private RecyclerView rvMembers;
    private RecyclerView rvExperiments;
    private MemberAdapter memberAdapter;
    private ExperimentAdapter experimentAdapter;
    private Button btnAddNewExperiment;

    public static ProjectInfoFragment newInstance(int projectId) {
        ProjectInfoFragment fragment = new ProjectInfoFragment();
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
        // Sử dụng layout fragment_project_info.xml đã tạo
        View view = inflater.inflate(R.layout.fragment_project_info, container, false);

        setupViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupClickListeners();
        setupObservers();

        // Yêu cầu ViewModel tải dữ liệu cho tab này
        // (Activity cha đã gọi loadProjectDetailsScreen,
        //  nên chúng ta chỉ cần observe)
    }

    private void setupViews(View view) {
        // Setup RecyclerView Thành viên
        rvMembers = view.findViewById(R.id.recyclerViewMembers);
        rvMembers.setLayoutManager(new LinearLayoutManager(getContext()));
        memberAdapter = new MemberAdapter();
        rvMembers.setAdapter(memberAdapter);
        rvMembers.setNestedScrollingEnabled(false); // Tắt cuộn lồng

        // Setup RecyclerView Thí nghiệm
        rvExperiments = view.findViewById(R.id.recyclerViewExperiments);
        rvExperiments.setLayoutManager(new LinearLayoutManager(getContext()));
        experimentAdapter = new ExperimentAdapter();
        rvExperiments.setAdapter(experimentAdapter);
        rvExperiments.setNestedScrollingEnabled(false); // Tắt cuộn lồng

        // Nút "Tạo thí nghiệm"
        btnAddNewExperiment = view.findViewById(R.id.btnAddNewExperiment);
    }

    private void setupClickListeners() {
        // UC 18: "có nút thêm mới dẫn đến màn Create New Experiment"
        btnAddNewExperiment.setOnClickListener(v -> {
            // TODO: Mở Activity/Fragment "Create Experiment (UC 5)"
            // intent.putExtra("PROJECT_ID", projectId);
            Toast.makeText(getContext(), "Mở màn hình Tạo Thí nghiệm (UC 5)", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupObservers() {
        // UC 18: "Hiển thị thành viên"
        viewModel.projectMembers.observe(getViewLifecycleOwner(), members -> {
            if (members != null) {
                memberAdapter.setMembers(members);
            }
        });

        // UC 18: "Hiển thị các thí nghiệm"
        viewModel.projectExperiments.observe(getViewLifecycleOwner(), experiments -> {
            if (experiments != null) {
                experimentAdapter.setExperiments(experiments);
            }
        });

        // Lắng nghe lỗi (nếu cần)
        viewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            // Hiển thị lỗi nếu cần
        });
    }
}