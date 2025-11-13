package com.lkms;

import android.util.Log;

import com.lkms.data.model.java.User;
import com.lkms.data.repository.IUserRepository;
import com.lkms.data.repository.implement.java.UserRepositoryImplJava;

import org.junit.Test;

import java.util.List;

/**
 * Test class cho UserRepositoryImplJava.
 * Giúp kiểm tra kết nối, CRUD, và callback hoạt động đúng.
 */
public class UserRepositoryImplJavaTest {

    private final UserRepositoryImplJava repo = new UserRepositoryImplJava();

    //region === COMMON USER TEST ===

    @Test
    public void testGetUserById() {
        Log.d("UserRepoTest", "🔍 Testing getUserById...");

        repo.getUserById(2, new IUserRepository.UserCallback() {
            // CORRECTED: Removed duplicate onSuccess method
            @Override
            public void onSuccess(User user) {
                Log.d("UserRepoTest", "✅ User retrieved: " + user);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("UserRepoTest", "❌ Error: " + errorMessage);
            }
        });

        // Chờ thread chạy xong
        sleep();
    }

    @Test
    public void testUpdateUserProfile() {
        Log.d("UserRepoTest", "📝 Testing updateUserProfile...");

        repo.updateUserProfile(2, "Test", "TOKYO-Test", new IUserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                Log.d("UserRepoTest", "✅ Updated user: " + user);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("UserRepoTest", "❌ Error updating: " + errorMessage);
            }
        });

        sleep();
    }

    //endregion

    //region === LAB MANAGER TEST ===

    @Test
    public void testGetAllUsers() {
        Log.d("UserRepoTest", "📋 Testing getAllUsers...");

        repo.getAllUsers(new IUserRepository.UserListCallback() {

            @Override
            public void onSuccess(List<User> users) {
                if (users == null || users.isEmpty()) {
                    Log.e("UserRepoTest", "⚠️ No users found");
                    return;
                }

                Log.d("UserRepoTest", "✅ Retrieved users:");
                for (User u : users) {
                    Log.d("UserRepoTest", " - " + u);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("UserRepoTest", "❌ Error: " + errorMessage);
            }
        });

        sleep();
    }

    @Test
    public void testUpdateUserRole() {
        Log.d("UserRepoTest", "🎭 Testing updateUserRole...");

        repo.updateUserRole(2, 1, new IUserRepository.UserCallback() {
            // CORRECTED: Removed duplicate onSuccess method
            @Override
            public void onSuccess(User user) {
                Log.d("UserRepoTest", "✅ Role updated successfully: " + user);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("UserRepoTest", "❌ Error: " + errorMessage);
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
