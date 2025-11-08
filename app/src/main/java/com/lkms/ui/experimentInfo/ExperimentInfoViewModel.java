package com.lkms.ui.experimentInfo;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lkms.data.model.java.combine.ExperimentReportData;
import com.lkms.data.model.java.combine.ExperimentUserProjectProtocol;
import com.lkms.data.repository.IExperimentRepository;
import com.lkms.domain.experimentdetail.GetExperimentDetailUseCase;
import com.lkms.domain.report.CompleteExperimentUseCase;
import com.lkms.domain.report.GetExperimentReportUseCase;

public class ExperimentInfoViewModel extends ViewModel {

    private final GetExperimentDetailUseCase getExperimentDetailUseCase;
    private final MutableLiveData<ExperimentUserProjectProtocol> experimentLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> _errorLiveData = new MutableLiveData<>();
    public LiveData<String> error = _errorLiveData;
    private final CompleteExperimentUseCase completeExperimentUseCase;
    private final MutableLiveData<Boolean> _completionSuccess = new MutableLiveData<>();
    public LiveData<Boolean> getCompletionSuccess() {
        return _completionSuccess;
    }

    private final GetExperimentReportUseCase getExperimentReportUseCase;

    // LiveData để giữ dữ liệu báo cáo
    private final MutableLiveData<ExperimentReportData> _reportData = new MutableLiveData<>();
    public LiveData<ExperimentReportData> reportData = _reportData;

    public ExperimentInfoViewModel(GetExperimentDetailUseCase useCase, CompleteExperimentUseCase completeUseCase, GetExperimentReportUseCase getExperimentReportUseCase) {
        this.getExperimentDetailUseCase = useCase;
        this.completeExperimentUseCase = completeUseCase;
        this.getExperimentReportUseCase = getExperimentReportUseCase;
    }

    public LiveData<ExperimentUserProjectProtocol> getExperiment() { return experimentLiveData; }
    public LiveData<String> getError() { return _errorLiveData; }

    public void loadExperiment(int experimentId) {
        getExperimentDetailUseCase.execute(experimentId, new GetExperimentDetailUseCase.GetExperimentDetailCallback() {
            @Override
            public void onSuccess(ExperimentUserProjectProtocol  experiment) {
                experimentLiveData.postValue(experiment);
            }

            @Override
            public void onError(String error) {
                _errorLiveData.postValue(error);
            }
        });
    }

    public void completeExperiment(int experimentId) {
        completeExperimentUseCase.execute(experimentId, new CompleteExperimentUseCase.CompleteExperimentCallback() {
            @Override
            public void onSuccess() {
                _completionSuccess.postValue(true); // BÁO CÁO THÀNH CÔNG!
                loadExperiment(experimentId);
            }

            @Override
            public void onError(String error) {
                _errorLiveData.postValue(error); // Báo lỗi như cũ
            }
        });
    }

    public void generateReport(int experimentId) {
        Log.d("ExperimentInfoViewModel", "Generating report for experiment ID: " + experimentId);
        getExperimentReportUseCase.execute(experimentId, new GetExperimentReportUseCase.GetExperimentReportCallback() {
            @Override
            public void onSuccess(ExperimentReportData data) {
                Log.d("ExperimentInfoViewModel", "Generate Report Success: " + data.getExperimentTitle());
                _reportData.postValue(data);
            }

            @Override
            public void onError(String errorMessage) {
                // Đẩy lỗi lên LiveData trên Main Thread
                _errorLiveData.postValue(errorMessage);
            }
        });
    }
}
