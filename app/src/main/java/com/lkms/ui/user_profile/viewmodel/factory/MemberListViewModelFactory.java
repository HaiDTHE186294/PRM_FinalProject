package com.lkms.ui.user_profile.viewmodel.factory;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.lkms.data.repository.implement.java.UserRepositoryImplJava;
import com.lkms.ui.user_profile.viewmodel.MemberListViewModel;

public class MemberListViewModelFactory implements ViewModelProvider.Factory {

    private final UserRepositoryImplJava userRepository;

    public MemberListViewModelFactory(UserRepositoryImplJava userRepository) {
        this.userRepository = userRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MemberListViewModel.class))
            return (T) new MemberListViewModel(userRepository);
        else
            throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
