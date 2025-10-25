package com.lkms.ui.common;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lkms.R;
import com.lkms.data.model.java.MaintenanceLog;
import com.lkms.data.repository.IEquipmentRepository;
import com.lkms.data.repository.implement.java.EquipmentRepositoryImplJava;
import com.lkms.domain.MaintenanceLogUseCase;
import com.lkms.ui.equipment.MaintenanceLogAdapter;

import java.util.List;

public class MaintenanceLogFragment extends Fragment {

    private static final String ARG_EQUIPMENT_ID = "equipment_id";

    private RecyclerView recyclerLog;
    private MaintenanceLogUseCase useCase;
    private int equipmentId;

    public static MaintenanceLogFragment newInstance(int equipmentId) {
        MaintenanceLogFragment fragment = new MaintenanceLogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_EQUIPMENT_ID, equipmentId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maintenance_log, container, false);

        recyclerLog = view.findViewById(R.id.rvMaintenanceLog);
        recyclerLog.setLayoutManager(new LinearLayoutManager(getContext()));

        equipmentId = getArguments().getInt(ARG_EQUIPMENT_ID, -1);
        useCase = new MaintenanceLogUseCase(new EquipmentRepositoryImplJava() {
        });

        loadLogs();

        return view;
    }

    private void loadLogs() {
        new Thread(() -> {
            useCase.getLogsByEquipment(equipmentId, new IEquipmentRepository.MaintenanceLogCallback() {
                @Override
                public void onSuccess(List<MaintenanceLog> logs) {
                    requireActivity().runOnUiThread(() -> {
                        recyclerLog.setAdapter(new MaintenanceLogAdapter(logs));
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show());
                }
            });

        }).start();
    }
}