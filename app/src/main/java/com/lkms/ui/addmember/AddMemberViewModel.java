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

    // Dữ liệu cho UI, vẫn giữ nguyên
    public LiveData<List<User>> searchResults;
    public LiveData<String> error;
    public LiveData<Boolean> addMembersSuccess;
    public LiveData<String> addMembersError;

    public AddMemberViewModel(AddMemberUseCase addMemberUseCase, AddMemberToTeamUseCase addMemberToTeamUseCase) {
        this.addMemberUseCase = addMemberUseCase;
        this.addMemberToTeamUseCase = addMemberToTeamUseCase;

        // Gán LiveData từ các UseCase
        this.searchResults = addMemberUseCase.getSearchResults();
        this.error = addMemberUseCase.getError();
        this.addMembersSuccess = addMemberToTeamUseCase.getAddMembersSuccess();
        this.addMembersError = addMemberToTeamUseCase.getAddMembersError();
    }

    /**
     * ⭐ THAY ĐỔI: Hàm này bây giờ sẽ nhận experimentId để lọc. ⭐
     * Nó gọi UseCase để thực hiện logic tìm kiếm và lọc những user đã có trong team.
     * @param query Từ khóa tìm kiếm (có thể rỗng).
     * @param experimentId ID của experiment hiện tại.
     */
    public void findAvailableUsers(String query, int experimentId) {
        // ViewModel chỉ cần ra lệnh, UseCase sẽ lo phần còn lại.
        addMemberUseCase.findAvailableUsers(query, experimentId);
    }

    /**
     * Hàm thêm thành viên vào team, giữ nguyên như cũ.
     * @param experimentId ID của experiment.
     * @param selectedUsers Set các User được chọn.
     */
    public void addMembersToTeam(int experimentId, Set<User> selectedUsers) {
        addMemberToTeamUseCase.execute(experimentId, selectedUsers);
    }
}
