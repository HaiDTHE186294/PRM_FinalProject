// File: ProtocolDetailViewModel.java
package com.lkms.ui.protocol.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lkms.data.model.java.Protocol;
import com.lkms.data.model.java.ProtocolItem;
import com.lkms.data.model.java.ProtocolStep;
import com.lkms.data.repository.IProtocolRepository;
import com.lkms.domain.protocolusecase.GetProtocolDetailsUseCase;
import com.lkms.data.repository.implement.java.ProtocolRepositoryImplJava;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ProtocolDetailViewModel extends ViewModel {

    private final GetProtocolDetailsUseCase getProtocolDetailsUseCase;

    private final MutableLiveData<Protocol> protocol = new MutableLiveData<>();
    private final MutableLiveData<List<ProtocolStep>> steps = new MutableLiveData<>();
    private final MutableLiveData<List<ProtocolItem>> items = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<Protocol> getProtocol() {
        return protocol;
    }

    public LiveData<List<ProtocolStep>> getSteps() {
        return steps;
    }

    public LiveData<List<ProtocolItem>> getItems() {
        return items;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public ProtocolDetailViewModel() {
        IProtocolRepository repository = new ProtocolRepositoryImplJava();
        this.getProtocolDetailsUseCase = new GetProtocolDetailsUseCase(repository);
    }

    public void loadProtocolDetails(int protocolId) {
        isLoading.postValue(true);

        final AtomicInteger dataPacketCounter = new AtomicInteger(3);
        final AtomicInteger loadingOffSignal = new AtomicInteger(0);

        getProtocolDetailsUseCase.execute(protocolId, new IProtocolRepository.ProtocolContentCallback() {
            private void checkAndTurnOffLoading() {
                // Hợp nhất hai câu lệnh if bằng toán tử &&
                if (dataPacketCounter.decrementAndGet() == 0 && loadingOffSignal.compareAndSet(0, 1)) {
                    isLoading.postValue(false);
                }
            }

            @Override
            public void onProtocolReceived(Protocol receivedProtocol) {
                protocol.postValue(receivedProtocol);
                checkAndTurnOffLoading();
            }

            @Override
            public void onStepsReceived(List<ProtocolStep> receivedSteps) {
                steps.postValue(receivedSteps);
                checkAndTurnOffLoading();
            }

            @Override
            public void onItemsReceived(List<ProtocolItem> receivedItems) {
                items.postValue(receivedItems);
                checkAndTurnOffLoading();
            }

            @Override
            public void onError(String errorMessage) {
                error.postValue(errorMessage);
                if (loadingOffSignal.compareAndSet(0, 1)) {
                    isLoading.postValue(false);
                }
            }
        });
    }
}
