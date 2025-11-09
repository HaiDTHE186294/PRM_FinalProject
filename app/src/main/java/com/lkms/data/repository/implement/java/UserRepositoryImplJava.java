package com.lkms.data.repository.implement.java;

import static com.lkms.BuildConfig.SUPABASE_URL;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lkms.data.model.java.Item;
import com.lkms.data.model.java.User;
import com.lkms.data.repository.IUserRepository;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;


public class UserRepositoryImplJava implements IUserRepository {

    private static final Gson gson = new Gson();


    @Override
    public void addUser(User user, UserCallback callback)
    {
        new Thread(() -> {
            if (callback == null) return;
            try {
                if (user == null) {
                    callback.onError("User cannot be null.");
                    return;
                }

                //Hash user's password
                String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
                user.setPassword(hashedPassword);

                String endpoint = SUPABASE_URL + "/rest/v1/User?select=*";
                String jsonBody = gson.toJson(user);

                // Supabase POST trả về bản ghi đã tạo
                String response = HttpHelper.postJson(endpoint, jsonBody);

                Type listType = new TypeToken<List<Item>>() {}.getType();
                List<User> createdUsers = gson.fromJson(response, listType);

                if (createdUsers != null && !createdUsers.isEmpty()) {
                    callback.onSuccess(createdUsers.get(0));
                } else {
                    callback.onError("Failed to add new user. Server returned empty response.");
                }

            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Failed to add new user");
            }
        }).start();
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
    @Override
    public void searchUsers(String query, UserListCallback callback) {
        new Thread(() -> {
            try {
                String endpoint;
                String finalQuery = (query == null) ? "" : query.trim();

                if (finalQuery.isEmpty()) {
                    // Nếu query rỗng, lấy tất cả user, giới hạn 20 người đầu
                    endpoint = SUPABASE_URL + "/rest/v1/User?select=*&limit=20";
                } else {
                    // Nếu có query, tìm kiếm theo tên hoặc email
                    String searchQuery = "%" + finalQuery + "%";
                    String encodedQuery = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8.toString());
                    String searchFilter = "or=(name.ilike." + encodedQuery + ",email.ilike." + encodedQuery + ")";
                    endpoint = SUPABASE_URL + "/rest/v1/User?select=*&" + searchFilter;
                }

                String json = HttpHelper.getJson(endpoint);

                Type listType = new TypeToken<List<User>>() {}.getType();
                List<User> users = gson.fromJson(json, listType);

                if (users != null) {
                    callback.onSuccess(users);
                } else {
                    // Trả về danh sách rỗng nếu kết quả là null để tránh crash
                    callback.onSuccess(new ArrayList<>());
                }

            } catch (Exception e) {
                callback.onError("Lỗi khi tìm kiếm người dùng: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void checkIfMemberExists(int userId, int experimentId, MemberExistsCallback callback) {
        new Thread(() -> {
            try {
                // Xây dựng URL để đếm số lượng bản ghi khớp
                // eq. = equals
                // and(...) = kết hợp nhiều điều kiện
                String endpoint = SUPABASE_URL + "/rest/v1/Team"
                        + "?select=*" // Chỉ cần select bất cứ thứ gì để đếm
                        + "&and=(userId.eq." + userId
                        + ",experimentId.eq." + experimentId
                        + ",status.eq.ACTIVE)";

                // Quan trọng: Thêm header "Prefer: count=exact" để Supabase chỉ trả về số lượng
                // mà không trả về toàn bộ dữ liệu.
                int count = HttpHelper.getCount(endpoint);

                // Nếu count > 0, nghĩa là thành viên đã tồn tại với status ACTIVE
                callback.onResult(count > 0);

            } catch (Exception e) {
                callback.onError("Lỗi khi kiểm tra thành viên: " + e.getMessage());
            }
        }).start();



    }
}
