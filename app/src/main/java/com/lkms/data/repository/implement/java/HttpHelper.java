package com.lkms.data.repository.implement.java;

import static com.lkms.BuildConfig.SUPABASE_ANON_KEY;
import static com.lkms.BuildConfig.SUPABASE_URL;

import android.webkit.MimeTypeMap;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.net.URLConnection;

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
// 🔹 UPLOAD FILE - Upload file lên Supabase Storage
// ===============================================================
    public static String uploadFile(String bucketName, String path, File file) throws IOException {
        String SUPABASE_URL = com.lkms.BuildConfig.SUPABASE_URL;
        String SUPABASE_ANON_KEY = com.lkms.BuildConfig.SUPABASE_ANON_KEY;

        String uploadUrl = SUPABASE_URL + "/storage/v1/object/" + bucketName + "/" + path;

        HttpURLConnection conn = (HttpURLConnection) new URL(uploadUrl).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_ANON_KEY);
        conn.setRequestProperty("apikey", SUPABASE_ANON_KEY);

        // 🔸 Xác định Content-Type (tương thích API 24)
        String contentType = URLConnection.guessContentTypeFromName(file.getName());
        if (contentType == null) contentType = "application/octet-stream";
        conn.setRequestProperty("Content-Type", contentType);

        // 🔸 Ghi file vào request body
        try (OutputStream os = conn.getOutputStream();
             FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        }

        // 🔸 Đọc phản hồi từ server
        int responseCode = conn.getResponseCode();
        InputStream stream = (responseCode >= 200 && responseCode < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) response.append(line);
        }

        if (responseCode >= 200 && responseCode < 300) {
            // 🔸 Trả về public URL của file
            return SUPABASE_URL + "/storage/v1/object/public/" + bucketName + "/" + path;
        } else {
            throw new IOException("Upload failed (" + responseCode + "): " + response);
        }
    }

    // ===============================================================
    // 🔹 DOWNLOAD FILE - Download file từ Supabase Storage
    // ===============================================================
    /**
     * Tải file từ một URL (thường là public URL của Supabase Storage)
     * và lưu nó vào một file tạm.
     *
     * @param url URL công khai của file cần tải.
     * @return một đối tượng File trỏ đến file tạm đã được tải về.
     * @throws IOException nếu có lỗi mạng hoặc lỗi I/O.
     */
    public static File downloadFile(String url) throws IOException {
        URL fileUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) fileUrl.openConnection();
        conn.setRequestMethod("GET");

        // Lưu ý: Giả định URL là public (do hàm uploadFile trả về).
        // Nếu URL của bạn yêu cầu xác thực, bạn cần thêm header
        // 'Authorization' và 'apikey' giống như trong 'uploadFile'.

        int responseCode = conn.getResponseCode();

        if (responseCode >= 200 && responseCode < 300) {
            // 🔸 Tạo file tạm
            // File sẽ có tên dạng "supabase_download_12345.tmp"
            // 1. Lấy đường dẫn (path) từ URL, ví dụ: /storage/.../file.pdf
            String path = fileUrl.getPath();

            // 2. Tách lấy phần đuôi file (ví dụ: "pdf")
            // MimeTypeMap sẽ tự động xử lý các query param (như ?token=...)
            String extension = MimeTypeMap.getFileExtensionFromUrl(path);

            // 3. Tạo suffix. Mặc định là .tmp nếu không tìm thấy
            String suffix = ".tmp";
            if (extension != null && !extension.isEmpty()) {
                suffix = "." + extension;
            }

            // 4. Tạo file tạm với ĐÚNG đuôi file (ví dụ: "supabase_download_12345.pdf")
            File tempFile = File.createTempFile("supabase_download_", suffix);
            // Đảm bảo file tạm bị xóa khi ứng dụng tắt (phòng trường hợp crash)
            tempFile.deleteOnExit();

            // 🔸 Ghi dữ liệu từ InputStream (network) vào FileOutputStream (disk)
            try (InputStream is = conn.getInputStream();
                 FileOutputStream fos = new FileOutputStream(tempFile)) {

                byte[] buffer = new byte[8192]; // Dùng buffer 8K
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
                fos.flush();
            } finally {
                conn.disconnect();
            }

            return tempFile;

        } else {
            // 🔸 Xử lý lỗi (ví dụ: 404 Not Found)
            InputStream errorStream = conn.getErrorStream();
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(errorStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) response.append(line);
            } finally {
                conn.disconnect();
            }
            throw new IOException("Download failed (" + responseCode + "): " + response.toString());
        }
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
        conn.setRequestProperty("Prefer", "return=representation");
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

    // 🔹 GET COUNT - Đếm số lượng bản ghi
    // ===============================================================
    /**
     * Gửi một request GET đến Supabase với header đặc biệt để chỉ đếm số lượng kết quả
     * thay vì tải toàn bộ dữ liệu.
     *
     * @param endpoint URL của API Supabase với các tham số lọc.
     * @return số lượng bản ghi khớp với điều kiện lọc.
     * @throws IOException nếu có lỗi mạng hoặc request không thành công.
     */
    public static int getCount(String endpoint) throws IOException {
        HttpURLConnection conn = createConnection(endpoint, "GET");

        // Header đặc biệt để yêu cầu Supabase chỉ đếm và trả về tổng số trong header
        conn.setRequestProperty("Prefer", "count=exact");

        // Không cần gọi conn.connect() rõ ràng, getResponseCode sẽ tự làm điều đó.
        int responseCode = conn.getResponseCode();

        // Với request 'count', Supabase sẽ trả về HTTP 200 OK ngay cả khi kết quả là 0.
        // Dữ liệu thực sự nằm trong header 'Content-Range'.
        if (responseCode >= 200 && responseCode < 300) {
            String contentRange = conn.getHeaderField("Content-Range"); // ví dụ: "0-4/5" hoặc "*/0"

            if (contentRange != null && contentRange.contains("/")) {
                // Lấy phần total, ví dụ "5" từ "0-4/5"
                String totalStr = contentRange.substring(contentRange.indexOf('/') + 1);

                // Supabase có thể trả về '*' nếu không thể tính toán, coi như là 0
                if (!totalStr.equals("*")) {
                    try {
                        return Integer.parseInt(totalStr);
                    } catch (NumberFormatException e) {
                        // Ghi log lỗi nếu cần và trả về 0
                        return 0;
                    }
                }
            }
            // Nếu không có header hoặc header không đúng định dạng, trả về 0.
            return 0;
        } else {
            // Ném lỗi nếu request không thành công để bên ngoài có thể xử lý.
            // Có thể đọc error stream để có thông báo lỗi chi tiết hơn nếu cần.
            conn.disconnect();
            throw new IOException("HTTP error code: " + responseCode + " while trying to get count.");
        }
    }

}
