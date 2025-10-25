package com.lkms.data.repository.implement.java;

import static com.lkms.BuildConfig.SUPABASE_ANON_KEY;
import static com.lkms.BuildConfig.SUPABASE_URL;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lkms.data.model.java.*;
import com.lkms.data.repository.IEquipmentRepository;
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums;

import java.lang.reflect.Type;
import java.util.List;

public class EquipmentRepositoryImplJava implements IEquipmentRepository {

    private static final Gson gson = new Gson();

    // -------------------- GET ALL --------------------
    @Override
    public void getAllEquipment(EquipmentListCallback callback) {
        new Thread(() -> {
            try {
                String endpoint = SUPABASE_URL + "/rest/v1/Equipment?select=*";
                String json = HttpHelper.getJson(endpoint);
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
                String json = HttpHelper.getJson(endpoint);
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
                String response = HttpHelper.postJson(endpoint, jsonBody);
                callback.onSuccess(0);
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
                String json = HttpHelper.getJson(endpoint);
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
                String eqEndpoint = SUPABASE_URL + "/rest/v1/Equipment?select=serialNumber&equipmentId=eq." + equipmentId;
                String eqJson = HttpHelper.getJson(eqEndpoint);

                if (!eqJson.contains("serialNumber")) {
                    callback.onError("Không tìm thấy serialNumber cho thiết bị ID=" + equipmentId);
                    return;
                }

                String serial = eqJson.split("\"serialNumber\":\"")[1].split("\"")[0];
                String manualEndpoint = SUPABASE_URL + "/rest/v1/UserManual?select=url&manualId=eq." + serial;
                String manualJson = HttpHelper.getJson(manualEndpoint);

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
                String json = HttpHelper.getJson(endpoint);
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
                String endpoint = SUPABASE_URL + "/rest/v1/Booking?select=*";
                String jsonBody = String.format(
                        "{\"userId\":%d,\"equipmentId\":%d,\"experimentId\":%d,\"startTime\":\"%s\",\"endTime\":\"%s\",\"bookingStatus\":\"%s\"}",
                        userId, equipmentId, experimentId, startTime, endTime, LKMSConstantEnums.BookingStatus.PENDING
                );

                String response = HttpHelper.postJson(endpoint, jsonBody);

                Type listType = new TypeToken<List<Booking>>() {}.getType();
                List<Booking> created = gson.fromJson(response, listType);

                if (created != null && !created.isEmpty() && created.get(0).getBookingId() != null) {
                    callback.onSuccess(created.get(0).getBookingId());
                } else {
                    callback.onError("Không thể tạo booking mới.");
                }

            } catch (Exception e) {
                callback.onError("Ngày này đã được đặt");
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
                HttpHelper.patchJson(endpoint, jsonBody);
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
                String json = HttpHelper.getJson(endpoint);

                if (json != null && json.contains("url")) {
                    String url = json.split("\"url\":\"")[1].split("\"")[0].trim();
                    callback.onSuccess(url);
                } else {
                    callback.onError("Không tìm thấy URL cho manualId: " + serialNumber);
                }
            } catch (Exception e) {
                callback.onError("Lỗi khi lấy manual: " + e.getMessage());
            }
        }).start();
    }


    @Override
    public void getBookingApproved(int userId, BookingListCallback callback) {
        new Thread(() -> {
            try {
                String endpoint = SUPABASE_URL + "/rest/v1/Booking?select=*"
                        + "&userId=eq." + userId;

                String json = HttpHelper.getJson(endpoint);

                Type listType = new TypeToken<List<Booking>>() {}.getType();
                List<Booking> bookings = gson.fromJson(json, listType);

                callback.onSuccess(bookings);
            } catch (Exception e) {
                callback.onError("Lỗi khi tải danh sách booking được duyệt: " + e.getMessage());
            }
        }).start();
    }

}
