package com.lkms.activities.viewmodel.factory; // Create a 'factory' sub-package

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.lkms.data.repository.implement.UserRepositoryImpl;
import com.lkms.activities.viewmodel.UserProfileViewModel;

public class UserProfileViewModelFactory implements ViewModelProvider.Factory {

    private final UserRepositoryImpl userRepository;

    public UserProfileViewModelFactory(UserRepositoryImpl userRepository) {
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
