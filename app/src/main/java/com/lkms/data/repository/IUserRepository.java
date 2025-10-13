package com.lkms.data.repository;

import com.lkms.data.model.User; // Ánh xạ bảng User [1]
import java.util.List;

/**
 * Giao diện Repository cho việc Quản lý Hồ sơ và Vai trò Người dùng.
 * Hỗ trợ chức năng User Profile & Role Management Activity (UC14) [3, 4].
 */
public interface IUserRepository {

    // --- Các giao diện Callback ---

    interface UserCallback {
        void onSuccess(User user);
        void onError(String errorMessage);
    }

    interface UserListCallback {
        void onSuccess(List<User> users);
        void onError(String errorMessage);
    }

    // --- Chức năng Quản lý Hồ sơ Cá nhân (UC14) ---

    /**
     * UC14: Cập nhật thông tin hồ sơ cá nhân của người dùng.
     * Cập nhật các trường "name" và "contactInfo" [1, 3] trong bảng "User" [1].
     *
     * @param userId Khóa chính của người dùng [1].
     * @param name Tên mới (trường "name" [1]).
     * @param contactInfo Thông tin liên hệ mới (trường "contactInfo" [1]).
     */
    void updateUserProfile(int userId, String name, String contactInfo, UserCallback callback);

    // --- Chức năng Quản lý Vai trò (Chỉ Lab Manager) (UC14) ---

    /**
     * UC14: Lấy danh sách tất cả người dùng trong phòng thí nghiệm.
     * Dùng cho chức năng "Manage Team" của Lab Manager [3, 4].
     * Truy vấn bảng "User" [1].
     */
    void getAllUsers(UserListCallback callback);

    /**
     * UC14: Thay đổi vai trò người dùng (Chỉ Lab Manager).
     * Cập nhật trường "roleId" [1] trong bảng "User" [1].
     *
     * @param targetUserId Khóa chính của người dùng cần thay đổi [1].
     * @param newRoleId ID vai trò mới (trường "roleId" [1]).
     */
    void updateUserRole(int targetUserId, int newRoleId, UserCallback callback);
}
