package com.lkms.domain;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.lkms.data.model.java.Role;
import com.lkms.data.repository.IRoleRepository;
import com.lkms.data.repository.implement.java.RoleRepositoryImplJava;

import java.util.List;

public class RoleUseCase
{
    private final RoleRepositoryImplJava roleRepository = new RoleRepositoryImplJava();
    private final MutableLiveData<List<Role>> _roleList = new MutableLiveData<>();

    public LiveData<List<Role>> getRoleList() {
        return _roleList;
    }

    /**
     * Loads the list of all users from the repository and updates the LiveData.
     */
    public void loadAllRoles() {
        roleRepository.getAllRoles(new IRoleRepository.RoleListCallback() {
            @Override
            public void onSuccess(List<Role> roles) {
                _roleList.postValue(roles);
            }

            @Override
            public void onError(String errorMessage) {
                _roleList.postValue(null);
            }
        });
    }
}
