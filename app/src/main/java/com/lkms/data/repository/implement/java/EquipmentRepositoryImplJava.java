package com.lkms.data.repository.implement.java;

import static com.lkms.BuildConfig.SUPABASE_ANON_KEY;
import static com.lkms.BuildConfig.SUPABASE_URL;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lkms.data.model.java.*;
import com.lkms.data.repository.IEquipmentRepository;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EquipmentRepositoryImplJava implements IEquipmentRepository {

    private static final Gson gson = new Gson();

    // -------------------- GET ALL --------------------
    @Override
    public void getAllEquipment(EquipmentListCallback callback) {
        new Thread(() -> {
            try {
                String endpoint = SUPABASE_URL + "/rest/v1/Equipment?select=*";
                String json = getJson(endpoint);
                Type listType = new TypeToken<List<Equipment>>() {}.getType();
                List<Equipment> list = gson.fromJson(json, listType);
                callback.onSuccess(list);
            } catch (Exception e) {
                callback.onError("Lỗi khi lấy danh sách thiết bị: " + e.getMessage());
            }
        }).start();
    }

    // -------------------- GET DETAIL BY ID --------------------
    @Override
    public void getEquipmentDetails(int equipmentId, EquipmentCallback callback) {
        new Thread(() -> {
            try {
                String endpoint = SUPABASE_URL + "/rest/v1/Equipment?select=*&equipmentId=eq." + equipmentId;
                String json = getJson(endpoint);
                Type listType = new TypeToken<List<Equipment>>() {}.getType();
                List<Equipment> list = gson.fromJson(json, listType);
                if (list != null && !list.isEmpty()) callback.onSuccess(list.get(0));
                else callback.onError("Không tìm thấy thiết bị ID: " + equipmentId);
            } catch (Exception e) {
                callback.onError("Lỗi khi lấy chi tiết thiết bị: " + e.getMessage());
            }
        }).start();
    }

    // -------------------- ADD EQUIPMENT --------------------
    @Override
    public void addEquipment(Equipment newEquipment, EquipmentIdCallback callback) {
        new Thread(() -> {
            try {
                String endpoint = SUPABASE_URL + "/rest/v1/Equipment";
                String jsonBody = gson.toJson(newEquipment);
                String response = postJson(endpoint, jsonBody);

                // Trả về ID mới (nếu Supabase có trả)
                callback.onSuccess(0); // hoặc parse từ response nếu có ID
            } catch (Exception e) {
                callback.onError("Lỗi khi thêm thiết bị: " + e.getMessage());
            }
        }).start();
    }

    // -------------------- GET MAINTENANCE LOGS --------------------
    @Override
    public void getMaintenanceLogs(int equipmentId, MaintenanceLogCallback callback) {
        new Thread(() -> {
            try {
                String endpoint = SUPABASE_URL + "/rest/v1/MaintenanceLog?select=*&equipmentId=eq." + equipmentId;
                String json = getJson(endpoint);
                Type listType = new TypeToken<List<MaintenanceLog>>() {}.getType();
                List<MaintenanceLog> logs = gson.fromJson(json, listType);
                callback.onSuccess(logs);
            } catch (Exception e) {
                callback.onError("Lỗi khi lấy log bảo trì: " + e.getMessage());
            }
        }).start();
    }

    // -------------------- GET MANUAL (PDF LINK) --------------------
    @Override
    public void getManualDownloadUrl(int equipmentId, StringCallback callback) {
        new Thread(() -> {
            try {
                // 🔹 B1: Lấy serialNumber từ bảng Equipment
                String eqEndpoint = SUPABASE_URL + "/rest/v1/Equipment?select=serialNumber&equipmentId=eq." + equipmentId;
                String eqJson = getJson(eqEndpoint);

                if (!eqJson.contains("serialNumber")) {
                    callback.onError("Không tìm thấy serialNumber cho thiết bị ID=" + equipmentId);
                    return;
                }

                // 🔹 B2: Parse serialNumber ra
                String serial = eqJson.split("\"serialNumber\":\"")[1].split("\"")[0];

                // 🔹 B3: Dùng serialNumber để lấy url trong bảng UserManual
                String manualEndpoint = SUPABASE_URL + "/rest/v1/UserManual?select=url&manualId=eq." + serial;
                String manualJson = getJson(manualEndpoint);

                if (manualJson.contains("url")) {
                    String url = manualJson.split("\"url\":\"")[1].split("\"")[0];
                    callback.onSuccess(url.trim());
                } else {
                    callback.onError("Không tìm thấy URL cho manualId=" + serial);
                }

            } catch (Exception e) {
                callback.onError("Lỗi khi lấy manual: " + e.getMessage());
            }
        }).start();
    }


    // -------------------- GET BOOKINGS --------------------
    @Override
    public void getEquipmentBookings(int equipmentId, String startDate, String endDate, BookingListCallback callback) {
        new Thread(() -> {
            try {
                String endpoint = SUPABASE_URL + "/rest/v1/Booking?select=*&equipmentId=eq." + equipmentId
                        + "&startTime=gte." + startDate + "&endTime=lte." + endDate;
                String json = getJson(endpoint);
                Type listType = new TypeToken<List<Booking>>() {}.getType();
                List<Booking> bookings = gson.fromJson(json, listType);
                callback.onSuccess(bookings);
            } catch (Exception e) {
                callback.onError("Lỗi khi lấy danh sách booking: " + e.getMessage());
            }
        }).start();
    }

    // -------------------- CREATE BOOKING --------------------
    @Override
    public void createBooking(int userId, int equipmentId, int experimentId, String startTime, String endTime, BookingIdCallback callback) {
        new Thread(() -> {
            try {
                String endpoint = SUPABASE_URL + "/rest/v1/Booking";
                String jsonBody = String.format(
                        "{\"userId\":%d,\"equipmentId\":%d,\"experimentId\":%d,\"startTime\":\"%s\",\"endTime\":\"%s\"}",
                        userId, equipmentId, experimentId, startTime, endTime);
                postJson(endpoint, jsonBody);
                callback.onSuccess(0);
            } catch (Exception e) {
                callback.onError("Lỗi khi tạo booking: " + e.getMessage());
            }
        }).start();
    }

    // -------------------- APPROVE / REJECT BOOKING --------------------
    @Override
    public void processBookingApproval(int bookingId, boolean approve, String rejectReason, GenericCallback callback) {
        new Thread(() -> {
            try {
                String endpoint = SUPABASE_URL + "/rest/v1/Booking?bookingId=eq." + bookingId;
                String jsonBody = approve ?
                        "{\"status\":\"approved\"}" :
                        "{\"status\":\"rejected\",\"rejectReason\":\"" + rejectReason + "\"}";
                patchJson(endpoint, jsonBody);
                callback.onSuccess();
            } catch (Exception e) {
                callback.onError("Lỗi khi cập nhật trạng thái booking: " + e.getMessage());
            }
        }).start();
    }

    // -------------------- GET BY ID (SHORT VERSION) --------------------
    @Override
    public void getEquipmentById(int equipmentId, EquipmentCallback callback) {
        getEquipmentDetails(equipmentId, callback);
    }

    // -------------------- GET MANUAL BY SERIAL --------------------
    @Override
    public void getManualBySerialNumber(String serialNumber, StringCallback callback) {
        new Thread(() -> {
            try {
                String endpoint = SUPABASE_URL + "/rest/v1/UserManual?select=url&manualId=eq." + serialNumber;
                String json = getJson(endpoint);

                if (json != null && json.contains("url")) {
                    // ✅ Lấy chính xác phần giữa "url":" và "}]
                    String url = json.split("\"url\":\"")[1]
                            .split("\"")[0]
                            .trim();

                    callback.onSuccess(url);
                } else {
                    callback.onError("Không tìm thấy URL cho manualId: " + serialNumber);
                }
            } catch (Exception e) {
                callback.onError("Lỗi khi lấy manual: " + e.getMessage());
            }
        }).start();
    }


    // ===============================================================
    // 🔧 Helper methods cho GET / POST / PATCH JSON
    // ===============================================================
    private String getJson(String endpoint) throws Exception {
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("apikey", SUPABASE_ANON_KEY);
        conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_ANON_KEY);
        conn.setRequestProperty("Accept", "application/json");

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        return sb.toString();
    }

    private String postJson(String endpoint, String jsonBody) throws Exception {
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("apikey", SUPABASE_ANON_KEY);
        conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_ANON_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        return sb.toString();
    }

    private String patchJson(String endpoint, String jsonBody) throws Exception {
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PATCH");
        conn.setRequestProperty("apikey", SUPABASE_ANON_KEY);
        conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_ANON_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Prefer", "return=representation");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        return sb.toString();
    }
}
