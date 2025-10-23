package com.lkms.ui.equipmentBooking;

import android.os.Build;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lkms.data.repository.IEquipmentRepository;
import com.lkms.data.repository.implement.java.EquipmentRepositoryImplJava;
import com.lkms.domain.EquipmentBookingUseCase;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BookingViewModel extends ViewModel {

    private final EquipmentBookingUseCase useCase;

    private final MutableLiveData<LocalDate> _startDate = new MutableLiveData<>();
    public LiveData<LocalDate> startDate = _startDate;

    private final MutableLiveData<LocalDate> _endDate = new MutableLiveData<>();
    public LiveData<LocalDate> endDate = _endDate;

    private final MutableLiveData<Integer> _selectedExperimentId = new MutableLiveData<>();
    public LiveData<Integer> selectedExperimentId = _selectedExperimentId;

    private final MutableLiveData<Boolean> _bookingResult = new MutableLiveData<>();
    public LiveData<Boolean> bookingResult = _bookingResult;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    private final int userId = 1; // tạm thời
    private int equipmentId;

    public BookingViewModel() {
        this.useCase = new EquipmentBookingUseCase(new EquipmentRepositoryImplJava());
    }

    public void setEquipmentId(int equipmentId) {
        this.equipmentId = equipmentId;
    }

    public void selectStartDate(LocalDate date) {
        _startDate.postValue(date);
    }

    public void selectEndDate(LocalDate date) {
        _endDate.postValue(date);
    }

    public void selectExperiment(int experimentId) {
        _selectedExperimentId.postValue(experimentId);
    }

    public void bookEquipment() {
        LocalDate start = _startDate.getValue();
        LocalDate end = _endDate.getValue();
        Integer experimentId = _selectedExperimentId.getValue();

        if (start == null || end == null) {
            _error.postValue("Chưa chọn đầy đủ ngày bắt đầu và ngày kết thúc");
            return;
        }

        if (experimentId == null) {
            _error.postValue("Chưa chọn experiment");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && end.isBefore(start)) {
            _error.postValue("Ngày kết thúc phải sau ngày bắt đầu");
            return;
        }

        LocalDateTime startTime = start.atStartOfDay();
        LocalDateTime endTime = end.atStartOfDay();

        useCase.bookEquipment(userId, equipmentId, startTime, endTime, experimentId,
                new IEquipmentRepository.BookingIdCallback() {
                    @Override
                    public void onSuccess(int bookingId) {
                        _bookingResult.postValue(true);
                    }

                    @Override
                    public void onError(String message) {
                        _bookingResult.postValue(false);
                        _error.postValue(message);
                    }
                });
    }
}
