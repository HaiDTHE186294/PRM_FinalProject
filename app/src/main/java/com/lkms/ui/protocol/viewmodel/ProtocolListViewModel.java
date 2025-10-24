// File: ProtocolListViewModel.java
package com.lkms.ui.protocol.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

// ✅ SỬA 1: Import các lớp từ package `domain` (nếu bạn đã di chuyển chúng)
import com.lkms.data.model.java.Protocol;
import com.lkms.data.repository.IProtocolRepository;

// ✅ SỬA 2: Import TẤT CẢ các UseCase cần thiết cho màn hình này
import com.lkms.domain.protocolUsecase.FilterProtocolsUseCase;
import com.lkms.domain.protocolUsecase.GetAllProtocolsUseCase;
import com.lkms.domain.protocolUsecase.GetLatestApprovedProtocolsUseCase;
import com.lkms.domain.protocolUsecase.SearchProtocolsUseCase;

// ✅ SỬA 3: Import implement của Repository từ lớp `data`
import com.lkms.data.repository.implement.java.ProtocolRepositoryImplJava;

import java.util.List;

public class ProtocolListViewModel extends ViewModel {

    // ✅ SỬA 4: Khai báo các UseCase thay vì một Repository duy nhất
    private final GetLatestApprovedProtocolsUseCase getLatestApprovedProtocolsUseCase;
    private final SearchProtocolsUseCase searchProtocolsUseCase;
    private final FilterProtocolsUseCase filterProtocolsUseCase;
    private final GetAllProtocolsUseCase getAllProtocolsUseCase;


    // (Các LiveData và Callback không thay đổi)
    private final MutableLiveData<List<Protocol>> _protocols = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> _error = new MutableLiveData<>();

    private final IProtocolRepository.ProtocolListCallback protocolListCallback = new IProtocolRepository.ProtocolListCallback() {
        @Override
        public void onSuccess(List<Protocol> list) {
            _protocols.postValue(list);
            _isLoading.postValue(false);
        }

        @Override
        public void onError(String errorMessage) {
            _error.postValue(errorMessage);
            _isLoading.postValue(false);
        }
    };

    // "Phơi bày" LiveData công khai (Không thay đổi)
    public LiveData<List<Protocol>> getProtocols() { return _protocols; }
    public LiveData<Boolean> isLoading() { return _isLoading; }
    public LiveData<String> getError() { return _error; }


    // ✅ SỬA 5: Cập nhật hàm khởi tạo (Constructor) để tạo các UseCase
    public ProtocolListViewModel() {
        // Cách khởi tạo tạm thời không dùng Dependency Injection:
        IProtocolRepository repository = new ProtocolRepositoryImplJava();

        // Dùng repository để khởi tạo tất cả các use case cần thiết
        this.getLatestApprovedProtocolsUseCase = new GetLatestApprovedProtocolsUseCase(repository);
        this.searchProtocolsUseCase = new SearchProtocolsUseCase(repository);
        this.filterProtocolsUseCase = new FilterProtocolsUseCase(repository);
        this.getAllProtocolsUseCase = new GetAllProtocolsUseCase(repository);
    }

    // --- ✅ SỬA 6: CÁC HÀM CHỨC NĂNG GỌI TỚI USECASE TƯƠNG ỨNG ---

    public void loadLatestApprovedLibrary() {
        _isLoading.postValue(true);
        // Gọi UseCase thay vì repository
        getLatestApprovedProtocolsUseCase.execute(protocolListCallback);
    }

    public void searchProtocols(String query) {
        if (query == null || query.trim().isEmpty()) {
            loadLatestApprovedLibrary();
            return;
        }
        _isLoading.postValue(true);
        // Gọi UseCase thay vì repository
        searchProtocolsUseCase.execute(query, protocolListCallback);
    }

    public void filterProtocols(Integer creatorId, String versionNumber) {
        _isLoading.postValue(true);
        // Gọi UseCase thay vì repository
        filterProtocolsUseCase.execute(creatorId, versionNumber, protocolListCallback);
    }

    public void loadAllProtocols() {
        _isLoading.postValue(true);
        // Gọi UseCase thay vì repository
        getAllProtocolsUseCase.execute(protocolListCallback);
    }
}
