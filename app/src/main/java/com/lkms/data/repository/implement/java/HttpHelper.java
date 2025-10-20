package com.lkms.data.repository.implement.java;

import static com.lkms.BuildConfig.SUPABASE_ANON_KEY;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * HttpHelper - tiện ích chung cho toàn bộ repository.
 * Hỗ trợ CRUD (GET, POST, PUT, PATCH, DELETE) với Supabase REST API.
 */
public class HttpHelper {

    // ===============================================================
    // 🔹 GET - lấy dữ liệu JSON
    // ===============================================================
    public static String getJson(String endpoint) throws IOException {
        HttpURLConnection conn = createConnection(endpoint, "GET");
        return readResponse(conn);
    }

    // ===============================================================
    // 🔹 POST - thêm mới bản ghi
    // ===============================================================
    public static String postJson(String endpoint, String jsonBody) throws IOException {
        HttpURLConnection conn = createConnection(endpoint, "POST");
        writeRequestBody(conn, jsonBody);
        return readResponse(conn);
    }

    // ===============================================================
    // 🔹 PUT - cập nhật toàn bộ bản ghi
    // ===============================================================
    public static String putJson(String endpoint, String jsonBody) throws IOException {
        HttpURLConnection conn = createConnection(endpoint, "PUT");
        writeRequestBody(conn, jsonBody);
        return readResponse(conn);
    }

    // ===============================================================
    // 🔹 PATCH - cập nhật một phần bản ghi
    // ===============================================================
    public static String patchJson(String endpoint, String jsonBody) throws IOException {
        HttpURLConnection conn = createConnection(endpoint, "PATCH");
        writeRequestBody(conn, jsonBody);
        return readResponse(conn);
    }

    // ===============================================================
    // 🔹 DELETE - xóa bản ghi
    // ===============================================================
    public static int delete(String endpoint) throws IOException {
        HttpURLConnection conn = createConnection(endpoint, "DELETE");
        conn.connect();
        int code = conn.getResponseCode();
        conn.disconnect();
        return code;
    }

    // ===============================================================
    // ⚙️ Private helper methods
    // ===============================================================

    /**
     * Tạo kết nối HTTP cơ bản với các header mặc định của Supabase.
     */
    private static HttpURLConnection createConnection(String endpoint, String method) throws IOException {
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod(method);
        conn.setRequestProperty("apikey", SUPABASE_ANON_KEY);
        conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_ANON_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoInput(true);

        // Các phương thức cần body (POST, PUT, PATCH)
        if (!method.equals("GET") && !method.equals("DELETE")) {
            conn.setDoOutput(true);
        }

        return conn;
    }

    /**
     * Ghi request body dạng JSON.
     */
    private static void writeRequestBody(HttpURLConnection conn, String jsonBody) throws IOException {
        if (jsonBody == null || jsonBody.isEmpty()) return;

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
            os.flush();
        }
    }

    /**
     * Đọc phản hồi từ server — cả success lẫn error message.
     */
    private static String readResponse(HttpURLConnection conn) throws IOException {
        int status = conn.getResponseCode();
        InputStream stream = (status >= 200 && status < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) response.append(line);
            return response.toString();
        } finally {
            conn.disconnect();
        }
    }
}
