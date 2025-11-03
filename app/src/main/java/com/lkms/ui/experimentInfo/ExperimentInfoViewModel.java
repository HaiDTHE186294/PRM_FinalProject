package com.lkms.ui.experimentInfo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lkms.data.model.java.combine.ExperimentUserProjectProtocol;
import com.lkms.domain.experimentdetail.GetExperimentDetailUseCase;

public class ExperimentInfoViewModel extends ViewModel {

    private final GetExperimentDetailUseCase getExperimentDetailUseCase;
    private final MutableLiveData<ExperimentUserProjectProtocol> experimentLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public ExperimentInfoViewModel(GetExperimentDetailUseCase useCase) {
        this.getExperimentDetailUseCase = useCase;
    }

    public LiveData<ExperimentUserProjectProtocol> getExperiment() { return experimentLiveData; }
    public LiveData<String> getError() { return errorLiveData; }

    public void loadExperiment(int experimentId) {
        getExperimentDetailUseCase.execute(experimentId, new GetExperimentDetailUseCase.GetExperimentDetailCallback() {
            @Override
            public void onSuccess(ExperimentUserProjectProtocol  experiment) {
                experimentLiveData.postValue(experiment);
            }

            @Override
            public void onError(String error) {
                errorLiveData.postValue(error);
            }
        });
    }
}
