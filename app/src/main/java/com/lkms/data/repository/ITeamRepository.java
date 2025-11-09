package com.lkms.data.repository;

import com.lkms.data.model.java.Team;
import java.util.List;

/**
 * Interface để quản lý các thành viên trong nhóm (Team) của một thí nghiệm. */
public interface ITeamRepository {

    /**
     * Callback cho việc thêm một thành viên mới.
     */
    interface TeamMemberCallback {
        void onSuccess(Team teamMember);
        void onError(String errorMessage);
    }

    /**
     * Thêm một thành viên mới vào nhóm của một thí nghiệm.
     *
     * @param teamMember Đối tượng Team chứa experimentId, userId, và status.
     * @param callback   Callback để nhận kết quả.
     */
    void addMember(Team teamMember, TeamMemberCallback callback);



    /**
     * Interface callback cho các tác vụ thêm hàng loạt thành viên.
     * Dùng để báo kết quả về cho UseCase khi thêm nhiều người cùng lúc.
     */
    interface BulkTeamMemberCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    /**
     * Thêm một danh sách các thành viên vào bảng Team.
     * Chức năng này phục vụ cho việc "Add Member" nhiều người cùng lúc.
     *
     * @param teamMembers Danh sách các đối tượng Team cần thêm.
     * @param callback    Callback để nhận kết quả (thành công hoặc thất bại).
     */
    void addMembers(List<Team> teamMembers, BulkTeamMemberCallback callback);

}
