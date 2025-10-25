package com.lkms.domain;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.lkms.data.model.java.User;
import com.lkms.data.repository.IUserRepository;
import com.lkms.data.repository.implement.java.UserRepositoryImplJava;

import java.util.List;

public class MemberManagementUseCase {
    private final UserRepositoryImplJava repository = new UserRepositoryImplJava();
    private final MutableLiveData<List<User>> _memberList = new MutableLiveData<>();

    public LiveData<List<User>> getAllMembers() {
        return _memberList;
    }

    /**
     * Loads the list of all users from the repository and updates the LiveData.
     */
    public void loadAllMembers() {
        repository.getAllUsers(new IUserRepository.UserListCallback() {
            @Override
            public void onSuccess(List<User> roles) {
                _memberList.postValue(roles);
            }

            @Override
            public void onError(String errorMessage) {
                _memberList.postValue(null);
            }
        });
    }
}
