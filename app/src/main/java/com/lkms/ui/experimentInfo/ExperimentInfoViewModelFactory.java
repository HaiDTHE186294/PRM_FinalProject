package com.lkms.ui.experimentInfo;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.lkms.domain.experimentdetail.GetExperimentDetailUseCase;

public class ExperimentInfoViewModelFactory implements ViewModelProvider.Factory {

    private final GetExperimentDetailUseCase useCase;

    public ExperimentInfoViewModelFactory(GetExperimentDetailUseCase useCase) {
        this.useCase = useCase;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ExperimentInfoViewModel.class)) {
            return (T) new ExperimentInfoViewModel(useCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
