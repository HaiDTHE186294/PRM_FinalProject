// Đặt trong package: com.lkms.ui.addlog
package com.lkms.ui.addlog;

/**
 * Một lớp "sealed" (giả lập trong Java) để đại diện
 * cho các trạng thái của hoạt động upload.
 */
public abstract class UploadState {
    private UploadState() {} // Ngăn không cho kế thừa từ bên ngoài

    // Trạng thái chờ, mặc định
    public static final class Idle extends UploadState {}

    // Đang xử lý (upload hoặc sao chép file)
    public static final class Loading extends UploadState {}

    // Thành công, mang theo ID của log mới
    public static final class Success extends UploadState {
        private final int newLogId;
        public Success(int newLogId) { this.newLogId = newLogId; }
        public int getNewLogId() { return newLogId; }
    }

    // Thất bại, mang theo thông báo lỗi
    public static final class Error extends UploadState {
        private final String message;
        public Error(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
}