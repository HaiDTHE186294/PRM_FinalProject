package com.lkms.ui.user_profile.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lkms.data.model.java.User;
import com.lkms.data.repository.implement.java.UserRepositoryImplJava;

import java.util.List;

public class MemberListViewModel extends ViewModel {

    private final UserRepositoryImplJava userRepository;
    private final MutableLiveData<List<User>> _memberList = new MutableLiveData<>();

    public LiveData<List<User>> getMemberList() {
        return _memberList;
    }

    // Constructor
    public MemberListViewModel(UserRepositoryImplJava userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads the list of all users from the repository and updates the LiveData.
     */
    public void loadAllUsers() {
        // Call the repository's suspend function and wait for the result
        userRepository.getAllUsers(new UserRepositoryImplJava.UserListCallback() {
            @Override
            public void onSuccess(List<User> users) {
                _memberList.postValue(users);
            }

            @Override
            public void onError(String errorMessage) {
                _memberList.postValue(null);
            }
        });
    }
}
