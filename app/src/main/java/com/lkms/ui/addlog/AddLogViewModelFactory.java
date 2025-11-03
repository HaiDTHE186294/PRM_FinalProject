// Đặt trong package: com.lkms.ui.addlog
package com.lkms.ui.addlog;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.lkms.domain.logentry.AddLogUseCase;

public class AddLogViewModelFactory implements ViewModelProvider.Factory {

    private final Application application;
    private final AddLogUseCase addLogUseCase;

    public AddLogViewModelFactory(
            Application application,
            AddLogUseCase addLogUseCase
    ) {
        this.application = application;
        this.addLogUseCase = addLogUseCase;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AddLogViewModel.class)) {
            // Tạo và trả về AddLogViewModel với các dependency
            return (T) new AddLogViewModel(application, addLogUseCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}