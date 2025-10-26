package com.lkms.data.repository.implement.java;

import static com.lkms.BuildConfig.SUPABASE_URL;
import com.lkms.BuildConfig;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lkms.data.model.java.User;
import com.lkms.data.repository.IAuthRepository;

import org.mindrot.jbcrypt.BCrypt;

import java.util.Date;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Triển khai IAuthRepository sử dụng Supabase REST API
 * Dùng Thread + HttpHelper thay vì coroutine.
 */
public class AuthRepositoryImplJava implements IAuthRepository {

    private static final Gson gson = new Gson();

    // -------------------- LOGIN --------------------
    @Override
    public void login(String email, String password, AuthCallback callback) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            callback.onError("Email và mật khẩu không được để trống.");
            return;
        }

        new Thread(() -> {
            try {
                // Chỉ query theo email, không query theo password
                String endpoint = SUPABASE_URL + "/rest/v1/User?select=*"
                        + "&email=eq." + email;

                String json = HttpHelper.getJson(endpoint);

                Type listType = new TypeToken<List<User>>() {}.getType();
                List<User> users = gson.fromJson(json, listType);

                if (users != null && !users.isEmpty()) {
                    User user = users.get(0);

                    // ✅ So sánh password nhập với hash trong DB
                    if (BCrypt.checkpw(password, user.getPassword())) {

                        // ✅ Tạo JWT token cho user
                        String SECRET_KEY = BuildConfig.JWT_SECRET; // ⚠️ Chỉ demo
                        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);

                        String token = JWT.create()
                                .withIssuer("LKMS_APP")
                                .withClaim("userId", user.getUserId())
                                .withClaim("email", user.getEmail())
                                .withClaim("roleId", user.getRoleId())
                                .withIssuedAt(new Date())
                                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) // 1 ngày
                                .sign(algorithm);

                        user.setToken(token); // gắn token vào user
                        callback.onSuccess(user);

                    } else {
                        callback.onError("Email hoặc mật khẩu không đúng.");
                    }
                } else {
                    callback.onError("Email hoặc mật khẩu không đúng.");
                }

            } catch (Exception e) {
                callback.onError("Đăng nhập thất bại: " + e.getMessage());
            }
        }).start();
    }
//
//    @Override
//    public void login(String email, String password, AuthCallback callback) {
//        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
//            callback.onError("Email và mật khẩu không được để trống.");
//            return;
//        }
//
//        new Thread(() -> {
//            try {
//                String endpoint = SUPABASE_URL + "/rest/v1/User?select=*"
//                        + "&email=eq." + email
//                        + "&password=eq." + password;
//
//                String json = HttpHelper.getJson(endpoint);
//
//                Type listType = new TypeToken<List<User>>() {}.getType();
//                List<User> users = gson.fromJson(json, listType);
//
//                if (users != null && !users.isEmpty()) {
//                    callback.onSuccess(users.get(0));
//                } else {
//                    callback.onError("Email hoặc mật khẩu không đúng.");
//                }
//
//            } catch (Exception e) {
//                callback.onError("Đăng nhập thất bại: " + e.getMessage());
//            }
//        }).start();
//    }

    // -------------------- LOGOUT --------------------
    @Override
    public void logout(LogoutCallback callback) {
        //handle logout trong controller
    }
}
