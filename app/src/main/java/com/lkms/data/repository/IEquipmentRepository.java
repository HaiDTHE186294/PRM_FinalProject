package com.lkms.data.repository;

import com.lkms.data.model.java.*;
import java.util.Date;
import java.util.List;

/**
 * Giao diện Repository cho việc Quản lý Thiết bị và Đặt chỗ (Equipment Booking).
 * Hỗ trợ UC9, UC10, và một phần của UC35.
 */
public interface IEquipmentRepository {

    // --- Các giao diện Callback ---

    interface EquipmentListCallback {
        void onSuccess(List<Equipment> equipmentList);
        void onError(String errorMessage);
    }

    interface EquipmentCallback {
        void onSuccess(Equipment equipment);
        void onError(String errorMessage);
    }
    
    interface EquipmentIdCallback {
        void onSuccess(int equipmentId); // Trả về ID của thiết bị mới
        void onError(String errorMessage);
    }

    interface BookingListCallback {
        void onSuccess(List<Booking> bookings);
        void onError(String errorMessage);
    }

    interface BookingIdCallback {
        void onSuccess(int bookingId); // Trả về ID đặt chỗ mới
        void onError(String errorMessage);
    }

    interface MaintenanceLogCallback {
        void onSuccess(List<MaintenanceLog> logs);
        void onError(String errorMessage);
    }

    interface StringCallback {
        void onSuccess(String url); // Dùng cho URL Manual hoặc thông báo
        void onError(String errorMessage);
    }

    interface GenericCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public interface BookingDisplayListCallback {
        void onSuccess(List<BookingDisplay> bookingDisplays);
        void onError(String message);
    }



    // --- Chức năng Quản lý Thiết bị (Equipment Management) ---

    /**
     * UC9: Lấy danh sách tất cả thiết bị có thể đặt chỗ.
     * Truy vấn bảng "Equipment" [1].
     */
    void getAllEquipment(EquipmentListCallback callback);

    /**
     * UC10: Lấy chi tiết thông tin thiết bị (model, serial number).
     */
    void getEquipmentDetails(int equipmentId, EquipmentCallback callback);

    /**
     * Thêm một thiết bị mới vào hệ thống (inventory).
     * Chèn dữ liệu vào bảng "Equipment".
     */
    void addEquipment(Equipment newEquipment, EquipmentIdCallback callback);

    /**
     * UC10: Lấy nhật ký bảo trì/hiệu chuẩn.
     * Truy vấn bảng "MaintenanceLog" [1, 2].
     */
    void getMaintenanceLogs(int equipmentId, MaintenanceLogCallback callback);

    /**
     * UC10: Lấy URL để tải xuống Sổ tay Hướng dẫn (PDF).
     * Truy vấn bảng "UserManual" [2].
     */
    void getManualDownloadUrl(int equipmentId, StringCallback callback);

    // --- Chức năng Đặt chỗ (Booking) ---

    /**
     * UC9: Lấy các đặt chỗ hiện có cho một thiết bị trong một khoảng thời gian.
     * Truy vấn bảng "Booking" [2] để hiển thị lịch (Calendar view).
     */
    void getEquipmentBookings(int equipmentId, String startDate, String endDate, BookingListCallback callback);

    /**
     * UC9: Tạo yêu cầu đặt chỗ mới.
     * Chèn dữ liệu vào bảng "Booking" [2] với "bookingStatus" là Pending.
     * Server sẽ chịu trách nhiệm xác thực xung đột thời gian.
     */
    void createBooking(int userId, int equipmentId, int experimentId, String startTime, String endTime, BookingIdCallback callback);

    // --- Chức năng Phê duyệt (Approval - Dành cho Lab Manager) ---

    /**
     * UC35: Xử lý phê duyệt Đặt chỗ.
     * Cập nhật trường "bookingStatus" và "rejectReason" trong bảng "Booking" [2].
     * Hàm này được gọi sau khi Lab Manager tương tác với màn hình Approve Checkout/Booking.
     */
    void processBookingApproval(int bookingId, boolean approve, String rejectReason, GenericCallback callback);

    /**
     * Lấy thông tin thiết bị theo ID (tách riêng với getEquipmentDetails nếu cần dùng riêng trong logic khác).
     */
    void getEquipmentById(int equipmentId, EquipmentCallback callback);

    /**
     * Lấy đường dẫn manual dựa theo serialNumber (manualId = serialNumber).
     */
    void getManualBySerialNumber(String serialNumber, StringCallback callback);


    void getBookingApproved(int userId, BookingListCallback callback);
}
