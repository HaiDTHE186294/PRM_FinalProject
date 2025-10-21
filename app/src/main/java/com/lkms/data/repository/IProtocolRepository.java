package com.lkms.data.repository;

// Sử dụng trực tiếp các model ánh xạ DB
import com.lkms.data.model.java.*;
import java.util.List;

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

    // --- Chức năng Tra cứu và Hiển thị Protocols (UC3) ---

    /**
     * UC3: Lấy danh sách protocols.
     * Truy vấn các trường từ bảng "Protocol" [1, 2].
     */
    void getAllProtocols(ProtocolListCallback callback);

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
    void approveProtocol(int protocolId, int approverUserId, boolean approved, String reason, GenericCallback callback);
}