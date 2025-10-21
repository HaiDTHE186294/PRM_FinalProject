package com.lkms.ui.user_profile.viewmodel.factory; // Create a 'factory' sub-package

import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.lkms.data.repository.implement.java.UserRepositoryImplJava;
import com.lkms.ui.user_profile.viewmodel.UserProfileViewModel;

public class UserProfileViewModelFactory implements ViewModelProvider.Factory {

    private final UserRepositoryImplJava userRepository;

    public UserProfileViewModelFactory(UserRepositoryImplJava userRepository) {
        this.userRepository = userRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(UserProfileViewModel.class)) {
            return (T) new UserProfileViewModel(userRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
