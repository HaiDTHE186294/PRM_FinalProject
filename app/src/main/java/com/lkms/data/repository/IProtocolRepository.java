// File: IProtocolRepository.java
package com.lkms.data.repository;

// Sử dụng trực tiếp các model ánh xạ DB
import com.lkms.data.model.java.*;
import java.util.List;
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums.ProtocolApproveStatus;

/**
 * Giao diện Repository cho việc Quản lý Protocols và SOPs (UC3, UC4).
 * Chỉ sử dụng các Model ánh xạ trực tiếp từ DB.
 */
public interface IProtocolRepository {

    // --- Các giao diện Callback ---

    interface ProtocolListCallback {
        void onSuccess(List<Protocol> protocols); // Trả về List<Protocol>
        void onError(String errorMessage);
    }

    // Callback đặc biệt để trả về nội dung chi tiết
    interface ProtocolContentCallback {
        void onProtocolReceived(Protocol protocol); // Dữ liệu chung từ bảng Protocol [1]
        void onStepsReceived(List<ProtocolStep> steps); // Danh sách các bước từ bảng ProtocolStep [2]
        void onItemsReceived(List<ProtocolItem> items); // Danh sách vật tư từ bảng ProtocolItem [2]
        void onError(String errorMessage);
    }

    interface ProtocolIdCallback {
        void onSuccess(int protocolId);
        void onError(String errorMessage);
    }

    interface GenericCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    interface ProtocolStepCallback {
        void onSuccess(ProtocolStep step);
        void onError(String errorMessage);
    }

    interface ProtocolCallBack{
        void onSuccess(Protocol protocol);
        void onError(String errorMessage);
    }


    // --- Chức năng Tra cứu và Hiển thị Protocols (UC3) ---

    /**
     * UC3: Lấy danh sách TẤT CẢ protocols (bao gồm cả bản nháp, bị từ chối...).
     * Hữu ích cho các màn hình quản lý của Admin hoặc Lab Manager.
     * Truy vấn các trường từ bảng "Protocol" [1, 2].
     */
    void getAllProtocols(ProtocolListCallback callback);

    // --- ▼▼▼ CÁC HÀM TÌM KIẾM VÀ LỌC MỚI ĐƯỢC THÊM VÀO ĐÂY ▼▼▼ ---

    /**
     * UC3: Lấy danh sách các protocol đã được duyệt (approved) và là phiên bản mới nhất.
     * Đây là "thư viện chính" cho người dùng cuối (Lab Staff).
     * @param callback Callback để trả về kết quả.
     */
    void getLatestApprovedProtocols(ProtocolListCallback callback);

    /**
     * UC3: Tìm kiếm protocol dựa trên tiêu đề (protocolTitle).
     * Sử dụng tìm kiếm không phân biệt chữ hoa/thường.
     * @param titleQuery Từ khóa tìm kiếm.
     * @param callback Callback để trả về kết quả.
     */
    void searchProtocolsByTitle(String titleQuery, ProtocolListCallback callback);

    /**
     * UC3: Lọc protocol một cách linh hoạt dựa trên nhiều điều kiện.
     * Bất kỳ tham số nào là null sẽ được bỏ qua trong bộ lọc.
     * @param creatorId ID của người tạo (hoặc null để bỏ qua).
     * @param versionNumber Số phiên bản (hoặc null để bỏ qua).
     * @param callback Callback để trả về kết quả.
     */
    void filterProtocols(Integer creatorId, String versionNumber, ProtocolListCallback callback);

    // --- Chức năng Xem chi tiết Protocol (UC4) ---

    /**
     * UC4: Lấy nội dung chi tiết của một protocol.
     * Hàm này phải thực hiện 3 truy vấn riêng biệt và trả về 3 loại Model khác nhau.
     * ViewModel sẽ chịu trách nhiệm chờ tất cả các kết quả này.
     */
    void getProtocolDetails(int protocolId, ProtocolContentCallback callback);

    // --- Chức năng Tạo Protocol (UC3) ---

    /**
     * UC3: Tạo một Protocol mới.
     * Dữ liệu đầu vào: Model Protocol (chứa protocolTitle, safetyWarning, approveStatus, creatorUserId, etc.) [1, 2]
     * và danh sách các bước [2].
     * @param creatorUserId (int) Dựa trên trường "creatorUserId" [2].
     */
    void createNewProtocol(Protocol protocolData, List<ProtocolStep> steps, List<ProtocolItem> items, int creatorUserId, ProtocolIdCallback callback);

    // --- Chức năng Phê duyệt Protocol (UC20, UC21 - Chỉ Lab Manager) ---

    /**
     * UC20: Duyệt hoặc từ chối một protocol mới.
     * Cập nhật trường "approveStatus" và "approverUserId" [2] trong bảng "Protocol".
     */
    void approveProtocol(int protocolId, int approverUserId, ProtocolApproveStatus newStatus, String reason, GenericCallback callback);
  
    // --- Chức năng lấy thông tin của protocolStep
    void getProtocolStep(int protocolStepId, ProtocolStepCallback callback);

    void getProtocolById(int protocolId, ProtocolCallBack callback);
}