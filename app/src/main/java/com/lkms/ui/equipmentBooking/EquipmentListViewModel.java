package com.lkms.ui.equipmentBooking;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lkms.data.model.java.Equipment;
import com.lkms.data.repository.IEquipmentRepository;
import com.lkms.domain.EquipmentBookingUseCase;

import java.util.List;

public class EquipmentListViewModel extends ViewModel {

    private final EquipmentBookingUseCase useCase;

    private final MutableLiveData<List<Equipment>> _equipmentList = new MutableLiveData<>();
    public LiveData<List<Equipment>> equipmentList = _equipmentList;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    public EquipmentListViewModel(EquipmentBookingUseCase useCase) {
        this.useCase = useCase;
    }

    public void loadEquipmentList() {
        useCase.loadEquipmentList(new IEquipmentRepository.EquipmentListCallback() {
            @Override
            public void onSuccess(List<Equipment> list) {
                _equipmentList.postValue(list); // postValue để thread background
            }

            @Override
            public void onError(String message) {
                _error.postValue(message);
            }
        });
    }
}
