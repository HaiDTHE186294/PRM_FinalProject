package com.lkms.data.repository.implement.java;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lkms.data.model.java.Item;
import com.lkms.data.model.java.ProtocolItem;
import com.lkms.data.model.java.SDS;
import com.lkms.data.repository.IInventoryRepository;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;

import static com.lkms.BuildConfig.SUPABASE_URL;

public class InventoryRepositoryImplJava implements IInventoryRepository {

    private static final Gson gson = new Gson();

    // Tên bảng Supabase
    private static final String ITEM_TABLE = "Item";
    private static final String SDS_TABLE = "SDS";

    // -------------------- GET ALL INVENTORY ITEMS --------------------
    @Override
    public void getAllInventoryItems(InventoryListCallback callback) {
        new Thread(() -> {
            try {
                String endpoint = SUPABASE_URL + "/rest/v1/" + ITEM_TABLE + "?select=*";
                String json = HttpHelper.getJson(endpoint);

                Type listType = new TypeToken<List<Item>>() {}.getType();
                List<Item> result = gson.fromJson(json, listType);

                if (callback != null) {
                    callback.onSuccess(result);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage() != null ? e.getMessage() : "Failed to load inventory items");
                }
            }
        }).start();
    }

    // -------------------- SEARCH INVENTORY --------------------
    @Override
    public void searchInventory(
            String query,
            InventoryListCallback callback
    ) {
        new Thread(() -> {
            try {
                String endpoint;

                if (query == null || query.trim().isEmpty()) {
                    // Nếu rỗng, lấy tất cả (giống getAllInventoryItems)
                    endpoint = SUPABASE_URL + "/rest/v1/" + ITEM_TABLE + "?select=*";
                } else {
                    // Tìm kiếm dựa trên trường itemName HOẶC casNumber
                    // Dùng OR filter của Supabase: ?or=(itemName.like.*query*,casNumber.like.*query*)
                    String encodedQuery = query.replace(" ", "%20");
                    String filter = "or=(itemName.like.*" + encodedQuery + "*,casNumber.like.*" + encodedQuery + "*)";
                    endpoint = SUPABASE_URL + "/rest/v1/" + ITEM_TABLE + "?select=*&" + filter;
                }

                String json = HttpHelper.getJson(endpoint);
                Type listType = new TypeToken<List<Item>>() {}.getType();
                List<Item> result = gson.fromJson(json, listType);

                if (callback != null) {
                    if (result != null && !result.isEmpty()) {
                        callback.onSuccess(result);
                    } else {
                        callback.onError("No results found for '" + query + "'");
                    }
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage() != null ? e.getMessage() : "Search failed");
                }
            }
        }).start();
    }

    // -------------------- GET SDS URL --------------------
    @Override
    public void getSdsUrl(
            String casNumber,
            StringCallback callback
    ) {
        new Thread(() -> {
            if (callback == null) return;
            try {
                // 1️⃣ Kiểm tra input
                if (casNumber == null || casNumber.trim().isEmpty()) {
                    callback.onError("CAS number is required.");
                    return;
                }

                // 2️⃣ Bỏ qua bước kiểm tra Item, đi thẳng vào truy vấn SDS
                // (Vì SDSId trong Kotlin được map thẳng với casNumber)
                // Query: /SDS?select=*&sdsId=eq.casNumber
                String endpoint = SUPABASE_URL + "/rest/v1/" + SDS_TABLE + "?select=*&sdsId=eq." + casNumber;
                String json = HttpHelper.getJson(endpoint);

                Type listType = new TypeToken<List<SDS>>() {}.getType();
                List<SDS> sdsList = gson.fromJson(json, listType);

                // 3️⃣ Trả kết quả về callback
                if (sdsList != null && !sdsList.isEmpty() && sdsList.get(0).getUrl() != null) {
                    callback.onSuccess(sdsList.get(0).getUrl());
                } else {
                    callback.onError("No SDS found for CAS number: " + casNumber);
                }

            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Error retrieving SDS URL");
            }
        }).start();
    }

    // -------------------- ADD NEW INVENTORY ITEM --------------------
    @Override
    public void addNewInventoryItem(
            Item itemData,
            InventoryItemCallback callback
    ) {
        new Thread(() -> {
            if (callback == null) return;
            try {
                if (itemData == null) {
                    callback.onError("Item data cannot be null.");
                    return;
                }

                String endpoint = SUPABASE_URL + "/rest/v1/" + ITEM_TABLE + "?select=*";
                String jsonBody = gson.toJson(itemData);

                // Supabase POST trả về bản ghi đã tạo
                String response = HttpHelper.postJson(endpoint, jsonBody);

                Type listType = new TypeToken<List<Item>>() {}.getType();
                List<Item> createdItems = gson.fromJson(response, listType);

                if (createdItems != null && !createdItems.isEmpty()) {
                    callback.onSuccess(createdItems.get(0));
                } else {
                    callback.onError("Failed to add new item. Server returned empty response.");
                }

            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Failed to add new item");
            }
        }).start();
    }

    // -------------------- UPDATE INVENTORY ITEM --------------------
    @Override
    public void updateInventoryItem(
            int itemId,
            Item updatedData,
            InventoryItemCallback callback
    ) {
        if (callback == null) return;
        if (updatedData == null) {
            callback.onError("Updated data cannot be null.");
            return;
        }

        new Thread(() -> {
            try {
                // 1️⃣ Cập nhật
                // PUT/PATCH yêu cầu filter theo ID để Supabase biết bản ghi nào cần cập nhật
                String updateEndpoint = SUPABASE_URL + "/rest/v1/" + ITEM_TABLE + "?itemId=eq." + itemId;
                String jsonBody = gson.toJson(updatedData);

                // Supabase PUT/PATCH trả về các bản ghi đã được cập nhật (select=*)
                String updateResponse = HttpHelper.patchJson(updateEndpoint, jsonBody);

                Type listType = new TypeToken<List<Item>>() {}.getType();
                List<Item> updatedItems = gson.fromJson(updateResponse, listType);

                // 2️⃣ Gửi kết quả (bản ghi đã cập nhật)
                if (updatedItems != null && !updatedItems.isEmpty()) {
                    callback.onSuccess(updatedItems.get(0));
                } else {
                    callback.onError("No item found with ID: " + itemId + " or failed to update.");
                }

            } catch (Exception e) {
                callback.onError("Failed to update item: " + e.getMessage());
            }
        }).start();
    }

    // -------------------- UPLOAD SDS FILE TO STORAGE --------------------
    @Override
    public void uploadFileSdsToStorage(
            File file,
            StringCallback callback
    ) {
        if (callback == null) return;
        if (file == null) {
            callback.onError("File is null");
            return;
        }

        new Thread(() -> {
            try {
                String bucketName = "SDS"; // Giả định tên bucket là "SDS"
                String path = System.currentTimeMillis() + "_" + file.getName();

                // Giả định HttpHelper.uploadFile xử lý việc upload và trả về public URL
                String publicUrl = HttpHelper.uploadFile(bucketName, path, file);

                callback.onSuccess(publicUrl);

            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Unknown error while uploading file");
            }
        }).start();
    }

    // -------------------- ADD SDS RECORD --------------------
    @Override
    public void addSds(
            String casNumber,
            String fileUrl,
            IdCallback callback
    ) {
        new Thread(() -> {
            if (callback == null) return;
            try {
                if (casNumber == null || casNumber.trim().isEmpty() || fileUrl == null || fileUrl.trim().isEmpty()) {
                    callback.onError("CAS Number and File URL cannot be null.");
                    return;
                }

                // 1️⃣ (Bỏ qua kiểm tra tồn tại trong Item table - giả định đã được xử lý ở tầng UI/Logic)
                //    Trong Kotlin code, bước này được thực hiện, nhưng trong REST API thuần,
                //    việc này thường được chuyển thành 2 cuộc gọi GET/POST, làm tăng độ phức tạp.
                //    Ta chỉ thực hiện POST:

                // 2️⃣ Tạo bản ghi SDS với sdsId = casNumber (giả định đây là khóa chính)
                SDS newSds = new SDS(casNumber, fileUrl);

                // 🔹 Thêm vào bảng SDS
                String endpoint = SUPABASE_URL + "/rest/v1/" + SDS_TABLE + "?select=*";
                String jsonBody = gson.toJson(newSds);
                String response = HttpHelper.postJson(endpoint, jsonBody);

                Type listType = new TypeToken<List<SDS>>() {}.getType();
                List<SDS> createdList = gson.fromJson(response, listType);

                // 🔹 Kiểm tra kết quả
                if (createdList != null && !createdList.isEmpty()) {
                    // Trả về sdsId (là CAS Number)
                    callback.onSuccess(createdList.get(0).getSdsId());
                } else {
                    callback.onError("Failed to add SDS for CAS: " + casNumber);
                }

            } catch (Exception e) {
                callback.onError("Error while adding SDS: " + e.getMessage());
            }
        }).start();
    }

    // -------------------- NOT IMPLEMENTED YET --------------------
    @Override
    public void logInventoryTransaction(
            int itemId,
            int userId,
            int quantityChange,
            String transactionType,
            TransactionIdCallback callback
    ) {
        if (callback != null) {
            callback.onError("Chưa được triển khai.");
        }
    }

    @Override
    public void processInventoryApproval(
            int transactionId,
            boolean approve,
            String rejectReason,
            GenericCallback callback
    ) {
        if (callback != null) {
            callback.onError("Chưa được triển khai.");
        }
    }
    // ✅ TRIỂN KHAI HÀM MỚI 1: checkAndDeductStock
    @Override
    public void checkAndDeductStock(List<ProtocolItem> itemsToDeduct, GenericCallback callback) {
        new Thread(() -> {
            if (itemsToDeduct == null || itemsToDeduct.isEmpty()) {
                callback.onSuccess(); // Không có gì để trừ, coi như thành công
                return;
            }

            try {
                // Duyệt qua từng vật tư cần dùng
                for (ProtocolItem itemToDeduct : itemsToDeduct) {
                    // 1. Lấy thông tin hiện tại của Item trong kho
                    String getItemUrl = SUPABASE_URL + "/rest/v1/Item?select=*&itemId=eq." + itemToDeduct.getItemId();
                    String itemJson = HttpHelper.getJson(getItemUrl);

                    Type itemType = new TypeToken<List<Item>>() {}.getType();
                    List<Item> currentItems = gson.fromJson(itemJson, itemType);

                    if (currentItems == null || currentItems.isEmpty()) {
                        throw new Exception("Không tìm thấy vật tư với ID: " + itemToDeduct.getItemId());
                    }

                    Item currentItem = currentItems.get(0);
                    int currentQuantity = currentItem.getQuantity();
                    int quantityNeeded = itemToDeduct.getQuantity();

                    // 2. Kiểm tra xem kho có đủ không
                    if (currentQuantity < quantityNeeded) {
                        throw new Exception("Không đủ " + currentItem.getItemName() + ". Cần " + quantityNeeded + ", chỉ còn " + currentQuantity);
                    }

                    // 3. Nếu đủ, tính số lượng mới và thực hiện PATCH để cập nhật
                    int newQuantity = currentQuantity - quantityNeeded;
                    String updateUrl = SUPABASE_URL + "/rest/v1/Item?itemId=eq." + currentItem.getItemId();
                    // Tạo JSON thủ công cho đơn giản: {"quantity": 50}
                    String patchBody = "{\"quantity\": " + newQuantity + "}";
                    // Giả sử bạn có hàm patchJson trong HttpHelper
                    HttpHelper.patchJson(updateUrl, patchBody);
                }

                // Nếu tất cả các vòng lặp thành công
                callback.onSuccess();

            } catch (Exception e) {
                // Nếu có bất kỳ lỗi nào xảy ra (không tìm thấy, không đủ hàng, lỗi mạng)
                // Lưu ý: Hệ thống rollback hoàn hảo sẽ cần hoàn tác lại những gì đã trừ ở các vòng lặp trước.
                // Để đơn giản, phiên bản này chỉ báo lỗi.
                callback.onError("Lỗi khi trừ kho: " + e.getMessage());
            }
        }).start();
    }

    // ✅ TRIỂN KHAI HÀM MỚI 2: restoreStock
    @Override
    public void restoreStock(List<ProtocolItem> itemsToRestore, GenericCallback callback) {
        new Thread(() -> {
            if (itemsToRestore == null || itemsToRestore.isEmpty()) {
                callback.onSuccess(); // Không có gì để hoàn tác
                return;
            }
            // Logic này tương tự như `deductStock` nhưng thay vì trừ, chúng ta sẽ cộng
            try {
                for (ProtocolItem itemToRestore : itemsToRestore) {
                    String getItemUrl = SUPABASE_URL + "/rest/v1/Item?select=*&itemId=eq." + itemToRestore.getItemId();
                    String itemJson = HttpHelper.getJson(getItemUrl);
                    Type itemType = new TypeToken<List<Item>>() {}.getType();
                    List<Item> currentItems = gson.fromJson(itemJson, itemType);

                    if (currentItems == null || currentItems.isEmpty()) {
                        // Bỏ qua nếu không tìm thấy item, vì có thể nó đã bị xóa
                        continue;
                    }

                    Item currentItem = currentItems.get(0);
                    int newQuantity = currentItem.getQuantity() + itemToRestore.getQuantity();

                    String updateUrl = SUPABASE_URL + "/rest/v1/Item?itemId=eq." + currentItem.getItemId();
                    String patchBody = "{\"quantity\": " + newQuantity + "}";
                    HttpHelper.patchJson(updateUrl, patchBody);
                }
                callback.onSuccess();
            } catch (Exception e) {
                callback.onError("Lỗi khi hoàn trả vật tư: " + e.getMessage());
            }
        }).start();
    }
}