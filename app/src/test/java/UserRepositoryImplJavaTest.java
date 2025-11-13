package com.lkms;

// 1. XÓA "import android.util.Log;" VÀ THÊM THƯ VIỆN LOG CỦA JAVA
import java.util.logging.Logger;

import com.lkms.data.model.java.User;
import com.lkms.data.repository.IUserRepository;
import com.lkms.data.repository.implement.java.UserRepositoryImplJava;

import org.junit.Test;

import java.util.List;

/**
 * Test class cho UserRepositoryImplJava.
 * Giúp kiểm tra kết nối, CRUD, và callback hoạt động đúng.
 * Đã sửa để dùng Logger của Java thay vì Log của Android.
 */
public class UserRepositoryImplJavaTest {

    // 2. KHỞI TẠO LOGGER CỦA JAVA
    private static final Logger logger = Logger.getLogger(UserRepositoryImplJavaTest.class.getName());

    private final UserRepositoryImplJava repo = new UserRepositoryImplJava();

    //region === COMMON USER TEST ===

    @Test
    public void testGetUserById() {
        // 3. THAY THẾ Log.d BẰNG logger.info
        logger.info("🔍 Testing getUserById...");

        repo.getUserById(2, new IUserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                logger.info("✅ User retrieved: " + user);
            }

            @Override
            public void onError(String errorMessage) {
                // 3. THAY THẾ Log.e BẰNG logger.severe (hoặc warning)
                logger.severe("❌ Error: " + errorMessage);
            }
        });

        // Chờ thread chạy xong
        sleep();
    }

    @Test
    public void testUpdateUserProfile() {
        logger.info("📝 Testing updateUserProfile...");

        repo.updateUserProfile(2, "Test", "TOKYO-Test", new IUserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                logger.info("✅ Updated user: " + user);
            }

            @Override
            public void onError(String errorMessage) {
                logger.severe("❌ Error updating: " + errorMessage);
            }
        });

        sleep();
    }

    //endregion

    //region === LAB MANAGER TEST ===

    @Test
    public void testGetAllUsers() {
        logger.info("📋 Testing getAllUsers...");

        repo.getAllUsers(new IUserRepository.UserListCallback() {

            @Override
            public void onSuccess(List<User> users) {
                if (users == null || users.isEmpty()) {
                    logger.severe("⚠️ No users found");
                    return;
                }

                logger.info("✅ Retrieved users:");
                for (User u : users) {
                    logger.info(" - " + u);
                }
            }

            @Override
            public void onError(String errorMessage) {
                logger.severe("❌ Error: " + errorMessage);
            }
        });

        sleep();
    }

    @Test
    public void testUpdateUserRole() {
        logger.info("🎭 Testing updateUserRole...");

        repo.updateUserRole(2, 1, new IUserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                logger.info("✅ Role updated successfully: " + user);
            }

            @Override
            public void onError(String errorMessage) {
                logger.severe("❌ Error: " + errorMessage);
            }
        });

        sleep();
    }

    //endregion

    //region === UTILITY ===
    private void sleep() {
        try {
            Thread.sleep(3000); // đợi 3s cho thread chạy
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    //endregion
}