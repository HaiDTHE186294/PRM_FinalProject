package com.lkms.domain;

import com.lkms.data.repository.IEquipmentRepository;
import com.lkms.data.model.java.MaintenanceLog;

import java.util.List;

public class MaintenanceLogUseCase {

    private final IEquipmentRepository repository;

    public MaintenanceLogUseCase(IEquipmentRepository repository) {
        this.repository = repository;
    }

    public void getLogsByEquipment(int equipmentId, IEquipmentRepository.MaintenanceLogCallback callback) {
        repository.getMaintenanceLogs(equipmentId, callback);
    }
}
