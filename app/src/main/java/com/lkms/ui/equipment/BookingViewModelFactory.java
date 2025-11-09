package com.lkms.ui.equipment;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class BookingViewModelFactory implements ViewModelProvider.Factory {

    private final int userId;

    public BookingViewModelFactory(int userId) {
        this.userId = userId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(BookingViewModel.class)) {
            return (T) new BookingViewModel(userId);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}