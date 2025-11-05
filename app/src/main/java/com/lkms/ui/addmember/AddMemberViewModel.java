package com.lkms.ui.addmember;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.lkms.data.model.java.User;
import com.lkms.domain.AddMemberUseCase;
import com.lkms.domain.AddMemberToTeamUseCase;
import java.util.List;
import java.util.Set;

public class AddMemberViewModel extends ViewModel {

    private final AddMemberUseCase addMemberUseCase;
    private final AddMemberToTeamUseCase addMemberToTeamUseCase;

    // ⭐ SỬA 1: Chuyển các LiveData thành private để đảm bảo tính đóng gói
    private final LiveData<List<User>> searchResults;
    private final LiveData<String> error;
    private final LiveData<Boolean> addMembersSuccess;
    private final LiveData<String> addMembersError;

    public AddMemberViewModel(AddMemberUseCase addMemberUseCase, AddMemberToTeamUseCase addMemberToTeamUseCase) {
        this.addMemberUseCase = addMemberUseCase;
        this.addMemberToTeamUseCase = addMemberToTeamUseCase;

        // Gán giá trị từ UseCase, không thay đổi
        this.searchResults = addMemberUseCase.getSearchResults();
        this.error = addMemberUseCase.getError();
        this.addMembersSuccess = addMemberToTeamUseCase.getAddMembersSuccess();
        this.addMembersError = addMemberToTeamUseCase.getAddMembersError();
    }

    // ⭐ SỬA 2: Cung cấp các phương thức getter công khai (public)
    public LiveData<List<User>> getSearchResults() {
        return searchResults;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getAddMembersSuccess() {
        return addMembersSuccess;
    }

    public LiveData<String> getAddMembersError() {
        return addMembersError;
    }


    /**
     * Ra lệnh cho UseCase thực hiện tìm kiếm và lọc người dùng.
     * @param query Từ khóa tìm kiếm.
     * @param experimentId ID của experiment hiện tại.
     */
    public void findAvailableUsers(String query, int experimentId) {
        // ViewModel chỉ cần ra lệnh, UseCase sẽ lo phần còn lại.
        addMemberUseCase.findAvailableUsers(query, experimentId);
    }

    /**
     * Ra lệnh cho UseCase thực hiện thêm thành viên vào team.
     * @param experimentId ID của experiment.
     * @param selectedUsers Set các User được chọn.
     */
    public void addMembersToTeam(int experimentId, Set<User> selectedUsers) {
        addMemberToTeamUseCase.execute(experimentId, selectedUsers);
    }
}
