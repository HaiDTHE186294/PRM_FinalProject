
package com.lkms.ui.protocol.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lkms.data.model.java.Protocol;
import com.lkms.data.repository.IProtocolRepository;
import com.lkms.data.repository.implement.java.ProtocolRepositoryImplJava;
import com.lkms.domain.protocolusecase.FilterProtocolsUseCase;
import com.lkms.domain.protocolusecase.GetAllProtocolsUseCase;
import com.lkms.domain.protocolusecase.GetLatestApprovedProtocolsUseCase;
import com.lkms.domain.protocolusecase.SearchProtocolsUseCase;

import java.util.List;

public class ProtocolListViewModel extends ViewModel {

    private final GetLatestApprovedProtocolsUseCase getLatestApprovedProtocolsUseCase;
    private final SearchProtocolsUseCase searchProtocolsUseCase;
    private final FilterProtocolsUseCase filterProtocolsUseCase;
    private final GetAllProtocolsUseCase getAllProtocolsUseCase;

    //Đổi tên các biến LiveData để tuân thủ quy ước của Java (bỏ dấu gạch dưới)
    private final MutableLiveData<List<Protocol>> protocols = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private final IProtocolRepository.ProtocolListCallback protocolListCallback = new IProtocolRepository.ProtocolListCallback() {
        @Override
        public void onSuccess(List<Protocol> list) {

            protocols.postValue(list);
            isLoading.postValue(false);
        }

        @Override
        public void onError(String errorMessage) {

            error.postValue(errorMessage);
            isLoading.postValue(false);
        }
    };

    // "Phơi bày" LiveData công khai
    public LiveData<List<Protocol>> getProtocols() {
        return protocols;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public ProtocolListViewModel() {
        IProtocolRepository repository = new ProtocolRepositoryImplJava();
        this.getLatestApprovedProtocolsUseCase = new GetLatestApprovedProtocolsUseCase(repository);
        this.searchProtocolsUseCase = new SearchProtocolsUseCase(repository);
        this.filterProtocolsUseCase = new FilterProtocolsUseCase(repository);
        this.getAllProtocolsUseCase = new GetAllProtocolsUseCase(repository);
    }

    // --- CÁC HÀM CHỨC NĂNG GỌI TỚI USECASE TƯƠNG ỨNG ---

    public void loadLatestApprovedLibrary() {

        isLoading.postValue(true);
        getLatestApprovedProtocolsUseCase.execute(protocolListCallback);
    }

    public void searchProtocols(String query) {
        // Hợp nhất câu lệnh if để giảm độ phức tạp
        if (query == null || query.trim().isEmpty()) {
            loadLatestApprovedLibrary();
            return;
        }


        isLoading.postValue(true);
        searchProtocolsUseCase.execute(query, protocolListCallback);
    }

    public void filterProtocols(Integer creatorId, String versionNumber) {

        isLoading.postValue(true);
        filterProtocolsUseCase.execute(creatorId, versionNumber, protocolListCallback);
    }

    public void loadAllProtocols() {

        isLoading.postValue(true);
        getAllProtocolsUseCase.execute(protocolListCallback);
    }
}
