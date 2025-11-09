package com.lkms.ui.project.projectdetail; // Đặt chung package với Activity

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.lkms.data.model.java.Experiment;
import com.lkms.ui.experimentInfo.ExperimentInfoActivity;
import com.lkms.ui.project.projectmanage.ProjectViewModel;
import com.lkms.ui.protocol.ProtocolListActivity;

public class ProjectInfoFragment extends Fragment implements ExperimentAdapter.OnExperimentClickListener{

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
        experimentAdapter = new ExperimentAdapter(this);
        rvExperiments.setAdapter(experimentAdapter);
        rvExperiments.setNestedScrollingEnabled(false);

        btnAddNewExperiment = view.findViewById(R.id.btnAddNewExperiment);
    }

    private void setupClickListeners() {
        btnAddNewExperiment.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ProtocolListActivity.class);
            startActivity(intent);});
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

    @Override
    public void onExperimentClick(Experiment experiment) {
        int experimentId = experiment.getExperimentId();
        Log.d("EXPERIMENT_CLICK", "Clicked Experiment ID: " + experimentId);

        if (experimentId != -1) {
            Intent intent = new Intent(requireContext(), ExperimentInfoActivity.class);
            intent.putExtra("experimentId", experimentId);
            startActivity(intent);
        } else {
            Toast.makeText(requireContext(), "Experiment ID không hợp lệ!", Toast.LENGTH_SHORT).show();
        }
    }
}