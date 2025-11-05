package com.lkms.domain;

// THÊM CÁC IMPORT NÀY:
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

    // --- SỬA Ở ĐÂY: Thêm 2 LiveData ---
    private final MutableLiveData<Boolean> _addMembersSuccess = new MutableLiveData<>();
    public LiveData<Boolean> getAddMembersSuccess() {
        return _addMembersSuccess;
    }

    private final MutableLiveData<String> _addMembersError = new MutableLiveData<>();
    public LiveData<String> getAddMembersError() {
        return _addMembersError;
    }
    // --- KẾT THÚC PHẦN SỬA ---


    public AddMemberToTeamUseCase(ITeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    // --- SỬA Ở ĐÂY: Bỏ tham số callback đi ---
    public void execute(int experimentId, Set<User> selectedUsers) {
        if (selectedUsers == null || selectedUsers.isEmpty()) {
            _addMembersError.postValue("No users selected.");
            return;
        }

        List<Team> newTeamMembers = new ArrayList<>();
        for (User user : selectedUsers) {
            newTeamMembers.add(new Team(experimentId, user.getUserId(), LKMSConstantEnums.TeamStatus.ACTIVE.name()));
        }

        // --- SỬA Ở ĐÂY: Tạo callback ngay tại đây ---
        teamRepository.addMembers(newTeamMembers, new ITeamRepository.BulkTeamMemberCallback() {
            @Override
            public void onSuccess() {
                _addMembersSuccess.postValue(true);
            }

            @Override
            public void onError(String errorMessage) {
                _addMembersError.postValue(errorMessage);
            }
        });
    }
}
