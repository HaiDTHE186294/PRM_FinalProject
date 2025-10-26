package com.lkms.data.repository.enumPackage;

public class LKMSConstantEnums {

    // =========================================================================
    // I. ENUMS VỀ VAI TRÒ (ROLE)
    // =========================================================================

    /**
     * Vai trò người dùng (roleId) trong hệ thống [1, 3].
     * Xác định quyền truy cập và các tính năng hiển thị [1].
     */
    public enum UserRole {
        LAB_MANAGER,      // Quản trị viên phòng thí nghiệm, có quyền phê duyệt [1, 4].
        RESEARCHER,       // Nhà nghiên cứu/Khoa học, người tạo và quản lý thí nghiệm chính [1].
        TECHNICIAN        // Kỹ thuật viên, tập trung vào quản lý kho và bảo trì thiết bị [1, 5].
    }

    // =========================================================================
    // II. ENUMS VỀ TRẠNG THÁI CHUNG (STATUS)
    // =========================================================================

    /**
     * Trạng thái chung của người dùng (userStatus) [3].
     */
    public enum UserStatus {
        ACTIVE,           // Hoạt động
        INACTIVE,         // Không hoạt động
        SUSPENDED         // Bị đình chỉ
    }

    /**
     * Trạng thái của một vai trò (roleStatus) [3].
     */
    public enum RoleStatus {
        ACTIVE,           // Hoạt động
        DEPRECATED        // Không còn được sử dụng
    }

    /**
     * Trạng thái của thành viên trong nhóm (status trong thực thể Team) [3].
     */
    public enum TeamStatus {
        ACTIVE,           // Thành viên chính thức của nhóm
        INVITED           // Đã được mời nhưng chưa chấp nhận
    }

    // =========================================================================
    // III. ENUMS VỀ THÍ NGHIỆM VÀ PROTOCOL
    // =========================================================================

    /**
     * Trạng thái của một thí nghiệm (experimentStatus) [3].
     */
    public enum ExperimentStatus {
        DRAFT,            // Bản nháp, chưa bắt đầu thực hiện.
        ONGOING,          // Đang tiến hành [6].
        COMPLETED,        // Đã hoàn thành, sẵn sàng báo cáo [7].
        CANCELED          // Đã hủy bỏ
    }

    /**
     * Trạng thái của một bước thí nghiệm (stepStatus) [3].
     */
    public enum StepStatus {
        PENDING,          // Đang chờ thực hiện
        EXECUTED,         // Đã thực hiện (thành công)
        FAILED,           // Thực hiện thất bại
        SKIPPED           // Bỏ qua bước này
    }

    /**
     * Trạng thái phê duyệt Protocol (approveStatus) [8].
     * Protocol mới phải được Lab Manager duyệt [4, 9].
     */
    public enum ProtocolApproveStatus {
        PENDING,          // Đang chờ Lab Manager phê duyệt
        APPROVED,         // Đã được phê duyệt
        REJECTED          // Bị từ chối (phải có rejectReason [8])
    }

    // =========================================================================
    // IV. ENUMS VỀ KHO VÀ ĐẶT THIẾT BỊ (INVENTORY & BOOKING)
    // =========================================================================

    /**
     * Loại giao dịch kho (transactionType) [5, 8].
     */
    public enum TransactionType {
        CHECK_IN,         // Nhập kho (Thêm số lượng)
        CHECK_OUT         // Xuất kho (Sử dụng số lượng)
    }

    /**
     * Trạng thái giao dịch kho (transactionStatus) [8].
     * Áp dụng cho yêu cầu xuất kho và được quản lý qua màn hình Approve Checkout/Booking [10].
     */
    public enum TransactionStatus {
        PENDING,          // Yêu cầu đang chờ Lab Manager duyệt [11].
        ACCEPTED,         // Yêu cầu đã được chấp nhận
        REJECTED,         // Yêu cầu bị từ chối (phải có rejectReason [8])
        EXECUTED          // Giao dịch đã được thực hiện (xuất/nhập thành công) [11].
    }

    /**
     * Trạng thái đặt thiết bị (bookingStatus) [2].
     * Áp dụng cho yêu cầu mượn thiết bị và được quản lý qua màn hình Approve Checkout/Booking [10].
     */
    public enum BookingStatus {
        PENDING,          // Yêu cầu đang chờ Lab Manager duyệt [11].
        CONFIRMED,        // Đã được chấp nhận/xác nhận
        REJECTED,         // Bị từ chối
        COMPLETED,        // Đã hoàn thành thời gian đặt
        CANCELED          // Đã hủy đặt
    }

    /**
     * Loại bảo trì (maintenanceType) [2].
     */
    public enum MaintenanceType {
        CALIBRATION,      // Hiệu chuẩn
        REPAIR,           // Sửa chữa
        SCHEDULED,        // Bảo trì định kỳ
        OTHER
    }

    // =========================================================================
    // V. ENUMS VỀ GHI NHẬT KÝ VÀ CỘNG TÁC (LOGBOOK & COLLABORATION)
    // =========================================================================

    /**
     * Loại mục nhật ký thí nghiệm (logType) [8].
     * Được ghi trong màn hình Manage Active Experiment Logbook [12, 13].
     */
    public enum LogType {
        NOTE,             // Ghi chú văn bản
        IMAGE_UPLOAD,     // Tải lên hình ảnh (ví dụ: từ camera/gallery) [12].
        DATA_ATTACHMENT,  // Đính kèm tệp dữ liệu (ví dụ: .csv, .xlsx) [14, 15].
        OBSERVATION       // Quan sát chi tiết
    }

    /**
     * Loại bình luận (commentType) [8].
     * Được sử dụng cho tính năng cộng tác nhóm (Team Collaboration) [14, 16].
     */
    public enum CommentType {
        GENERAL,          // Bình luận chung về thí nghiệm
        PEER_REVIEW,      // Bình luận trong quá trình đánh giá ngang hàng (Peer Review) [16, 17].
        DISCUSSION        // Bình luận trong mục thảo luận dự án (Discussion) [16, 17].
    }
}