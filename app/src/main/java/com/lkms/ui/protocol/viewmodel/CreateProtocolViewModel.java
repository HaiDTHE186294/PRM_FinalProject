package com.lkms.ui.protocol.viewmodel;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lkms.data.model.java.Item;
import com.lkms.data.model.java.Protocol;
import com.lkms.data.model.java.ProtocolItem;
import com.lkms.data.model.java.ProtocolStep;
import com.lkms.data.repository.IInventoryRepository;
import com.lkms.data.repository.IProtocolRepository;
import com.lkms.data.repository.implement.java.InventoryRepositoryImplJava;
import com.lkms.data.repository.implement.java.ProtocolRepositoryImplJava;
import com.lkms.domain.protocolusecase.CreateNewProtocolUseCase;

import java.util.List;

public class CreateProtocolViewModel extends ViewModel {

    private static final String TAG = "CreateProtocolVM";

    private final CreateNewProtocolUseCase createProtocolUseCase;
    // Bỏ UseCase, thay bằng Repository
    private final IInventoryRepository inventoryRepository;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading() { return _isLoading; }

    private final MutableLiveData<Integer> _creationSuccess = new MutableLiveData<>();
    public LiveData<Integer> getCreationSuccess() { return _creationSuccess; }

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> getError() { return _error; }

    private final MutableLiveData<List<Item>> _items = new MutableLiveData<>();
    public LiveData<List<Item>> getItems() { return _items; }

    private boolean areItemsLoaded = false;

    public CreateProtocolViewModel() {
        this.createProtocolUseCase = new CreateNewProtocolUseCase(new ProtocolRepositoryImplJava());
        // Khởi tạo Repository trực tiếp
        this.inventoryRepository = new InventoryRepositoryImplJava();
    }

    public void loadInitialData() {
        if (areItemsLoaded) {
            return;
        }
        areItemsLoaded = true;
        loadAllAvailableItems();
    }

    private void loadAllAvailableItems() {
        _isLoading.postValue(true);
        Log.d(TAG, "Bắt đầu tải danh sách vật tư (TRỰC TIẾP TỪ REPO)...");

        // Gọi thẳng vào repository
        inventoryRepository.getAllInventoryItems(new IInventoryRepository.InventoryListCallback() {
            @Override
            public void onSuccess(List<Item> items) {
                _items.postValue(items);
                _isLoading.postValue(false);
                Log.d(TAG, "Tải thành công (TRỰC TIẾP) " + (items != null ? items.size() : 0) + " vật tư.");
            }

            @Override
            public void onError(String errorMessage) {
                _error.postValue("Lỗi tải vật tư (TRỰC TIẾP): " + errorMessage);
                _isLoading.postValue(false);
                Log.e(TAG, "Lỗi khi tải vật tư (TRỰC TIẾP): " + errorMessage);
            }
        });
    }

    public void createProtocol(Protocol protocolData, List<ProtocolStep> steps, List<ProtocolItem> items, int creatorId) {
        // ... hàm này không thay đổi
        _isLoading.postValue(true);
        _error.postValue(null);

        if (creatorId == -1) {
            _error.postValue("Không thể xác thực người dùng. Vui lòng đăng nhập lại.");
            _isLoading.postValue(false);
            return;
        }

        createProtocolUseCase.execute(protocolData, steps, items, creatorId, new IProtocolRepository.ProtocolIdCallback() {
            @Override
            public void onSuccess(int protocolId) {
                _creationSuccess.postValue(protocolId);
                _isLoading.postValue(false);
            }

            @Override
            public void onError(String errorMessage) {
                _error.postValue(errorMessage);
                _isLoading.postValue(false);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        _creationSuccess.setValue(null);
        _error.setValue(null);
    }
}
