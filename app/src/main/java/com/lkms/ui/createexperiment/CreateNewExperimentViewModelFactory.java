package com.lkms.ui.createexperiment;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;import androidx.lifecycle.ViewModelProvider;

import com.lkms.data.repository.*;
import com.lkms.data.repository.implement.java.*;
import com.lkms.domain.createexperiment.*;
import com.lkms.domain.protocolusecase.GetProtocolDetailsUseCase;

public class CreateNewExperimentViewModelFactory implements ViewModelProvider.Factory {

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(CreateNewExperimentViewModel.class)) {

            // Bước 1: Khởi tạo các Repositories cần thiết
            IExperimentRepository experimentRepo = new ExperimentRepositoryImplJava();
            IProtocolRepository protocolRepo = new ProtocolRepositoryImplJava();
            IProjectRepository projectRepo = new ProjectRepositoryImplJava();
            IExperimentStepRepository experimentStepRepo = new ExperimentStepRepositoryImplJava();

            // --- SỬA Ở ĐÂY: THÊM LẠI INVENTORY REPO ---
            IInventoryRepository inventoryRepo = new InventoryRepositoryImplJava();

            // Bước 2: Khởi tạo UseCases
            // --- SỬA Ở ĐÂY: THÊM LẠI inventoryRepo VÀO CONSTRUCTOR ---
            CreateFullExperimentUseCase createFullUseCase = new CreateFullExperimentUseCase(
                    experimentRepo,
                    protocolRepo,
                    experimentStepRepo,
                    inventoryRepo // Thêm repository kho vào
            );

            GetProtocolDetailsUseCase getProtocolDetailsUseCase = new GetProtocolDetailsUseCase(protocolRepo);
            GetAvailableProjectsUseCase getProjectsUseCase = new GetAvailableProjectsUseCase(projectRepo);

            // Bước 3: Tạo ViewModel với các UseCase đã được cập nhật
            return (T) new CreateNewExperimentViewModel(
                    createFullUseCase,
                    getProtocolDetailsUseCase,
                    getProjectsUseCase
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
