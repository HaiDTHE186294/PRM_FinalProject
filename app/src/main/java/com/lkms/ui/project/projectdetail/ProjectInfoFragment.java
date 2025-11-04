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

        viewModel = new ViewModelProvider(requireActivity()).get(ProjectViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_info, container, false);

        setupViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupClickListeners();
        setupObservers();

    }

    private void setupViews(View view) {
        rvMembers = view.findViewById(R.id.recyclerViewMembers);
        rvMembers.setLayoutManager(new LinearLayoutManager(getContext()));
        memberAdapter = new MemberAdapter();
        rvMembers.setAdapter(memberAdapter);
        rvMembers.setNestedScrollingEnabled(false);

        rvExperiments = view.findViewById(R.id.recyclerViewExperiments);
        rvExperiments.setLayoutManager(new LinearLayoutManager(getContext()));
        experimentAdapter = new ExperimentAdapter();
        rvExperiments.setAdapter(experimentAdapter);
        rvExperiments.setNestedScrollingEnabled(false);

        btnAddNewExperiment = view.findViewById(R.id.btnAddNewExperiment);
    }

    private void setupClickListeners() {
        btnAddNewExperiment.setOnClickListener(v -> {
            // TODO: Mở Activity/Fragment "Create Experiment (UC 5)"
            // intent.putExtra("PROJECT_ID", projectId);
            Toast.makeText(getContext(), "Create Experiment", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupObservers() {
        viewModel.projectMembers.observe(getViewLifecycleOwner(), members -> {
            if (members != null) {
                memberAdapter.setMembers(members);
            }
        });

        viewModel.projectExperiments.observe(getViewLifecycleOwner(), experiments -> {
            if (experiments != null) {
                experimentAdapter.setExperiments(experiments);
            }
        });

        viewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
        });
    }
}