package com.lkms.domain;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.lkms.data.model.java.Team;
import com.lkms.data.model.java.User;
import com.lkms.data.repository.ITeamRepository;
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AddMemberToTeamUseCase {

    private final ITeamRepository teamRepository;

    private final MutableLiveData<Boolean> addMembersSuccess = new MutableLiveData<>();
    public LiveData<Boolean> getAddMembersSuccess() {
        return addMembersSuccess;
    }

    private final MutableLiveData<String> addMembersError = new MutableLiveData<>();
    public LiveData<String> getAddMembersError() {
        return addMembersError;
    }


    public AddMemberToTeamUseCase(ITeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    public void execute(int experimentId, Set<User> selectedUsers) {
        if (selectedUsers == null || selectedUsers.isEmpty()) {
            // Sử dụng tên biến đã được đổi
            addMembersError.postValue("No users selected.");
            return;
        }

        List<Team> newTeamMembers = new ArrayList<>();
        for (User user : selectedUsers) {
            newTeamMembers.add(new Team(experimentId, user.getUserId(), LKMSConstantEnums.TeamStatus.ACTIVE.name()));
        }

        teamRepository.addMembers(newTeamMembers, new ITeamRepository.BulkTeamMemberCallback() {
            @Override
            public void onSuccess() {
                addMembersSuccess.postValue(true);
            }

            @Override
            public void onError(String errorMessage) {
                addMembersError.postValue(errorMessage);
            }
        });
    }
}
