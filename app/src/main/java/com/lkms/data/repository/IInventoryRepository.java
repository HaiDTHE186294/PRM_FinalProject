package com.lkms.data.repository;

import com.lkms.data.model.java.*;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Giao diện Repository cho việc Quản lý Tồn kho (Inventory Management).
 * Hỗ trợ các chức năng UC7, UC8, UC11, và một phần của UC35.
 */
public interface IInventoryRepository {

    // --- Các giao diện Callback ---

    interface IdCallback {
        void onSuccess(String id); // Trả về id mới
        void onError(String errorMessage);
    }

    interface InventoryListCallback {
        void onSuccess(List<Item> items);
        void onError(String errorMessage);
    }

    interface InventoryItemCallback {
        void onSuccess(Item item);
        void onError(String errorMessage);
    }

    interface TransactionIdCallback {
        void onSuccess(int transactionId); // Trả về ID giao dịch mới
        void onError(String errorMessage);
    }

    interface StringCallback {
        void onSuccess(String result); // Dùng cho URL SDS
        void onError(String errorMessage);
    }

    interface GenericCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public interface InventoryDisplayListCallback {
        void onSuccess(List<InventoryDisplayItem> displayItems);
        void onError(String errorMessage);
    }

    // --- Chức năng Tra cứu và Hiển thị Tồn kho (UC7, UC11) ---

    /**
     * UC7: Lấy danh sách tất cả mặt hàng tồn kho (chemicals, reagents, consumables).
     * Truy vấn bảng "Item" [1, 2].
     */
    void getAllInventoryItems(InventoryListCallback callback);

    /**
     * UC7: Tìm kiếm mặt hàng tồn kho theo tên hoặc CAS number.
     * Truy vấn bảng "Item" [1].
     */
    void searchInventory(String query, InventoryListCallback callback);

    /**
     * UC11: Tra cứu thông tin SDS (Safety Data Sheet).
     * Truy vấn bảng "SDS" [3] dựa trên CAS Number.
     */
    void getSdsUrl(String casNumber, StringCallback callback);

    // --- Chức năng Thêm / Cập nhật và Giao dịch (UC8) ---

    /**
     * UC8: Thêm một mặt hàng mới vào kho.
     * Chèn dữ liệu vào bảng "Item" [1].
     */
    void addNewInventoryItem(Item itemData, InventoryItemCallback callback);

    /**
     * UC8: Ghi lại một giao dịch tồn kho (Check In/Check Out).
     * Chèn dữ liệu vào bảng "InventoryTransaction" [2].
     * Việc cập nhật số lượng trong bảng "Item" sẽ được xử lý sau khi Admin duyệt (UC35).
     *
     * @param transactionType "Check In" hoặc "Check Out".
     * @param quantityChange Số lượng mặt hàng giao dịch.
     * @return transactionId để theo dõi yêu cầu.
     */
    void logInventoryTransaction(
            int itemId,
            int userId,
            int quantityChange,
            String transactionType,
            TransactionIdCallback callback
    );

    // --- Chức năng Phê duyệt Tồn kho (Approval - Dành cho Lab Manager) ---

    /**
     * UC35: Xử lý phê duyệt giao dịch tồn kho (yêu cầu Check Out/sử dụng hóa chất).
     * Cập nhật trường "transactionStatus" và "rejectReason" trong bảng "InventoryTransaction" [2].
     */
    void processInventoryApproval(int transactionId, boolean approve, String rejectReason, GenericCallback callback);

    /**
     * @param itemId ID của mặt hàng cần cập nhật.
     * @param updatedData Đối tượng Item chứa thông tin mới.
     * @param callback Gọi lại khi hoàn tất (trả về Item sau khi cập nhật hoặc báo lỗi).
     */
    void updateInventoryItem(int itemId, Item updatedData, InventoryItemCallback callback);


    void uploadFileSdsToStorage(File file, StringCallback callback);


    void addSds(String casNumber, String fileUrl, IdCallback callback);

    // ✅ HÀM MỚI 1: Hàm chính để trừ kho
    /**
     * Kiểm tra và trừ kho cho một danh sách vật tư bằng cách cập nhật trực tiếp bảng Item.
     * Đây là một "giao dịch": hoặc thành công tất cả, hoặc thất bại hoàn toàn.
     * @param itemsToDeduct Danh sách các ProtocolItem cần trừ.
     * @param callback Báo cáo thành công (onSuccess) hoặc thất bại (onError) với thông báo lỗi cụ thể.
     */
    void checkAndDeductStock(List<ProtocolItem> itemsToDeduct, GenericCallback callback);

    // ✅ HÀM MỚI 2: Hàm để hoàn tác (rollback)
    /**
     * Hoàn trả lại vật tư vào kho trong trường hợp có lỗi xảy ra ở bước sau.
     * @param itemsToRestore Danh sách vật phẩm cần hoàn trả.
     * @param callback
     */
    void restoreStock(List<ProtocolItem> itemsToRestore, GenericCallback callback);
}