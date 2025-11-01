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
    private final IInventoryRepository inventoryRepository;

    // --- SỬA LỖI ĐẶT TÊN: Bỏ dấu gạch dưới '_' ở đầu tên biến ---
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading() { return isLoading; }

    private final MutableLiveData<Integer> creationSuccess = new MutableLiveData<>();
    public LiveData<Integer> getCreationSuccess() { return creationSuccess; }

    private final MutableLiveData<String> error = new MutableLiveData<>();
    public LiveData<String> getError() { return error; }

    private final MutableLiveData<List<Item>> items = new MutableLiveData<>();
    public LiveData<List<Item>> getItems() { return items; }

    private boolean areItemsLoaded = false;

    public CreateProtocolViewModel() {
        this.createProtocolUseCase = new CreateNewProtocolUseCase(new ProtocolRepositoryImplJava());
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
        isLoading.postValue(true); // Sửa tên biến ở đây
        Log.d(TAG, "Bắt đầu tải danh sách vật tư...");
        inventoryRepository.getAllInventoryItems(new IInventoryRepository.InventoryListCallback() {
            @Override
            public void onSuccess(List<Item> result) {
                items.postValue(result); // Sửa tên biến ở đây
                isLoading.postValue(false); // Sửa tên biến ở đây
                Log.d(TAG, "Tải thành công " + (result != null ? result.size() : 0) + " vật tư.");
            }

            @Override
            public void onError(String errorMessage) {
                error.postValue("Lỗi tải vật tư: " + errorMessage); // Sửa tên biến ở đây
                isLoading.postValue(false); // Sửa tên biến ở đây
                Log.e(TAG, "Lỗi khi tải vật tư: " + errorMessage);
            }
        });
    }

    public void createProtocol(Protocol protocolData, List<ProtocolStep> steps, List<ProtocolItem> protocolItems, int creatorId) {
        isLoading.postValue(true); // Sửa tên biến ở đây
        error.postValue(null); // Sửa tên biến ở đây

        if (creatorId == -1) {
            error.postValue("Không thể xác thực người dùng. Vui lòng đăng nhập lại."); // Sửa tên biến ở đây
            isLoading.postValue(false); // Sửa tên biến ở đây
            return;
        }

        createProtocolUseCase.execute(protocolData, steps, protocolItems, creatorId, new IProtocolRepository.ProtocolIdCallback() {
            @Override
            public void onSuccess(int protocolId) {
                creationSuccess.postValue(protocolId); // Sửa tên biến ở đây
                isLoading.postValue(false); // Sửa tên biến ở đây
            }

            @Override
            public void onError(String errorMessage) {
                error.postValue(errorMessage); // Sửa tên biến ở đây
                isLoading.postValue(false); // Sửa tên biến ở đây
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Không bắt buộc phải set null ở đây, nhưng nếu có thì cũng sửa lại tên biến
        creationSuccess.setValue(null); // Sửa tên biến ở đây
        error.setValue(null); // Sửa tên biến ở đây
    }
}
