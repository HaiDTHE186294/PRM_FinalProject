package com.lkms.ui.experimentInfo;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.lkms.domain.experimentdetail.GetExperimentDetailUseCase;
import com.lkms.domain.report.CompleteExperimentUseCase;
import com.lkms.domain.report.GetExperimentReportUseCase;

public class ExperimentInfoViewModelFactory implements ViewModelProvider.Factory {

    private final GetExperimentDetailUseCase useCase;
    private final CompleteExperimentUseCase completeUseCase;
    private final GetExperimentReportUseCase reportUseCase;


    public ExperimentInfoViewModelFactory(GetExperimentDetailUseCase useCase, CompleteExperimentUseCase completeUseCase, GetExperimentReportUseCase reportUseCase) {
        this.useCase = useCase;
        this.completeUseCase = completeUseCase;
        this.reportUseCase = reportUseCase;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ExperimentInfoViewModel.class)) {
            return (T) new ExperimentInfoViewModel(useCase, completeUseCase, reportUseCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
