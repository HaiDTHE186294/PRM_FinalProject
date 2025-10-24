// Tạo file mới: ExperimentDetailViewModel.java
// (Thường nằm trong package: com.lkms.ui.experimentdetail)

package com.lkms.ui.experimentdetail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lkms.data.model.java.ExperimentStep;
import com.lkms.data.model.java.ProtocolStep;
import com.lkms.data.model.java.combine.ExperimentProtocolStep;
import com.lkms.ui.experimentdetail.adapter.AdapterItem;
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

    // 2. LiveData để giữ trạng thái cho UI
    //    Activity sẽ "lắng nghe" (observe) các LiveData này

    // Dữ liệu khi thành công
    private final MutableLiveData<List<AdapterItem>> _adapterItems = new MutableLiveData<>();
    public LiveData<List<AdapterItem>> adapterItems = _adapterItems;

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
            GetProtocolStepBasedOnExperimentStepUseCase getProtocolStepUseCase // Thêm dependency
    ) {
        this.getExperimentStepUseCase = getExperimentStepUseCase;
        this.getProtocolStepUseCase = getProtocolStepUseCase; // Gán dependency
    }

    // 4. Phương thức mà Activity sẽ gọi
    public void loadExperimentData(int experimentId) {
        _isLoading.setValue(true); // Bắt đầu loading

        // Bước 1: Lấy danh sách ExperimentStep
        getExperimentStepUseCase.execute(experimentId, new GetExperimentStepUseCase.getExperimentStepsListCallback() {

            @Override
            public void onSuccess(List<ExperimentStep> originalSteps) {
                // Bước 1 thành công!
                // THAY ĐỔI: Không cập nhật LiveData vội.
                // Thay vào đó, bắt đầu Bước 2: Lấy ProtocolSteps
                initializeFlatList(originalSteps, new FlatListCallback() {
                    @Override
                    public void onFlatListReady(List<AdapterItem> flatList) {
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
}