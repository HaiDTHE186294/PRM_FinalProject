package com.lkms.data.repository.implement.java;

import static com.lkms.BuildConfig.SUPABASE_URL;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lkms.data.model.java.Item;
import com.lkms.data.model.java.User;
import com.lkms.data.repository.IUserRepository;

import java.lang.reflect.Type;
import java.util.List;

public class UserRepositoryImplJava implements IUserRepository {

    private static final Gson gson = new Gson();


    @Override
    public void addUser(String fullName, String email, String password, UserCallback callback)
    {
        //TODO: Ask someone to work with this.
    }

    @Override
    public void getUserById(int userId, UserCallback callback) {
        new Thread(() -> {
            try {
                String endpoint = SUPABASE_URL + "/rest/v1/User?select=*&userId=eq." + userId;
                String json = HttpHelper.getJson(endpoint);

                Type listType = new TypeToken<List<User>>() {}.getType();
                List<User> users = gson.fromJson(json, listType);

                if (users == null || users.isEmpty()) {
                    callback.onError("Không tìm thấy userId=" + userId);
                    return;
                }

                callback.onSuccess(users.get(0));

            } catch (Exception e) {
                callback.onError("Lỗi khi lấy user: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void updateUserProfile(int userId, String name, String contactInfo, UserCallback callback) {
        new Thread(() -> {
            try {
                String endpoint = SUPABASE_URL + "/rest/v1/User?userId=eq." + userId;
                String jsonBody = "{\"name\":\"" + name + "\",\"contactInfo\":\"" + contactInfo + "\"}";

                HttpHelper.patchJson(endpoint, jsonBody);

                // Lấy lại user sau khi update
                String refreshedJson = HttpHelper.getJson(SUPABASE_URL + "/rest/v1/User?select=*&userId=eq." + userId);
                Type listType = new TypeToken<List<User>>() {}.getType();
                List<User> updatedUsers = gson.fromJson(refreshedJson, listType);

                callback.onSuccess(updatedUsers.get(0));

            } catch (Exception e) {
                callback.onError("Lỗi khi cập nhật: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void getAllUsers(UserListCallback callback) {
        new Thread(() -> {
            try {
                String endpoint = SUPABASE_URL + "/rest/v1/User?select=*";
                String json = HttpHelper.getJson(endpoint);

                Type listType = new TypeToken<List<User>>() {}.getType();
                List<User> users = gson.fromJson(json, listType);
                callback.onSuccess(users);

            } catch (Exception e) {
                callback.onError("Lỗi khi tải danh sách user: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void updateUserRole(int targetUserId, int newRoleId, UserCallback callback) {
        new Thread(() -> {
            try {
                String endpoint = SUPABASE_URL + "/rest/v1/User?userId=eq." + targetUserId;
                String jsonBody = "{\"roleId\":" + newRoleId + "}";

                HttpHelper.patchJson(endpoint, jsonBody);

                // Lấy lại user sau khi cập nhật
                String refreshedJson = HttpHelper.getJson(SUPABASE_URL + "/rest/v1/User?select=*&userId=eq." + targetUserId);
                Type listType = new TypeToken<List<User>>() {}.getType();
                List<User> updatedUsers = gson.fromJson(refreshedJson, listType);

                callback.onSuccess(updatedUsers.get(0));

            } catch (Exception e) {
                callback.onError("Lỗi khi cập nhật role: " + e.getMessage());
            }
        }).start();
    }
}
