// Tạo file mới: ExperimentDetailViewModel.java
// (Thường nằm trong package: com.lkms.ui.experimentdetail)

package com.lkms.ui.experimentdetail;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lkms.data.model.java.ExperimentStep;
import com.lkms.data.model.java.LogEntry;
import com.lkms.data.model.java.ProtocolStep;
import com.lkms.data.model.java.combine.ExperimentProtocolStep;
import com.lkms.domain.experimentdetail.GetLogEntryUseCase;
import com.lkms.ui.experimentdetail.adapter.AdapterItem;
import com.lkms.ui.experimentdetail.adapter.LogInsertWrapper;
import com.lkms.ui.experimentdetail.adapter.StepItemWrapper;

import com.lkms.domain.experimentdetail.GetExperimentStepUseCase;
import com.lkms.domain.experimentdetail.GetProtocolStepBasedOnExperimentStepUseCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ExperimentDetailViewModel extends ViewModel {

    // 1. Dependencies
    private final GetExperimentStepUseCase getExperimentStepUseCase;
    private final GetProtocolStepBasedOnExperimentStepUseCase getProtocolStepUseCase;
    private final GetLogEntryUseCase getLogEntryUseCase;

    // 2. LiveData để giữ trạng thái cho UI
    //    Activity sẽ "lắng nghe" (observe) các LiveData này

    // Dữ liệu khi thành công
    private final MutableLiveData<List<AdapterItem>> _adapterItems = new MutableLiveData<>();
    public LiveData<List<AdapterItem>> adapterItems = _adapterItems;

    private final MutableLiveData<LogInsertWrapper> _logInsertWrapper = new MutableLiveData<>();
    public LiveData<LogInsertWrapper> logInsertWrapper = _logInsertWrapper;

    // Trạng thái loading
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    // Dữ liệu khi có lỗi
    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    // 3. Constructor (Nơi nhận UseCase)
    //    Chúng ta sẽ cần một ViewModelFactory để "tiêm" UseCase này vào
    public ExperimentDetailViewModel(
            GetExperimentStepUseCase getExperimentStepUseCase,
            GetProtocolStepBasedOnExperimentStepUseCase getProtocolStepUseCase,
            // Thêm dependency
            GetLogEntryUseCase getLogEntryUseCase
    ) {
        this.getExperimentStepUseCase = getExperimentStepUseCase;
        this.getProtocolStepUseCase = getProtocolStepUseCase; // Gán dependency
        this.getLogEntryUseCase = getLogEntryUseCase;
    }

    // 4. Phương thức mà Activity sẽ gọi
    public void loadExperimentData(int experimentId) {
        Log.d("ViewModelDebug", "loadExperimentData BẮT ĐẦU. ID: " + experimentId);
        _isLoading.setValue(true); // Bắt đầu loading

        // Bước 1: Lấy danh sách ExperimentStep
        getExperimentStepUseCase.execute(experimentId, new GetExperimentStepUseCase.getExperimentStepsListCallback() {

            public void onSuccess(List<ExperimentStep> originalSteps) {
                // Bước 1 thành công!
                // THAY ĐỔI: Không cập nhật LiveData vội.
                // Thay vào đó, bắt đầu Bước 2: Lấy ProtocolSteps
                Log.d("ViewModelDebug", "loadExperimentData thành công. StepSize: " + originalSteps.size());

                initializeFlatList(originalSteps, new FlatListCallback() {
                    @Override
                    public void onFlatListReady(List<AdapterItem> flatList) {
                        Log.d("ViewModelDebug", "loadExperimentData: UseCase THÀNH CÔNG. Nhận được " + originalSteps.size() + " steps.");
                        // Bước 2 thành công!
                        _adapterItems.postValue(flatList); // Cập nhật danh sách cuối cùng
                        _isLoading.postValue(false); // Báo UI: Hết loading
                    }

                    @Override
                    public void onError(String errorMsg) {
                        // Lỗi ở Bước 2
                        _error.postValue(errorMsg);
                        _isLoading.postValue(false);
                    }
                });
            }

            @Override
            public void onError(String errorMsg) {
                // Lỗi ngay từ Bước 1
                _error.postValue(errorMsg);
                _isLoading.postValue(false);
            }
        });
    }

    private void initializeFlatList(List<ExperimentStep> originalSteps, final FlatListCallback callback) {
        if (originalSteps == null || originalSteps.isEmpty()) {
            callback.onFlatListReady(new ArrayList<>());
            return;
        }

        final int totalSteps = originalSteps.size();
        final AtomicInteger tasksCompleted = new AtomicInteger(0);
        final Map<Integer, AdapterItem> resultMap = new ConcurrentHashMap<>();

        final Runnable checkCompletion = () -> {
            if (tasksCompleted.incrementAndGet() == totalSteps) {
                List<AdapterItem> flatList = new ArrayList<>();
                for (int j = 0; j < totalSteps; j++) {
                    if (resultMap.containsKey(j)) {
                        flatList.add(resultMap.get(j));
                    }
                }
                Log.d("Get Flatlist Sucessfull", "Flatlist size: " + flatList.size());
                callback.onFlatListReady(flatList);
            }
        };

        for (int i = 0; i < totalSteps; i++) {
            final ExperimentStep experimentStep = originalSteps.get(i);
            final int index = i;
            int protocolId = experimentStep.getProtocolStepId();

            // Dùng UseCase thứ 2
            getProtocolStepUseCase.execute(protocolId,
                    new GetProtocolStepBasedOnExperimentStepUseCase.getProtocolStepBasedOnExperimentStepCallback() {
                        @Override
                        public void onSuccess(ProtocolStep protocolStep) {
                            ExperimentProtocolStep combinedStep = new ExperimentProtocolStep(experimentStep, protocolStep);
                            StepItemWrapper stepWrapper = new StepItemWrapper(combinedStep);
                            resultMap.put(index, stepWrapper);
                            checkCompletion.run();
                        }

                        @Override
                        public void onError(String error) {
                            System.out.println("Lỗi khi lấy PStep: " + error);
                            // Quyết định: Bỏ qua hay báo lỗi?
                            // Tạm thời bỏ qua item này và tiếp tục
                            checkCompletion.run();
                            // Nếu muốn dừng toàn bộ, gọi: callback.onError(error);
                        }
                    });
        }
    }

    public interface FlatListCallback {
        void onFlatListReady(List<AdapterItem> flatList);
        void onError(String error);
    }

    public void loadLogFromStep(int stepId, int adapterPosition){
        _isLoading.postValue(true); // Bắt đầu loading

        // Gọi UseCase
        getLogEntryUseCase.execute(stepId, new GetLogEntryUseCase.GetLogEntryCallback() {

            @Override
            public void onSuccess(List<LogEntry> logs) {
                _isLoading.postValue(false); // Xong loading

                if (logs != null && !logs.isEmpty()) {
                    // Thành công!
                    // Gửi "Event" chứa cả vị trí và danh sách log
                    _logInsertWrapper.postValue(new LogInsertWrapper(adapterPosition, logs));
                } else {
                    // Không có log nào, không cần chèn gì cả
                    // (Bạn có thể gửi một event "empty" nếu muốn)
                }
            }

            @Override
            public void onError(String error) {
                _isLoading.postValue(false); // Xong loading
                _error.postValue(error); // Gửi lỗi
            }
        });
    }
}