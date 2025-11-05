package com.lkms.data.repository;

import com.lkms.data.model.java.*;
import java.util.List;

/**
 * Giao diện Repository cho việc Quản lý Hồ sơ và Vai trò Người dùng.
 * Hỗ trợ chức năng User Profile & Role Management Activity (UC14) [3, 4].
 */
public interface IUserRepository {

    //region //Callback

    interface UserCallback {
        void onSuccess(User user);
        void onError(String errorMessage);
    }

    interface UserListCallback {
        void onSuccess(List<User> users);
        void onError(String errorMessage);
    }

    //endregion

    //region //Chức năng Quản lý Hồ sơ Cá nhân (UC14)

//    /**
//     * UC01: Lấy dữ liệu của người dùng sau khi login bằng email và password
//     *
//     * @param email email người dùng nhập vào
//     * @param password password người dùng nhập vào
//     */
//    void getUser(String email, String password);

    /**
     * UC14: Lấy dữ liệu người dùng dựa trên ID của người dùng đó
     *
     * @param userId Id của người dùng
     */
    void getUserById(int userId, UserCallback callback);

    /**
     * UC14: Cập nhật thông tin hồ sơ cá nhân của người dùng.
     * Cập nhật các trường "name" và "contactInfo" [1, 3] trong bảng "User" [1].
     *
     * @param userId Khóa chính của người dùng [1].
     * @param name Tên mới (trường "name" [1]).
     * @param contactInfo Thông tin liên hệ mới (trường "contactInfo" [1]).
     */
    void updateUserProfile(int userId, String name, String contactInfo, UserCallback callback);

    //endregion

    //region //Chức năng Quản lý Vai trò (Chỉ Lab Manager) (UC14)

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

    //endregion

    /**
     * Tìm kiếm người dùng dựa trên một chuỗi truy vấn (tên hoặc email).
     * @param query Chuỗi tìm kiếm (ví dụ: "john", "dev")
     * @param callback Callback để trả về danh sách người dùng tìm thấy.
     */
    void searchUsers(String query, UserListCallback callback);

    void checkIfMemberExists(int userId, int experimentId, MemberExistsCallback callback);

    // Interface callback mới
    interface MemberExistsCallback {
        void onResult(boolean exists);
        void onError(String errorMessage);
    }
}
