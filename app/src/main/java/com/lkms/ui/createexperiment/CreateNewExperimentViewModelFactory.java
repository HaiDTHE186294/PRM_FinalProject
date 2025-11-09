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
            ITeamRepository teamRepo = new TeamRepositoryImplJava();

            // KHỞI TẠO CÁC USE CASE VỚI ĐẦY ĐỦ REPOSITORY ---

            // UseCase 1: Chỉ để KIỂM TRA kho, không hành động
            CheckInventoryUseCase checkInventoryUseCase = new CheckInventoryUseCase(inventoryRepo, protocolRepo);

            // UseCase 2: TẠO thí nghiệm, team và các bước
            CreateFullExperimentUseCase createFullUseCase = new CreateFullExperimentUseCase(
                    experimentRepo,
                    protocolRepo,
                    experimentStepRepo,
                    teamRepo
            );

            // UseCase 3: Chỉ để TRỪ KHO, sau khi đã tạo thí nghiệm thành công
            DeductInventoryForExperimentUseCase deductInventoryUseCase = new DeductInventoryForExperimentUseCase(
                    inventoryRepo,
                    protocolRepo
            );

            GetProtocolDetailsUseCase getProtocolDetailsUseCase = new GetProtocolDetailsUseCase(protocolRepo);
            GetAvailableProjectsUseCase getProjectsUseCase = new GetAvailableProjectsUseCase(projectRepo);


            // --- "TIÊM" CÁC USE CASE LIÊN QUAN VÀO VIEWMODEL ---
            return (T) new CreateNewExperimentViewModel(
                    createFullUseCase,
                    deductInventoryUseCase,
                    checkInventoryUseCase,
                    getProtocolDetailsUseCase,
                    getProjectsUseCase
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
