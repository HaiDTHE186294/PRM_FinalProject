// File: ProtocolDetailViewModel.java
package com.lkms.ui.protocol.viewmodel; // Giữ nguyên package của bạn

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

// ✅ SỬA 1: Import các lớp từ package `domain` (nếu bạn đã di chuyển chúng)
// Nếu chưa, hãy giữ nguyên đường dẫn cũ, nó vẫn sẽ hoạt động.
import com.lkms.data.model.java.Protocol;
import com.lkms.data.model.java.ProtocolItem;
import com.lkms.data.model.java.ProtocolStep;
import com.lkms.data.repository.IProtocolRepository;

// ✅ SỬA 2: Import UseCase bạn đã tạo
import com.lkms.domain.protocolUsecase.GetProtocolDetailsUseCase;

// ✅ SỬA 3: Import implement của Repository từ lớp `data`
import com.lkms.data.repository.implement.java.ProtocolRepositoryImplJava;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ProtocolDetailViewModel extends ViewModel {

    // ✅ SỬA 4: Khai báo UseCase thay vì Repository
    private final GetProtocolDetailsUseCase getProtocolDetailsUseCase;

    // (Các LiveData không thay đổi)
    private final MutableLiveData<Protocol> _protocol = new MutableLiveData<>();
    private final MutableLiveData<List<ProtocolStep>> _steps = new MutableLiveData<>();
    private final MutableLiveData<List<ProtocolItem>> _items = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> _error = new MutableLiveData<>();

    // "Phơi bày" LiveData công khai (Không thay đổi)
    public LiveData<Protocol> getProtocol() { return _protocol; }
    public LiveData<List<ProtocolStep>> getSteps() { return _steps; }
    public LiveData<List<ProtocolItem>> getItems() { return _items; }
    public LiveData<Boolean> isLoading() { return _isLoading; }
    public LiveData<String> getError() { return _error; }


    // ✅ SỬA 5: Cập nhật hàm khởi tạo (Constructor)
    public ProtocolDetailViewModel() {
        // Cách khởi tạo tạm thời không dùng Dependency Injection:
        // 1. Tạo một instance của Repository từ lớp Data.
        IProtocolRepository repository = new ProtocolRepositoryImplJava();
        // 2. Dùng repository đó để tạo instance cho UseCase.
        this.getProtocolDetailsUseCase = new GetProtocolDetailsUseCase(repository);
    }


    /**
     * Tải toàn bộ dữ liệu chi tiết cho một protocol dựa vào ID.
     * @param protocolId ID của protocol cần tải.
     */
    public void loadProtocolDetails(int protocolId) {
        _isLoading.postValue(true);

        final AtomicInteger dataPacketCounter = new AtomicInteger(3);
        final AtomicInteger loadingOffSignal = new AtomicInteger(0);

        // ✅ SỬA 6: Gọi `execute` từ UseCase thay vì gọi `repository`
        getProtocolDetailsUseCase.execute(protocolId, new IProtocolRepository.ProtocolContentCallback() {
            private void checkAndTurnOffLoading() {
                if (dataPacketCounter.decrementAndGet() == 0) {
                    if (loadingOffSignal.compareAndSet(0, 1)) {
                        _isLoading.postValue(false);
                    }
                }
            }

            @Override
            public void onProtocolReceived(Protocol protocol) {
                _protocol.postValue(protocol);
                checkAndTurnOffLoading();
            }

            @Override
            public void onStepsReceived(List<ProtocolStep> steps) {
                _steps.postValue(steps);
                checkAndTurnOffLoading();
            }

            @Override
            public void onItemsReceived(List<ProtocolItem> items) {
                _items.postValue(items);
                checkAndTurnOffLoading();
            }

            @Override
            public void onError(String errorMessage) {
                _error.postValue(errorMessage);
                if (loadingOffSignal.compareAndSet(0, 1)) {
                    _isLoading.postValue(false);
                }
            }
        });
    }
}
