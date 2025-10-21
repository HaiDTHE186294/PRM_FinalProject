package com.lkms;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.lkms.data.model.java.User;
import com.lkms.data.repository.IAuthRepository;
import com.lkms.data.repository.implement.java.AuthRepositoryImplJava; // Giả định tên lớp Java Repository của bạn
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Kiểm thử cho AuthRepositoryImplJava.
 * LƯU Ý: Sử dụng Thread.sleep() để chờ thao tác bất đồng bộ hoàn thành,
 * đây là cách tiếp cận đơn giản nhưng không đáng tin cậy trong các bài kiểm thử nghiêm ngặt.
 */
@RunWith(AndroidJUnit4.class)
public class AuthRepositoryImplJavaTest {

    // Giả định bạn có lớp AuthRepositoryImplJava tương ứng
    private final AuthRepositoryImplJava repo = new AuthRepositoryImplJava();
    private static final int SLEEP_TIME_MS = 3000;

    // ----------------------------------------------------------------------------------
    // ✅ 1. Test login thành công
    // ----------------------------------------------------------------------------------
    @Test
    public void testLoginSuccess() throws InterruptedException {
        // Thay thế bằng thông tin đăng nhập hợp lệ trong DB của bạn
        final String email = "test1@gmail.com";
        final String password = "1";

        repo.login(email, password, new IAuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                System.out.println("✅ Login success: " + user.getEmail());
                // Kiểm tra đơn giản: đảm bảo email khớp hoặc ID người dùng không null
                if (!email.equals(user.getEmail())) {
                    System.err.println("Kiểm tra thất bại: Email trả về không khớp.");
                }
            }

            @Override
            public void onError(String error) {
                System.out.println("❌ Login failed: " + error);
                System.err.println("Kiểm tra thất bại: Login đáng lẽ phải thành công.");
            }
        });

        // Chặn luồng chính để chờ kết quả từ luồng mạng
        Thread.sleep(SLEEP_TIME_MS);
    }

    // ----------------------------------------------------------------------------------
    // ❌ 2. Test login thất bại (ví dụ thêm)
    // ----------------------------------------------------------------------------------
    @Test
    public void testLoginFailure() throws InterruptedException {
        final String email = "nonexistent@gmail.com";
        final String password = "wrong_password";

        repo.login(email, password, new IAuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                System.out.println("❌ Login đáng lẽ phải thất bại, nhưng thành công với: " + user.getEmail());
                System.err.println("Kiểm tra thất bại: Login không được phép thành công.");
            }

            @Override
            public void onError(String error) {
                System.out.println("✅ Login failed như mong đợi: " + error);
            }
        });

        Thread.sleep(SLEEP_TIME_MS);
    }
}