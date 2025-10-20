package com.lkms.data.repository.implement.java;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lkms.data.dal.SupabaseClient;
import com.lkms.data.model.User;
import com.lkms.data.repository.IAuthRepository;

import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthRepositoryImpl implements IAuthRepository {

    private static final String SUPABASE_URL = "https://your-project.supabase.co/rest/v1"; // chỉnh lại URL thật
    private static final String SUPABASE_API_KEY = "your-api-key"; // thêm key nếu cần
    private static final Gson gson = new Gson();

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    @Override
    public void login(String email, String password, AuthCallback callback) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            if (callback != null) callback.onError("Email and Password is not blanked.");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                // Gọi API REST của Supabase (dạng PostgREST)
                String endpoint = SUPABASE_URL + "/User?select=*&email=eq." + email + "&password=eq." + password;

                String json = getJson(endpoint);

                Type listType = new TypeToken<List<User>>() {}.getType();
                List<User> users = gson.fromJson(json, listType);

                if (users != null && !users.isEmpty()) {
                    if (callback != null) callback.onSuccess(users.get(0));
                } else {
                    if (callback != null) callback.onError("Email or Password is wrong.");
                }
            } catch (Exception e) {
                if (callback != null) callback.onError("Login failed: " + e.getMessage());
            }
        }, executor);
    }

    @Override
    public void logout(LogoutCallback callback) {
    }

    // Hàm gọi GET Supabase (thay thế cho coroutine client trong Kotlin)
    private String getJson(String endpoint) throws Exception {
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("apikey", SUPABASE_API_KEY);
        conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_API_KEY);
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("HTTP error code: " + responseCode);
        }

        try (Scanner scanner = new Scanner(conn.getInputStream())) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }
}
