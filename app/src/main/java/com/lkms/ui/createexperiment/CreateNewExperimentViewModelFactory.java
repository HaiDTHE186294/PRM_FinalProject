package com.lkms.ui.createexperiment;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.lkms.data.repository.*;
import com.lkms.data.repository.implement.java.*;
import com.lkms.domain.createexperiment.*;
// Import các UseCase liên quan đến kho
import com.lkms.domain.createexperiment.CheckInventoryUseCase;
import com.lkms.domain.createexperiment.DeductInventoryForExperimentUseCase;
import com.lkms.domain.protocolusecase.GetProtocolDetailsUseCase;

public class CreateNewExperimentViewModelFactory implements ViewModelProvider.Factory {

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(CreateNewExperimentViewModel.class)) {

            // Bước 1: Khởi tạo tất cả các Repositories cần thiết
            IExperimentRepository experimentRepo = new ExperimentRepositoryImplJava();
            IProtocolRepository protocolRepo = new ProtocolRepositoryImplJava();
            IProjectRepositoryVjet projectRepo = new ProjectRepositoryVjetImplJava();
            IExperimentStepRepositoryVjet experimentStepRepo = new ExperimentStepRepositoryVjetImplJava();
            IInventoryRepository inventoryRepo = new InventoryRepositoryImplJava();

            // --- SỬA Ở ĐÂY: KHỞI TẠO CẢ 3 USE CASE CHO LUỒNG LOGIC MỚI ---

            // UseCase 1: Chỉ để KIỂM TRA kho, không hành động
            CheckInventoryUseCase checkInventoryUseCase = new CheckInventoryUseCase(inventoryRepo, protocolRepo);

            // UseCase 2: Chỉ để TẠO thí nghiệm, không dính đến kho
            CreateFullExperimentUseCase createFullUseCase = new CreateFullExperimentUseCase(
                    experimentRepo,
                    protocolRepo,
                    experimentStepRepo
            );

            // UseCase 3: Chỉ để TRỪ KHO, sau khi đã tạo thí nghiệm thành công
            DeductInventoryForExperimentUseCase deductInventoryUseCase = new DeductInventoryForExperimentUseCase(
                    inventoryRepo,
                    protocolRepo
            );

            // Các UseCase phụ khác
            GetProtocolDetailsUseCase getProtocolDetailsUseCase = new GetProtocolDetailsUseCase(protocolRepo);
            GetAvailableProjectsUseCase getProjectsUseCase = new GetAvailableProjectsUseCase(projectRepo);


            // --- "TIÊM" CẢ 3 USE CASE LIÊN QUAN VÀO VIEWMODEL ---
            return (T) new CreateNewExperimentViewModel(
                    createFullUseCase,
                    deductInventoryUseCase,
                    checkInventoryUseCase, // Thêm UseCase kiểm tra vào
                    getProtocolDetailsUseCase,
                    getProjectsUseCase
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
