package com.lkms.data.repository;

import com.lkms.data.model.User;

/**
 * Giao diện Repository cho các chức năng liên quan đến Xác thực (Authentication).
 * Hỗ trợ chức năng Đăng nhập và Đăng xuất (UC1) [3, 4].
 */
public interface IAuthRepository {

    /**
     * Thực hiện xác thực người dùng.
     * Dữ liệu đầu vào: User ID/Email và Password [4].
     *
     * @param email Email hoặc ID của người dùng (tương đương trường "email" trong bảng "User") [1].
     * @param password Mật khẩu của người dùng (tương đương trường "password" trong bảng "User") [1].
     * @return AuthResult chứa token và vai trò của người dùng nếu thành công [3, 4].
     * Giả định: Sử dụng một cơ chế bất đồng bộ cho môi trường Java/Android (ví dụ: Callback<AuthResult>).
     */
    void login(String email, String password, AuthCallback callback);

    /**
     * Xóa token xác thực được lưu trữ trên thiết bị và đăng xuất khỏi hệ thống.
     *
     * @param callback Cơ chế báo cáo hoàn thành.
     */
    void logout(LogoutCallback callback);

    // Giao diện callback để xử lý kết quả bất đồng bộ trong Java

    interface AuthCallback {
        void onSuccess(User user);
        //  void onSuccess(AuthResult result);
        void onError(String errorMessage);
    }

    interface LogoutCallback {
        void onComplete();
        void onError(String errorMessage);
    }
}
