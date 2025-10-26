// Sửa lại: com/lkms/ui/experimentdetail/ExperimentDetailViewModelFactory.java

package com.lkms.ui.experimentdetail;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.lkms.data.repository.IProtocolRepository;
import com.lkms.data.repository.implement.java.ExperimentRepositoryImplJava;
import com.lkms.data.repository.IExperimentRepository;
import com.lkms.data.repository.implement.java.ProtocolRepositoryImplJava;
import com.lkms.domain.experimentdetail.GetExperimentStepUseCase;
import com.lkms.domain.experimentdetail.GetLogEntryUseCase;
import com.lkms.domain.experimentdetail.GetProtocolStepBasedOnExperimentStepUseCase;

public class ExperimentDetailViewModelFactory implements ViewModelProvider.Factory {

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ExperimentDetailViewModel.class)) {

            IExperimentRepository repository = new ExperimentRepositoryImplJava();
            IProtocolRepository repository1 = new ProtocolRepositoryImplJava();

            GetExperimentStepUseCase useCase = new GetExperimentStepUseCase(repository);
            GetLogEntryUseCase usecaseLog = new GetLogEntryUseCase(repository);
            GetProtocolStepBasedOnExperimentStepUseCase useCase1 = new GetProtocolStepBasedOnExperimentStepUseCase(repository1);

            return (T) new ExperimentDetailViewModel(useCase, useCase1, usecaseLog);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}