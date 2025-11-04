package com.lkms.data.repository.implement.java;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lkms.data.model.java.InventoryTransaction;
import com.lkms.data.model.java.Item;
import com.lkms.data.model.java.ProtocolItem;
import com.lkms.data.model.java.SDS;
import com.lkms.data.repository.IInventoryRepository;

import java.io.File;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.lkms.BuildConfig.SUPABASE_URL;

public class InventoryRepositoryImplJava implements IInventoryRepository {

    private static final Gson gson = new Gson();

    // Tên bảng Supabase
    private static final String ITEM_TABLE = "Item";
    private static final String SDS_TABLE = "SDS";
    private static final String INVT_TRANS_TABLE = "InventoryTransaction";

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
                    String filter = "or=(itemName.ilike.*" + encodedQuery + "*,casNumber.ilike.*" + encodedQuery + "*)";
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

    @Override
    public void logInventoryTransaction(
            int itemId,
            int userId,
            int quantityChange,
            String transactionType,
            TransactionIdCallback callback
    ) {
        new Thread(() -> {
            if (callback == null) {
                // Log an error or handle silently if no callback is provided
                return;
            }
            String jsonBody = "";
            try {
                String endpoint = SUPABASE_URL + "/rest/v1/" + INVT_TRANS_TABLE + "?select=*";

                // Build request body
                Map<String, Object> request = new HashMap<>();
                request.put("transactionType", transactionType);
                request.put("itemId", itemId);
                request.put("userId", userId);
                request.put("quantity", quantityChange);
                request.put("transactionStatus", "ACCEPT");

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String formattedDate = sdf.format(new Date());
                request.put("transactionTime", formattedDate);

                jsonBody = gson.toJson(request);
                String response = HttpHelper.postJson(endpoint, jsonBody);

                // The response will be an array with the newly created object, e.g., [{"transactionId": 123}]
                Type listType = new TypeToken<List<InventoryTransaction>>() {}.getType();
                List<InventoryTransaction> createdTransactions = gson.fromJson(response, listType);

                if (createdTransactions != null && !createdTransactions.isEmpty()) {
                    callback.onSuccess(createdTransactions.get(0).getTransactionId());
                } else {
                    callback.onError(jsonBody + "\n" + response);
                }
            } catch (Exception e) {
                callback.onError("Failed to log transaction: " + (e.getMessage() != null ? e : "Unknown error") + "\n Body: " + jsonBody);
            }
        }).start();
    }

    @Override
    public void getInventoryTransaction(int itemId, InventoryTransactionListCallback callback) {
        new Thread(() -> {
            if (callback == null) {
                return; // Silently exit if no callback is provided
            }
            try {
                // Construct the endpoint to get all transactions for a specific itemId
                String endpoint = SUPABASE_URL + "/rest/v1/" + INVT_TRANS_TABLE + "?select=*&itemId=eq." + itemId;

                // Make the GET request
                String jsonResponse = HttpHelper.getJson(endpoint);

                // Define the type for a list of InventoryTransaction objects
                Type listType = new TypeToken<List<InventoryTransaction>>() {}.getType();
                List<InventoryTransaction> transactions = gson.fromJson(jsonResponse, listType);

                // Check the result and call the appropriate callback method
                if (transactions != null) {
                    callback.onSuccess(transactions); // Return the list, even if it's empty
                } else {
                    // This case is unlikely with Gson but included for safety
                    callback.onError("Failed to retrieve transactions. Server returned a null response.");
                }
            } catch (Exception e) {
                // Handle any exceptions during the network call or parsing
                String errorMessage = "Error fetching inventory transactions: " + (e.getMessage() != null ? e.getMessage() : "Unknown error");
                callback.onError(errorMessage);
            }
        }).start();
    }

    // -------------------- NOT IMPLEMENTED YET --------------------
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

    @Override
    public void getItemById(int itemId, InventoryItemCallback callback) {
        new Thread(() -> {
            try {
                String endpoint = SUPABASE_URL + "/rest/v1/" + ITEM_TABLE + "?select=*&itemId=eq." + itemId;
                String json = HttpHelper.getJson(endpoint);

                Type itemType = new TypeToken<List<Item>>() {}.getType();
                List<Item> result = gson.fromJson(json, itemType);

                if (result != null && !result.isEmpty()) {
                    callback.onSuccess(result.get(0));
                } else {
                    callback.onError("Item not found with ID: " + itemId);
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Error retrieving item details");
            }
        }).start();
    }

    public void deductStock(List<ProtocolItem> itemsToDeduct, GenericCallback callback) {
        final String TAG = "DeductStock_OldMethod";
        new Thread(() -> {
            try {
                if (itemsToDeduct == null || itemsToDeduct.isEmpty()) {
                    callback.onSuccess();
                    return;
                }
                android.util.Log.i(TAG, "Bắt đầu trừ kho theo phương pháp cũ (GET -> PATCH) cho " + itemsToDeduct.size() + " loại vật tư.");

                for (ProtocolItem itemToDeduct : itemsToDeduct) {
                    // 1. Lấy thông tin hiện tại của Item trong kho
                    String getItemUrl = SUPABASE_URL + "/rest/v1/Item?select=*&itemId=eq." + itemToDeduct.getItemId();
                    String itemJson = HttpHelper.getJson(getItemUrl);
                    Type itemType = new TypeToken<List<Item>>() {}.getType();
                    List<Item> currentItems = gson.fromJson(itemJson, itemType);

                    if (currentItems == null || currentItems.isEmpty()) {
                        android.util.Log.w(TAG, "Không tìm thấy vật tư ID " + itemToDeduct.getItemId() + " khi đang trừ kho.");
                        continue;
                    }

                    Item currentItem = currentItems.get(0);
                    int currentQuantity = currentItem.getQuantity();
                    int quantityNeeded = itemToDeduct.getQuantity();

                    // 2. Kiểm tra lại lần cuối
                    if (currentQuantity < quantityNeeded) {
                        throw new InsufficientStockException("Không đủ " + currentItem.getItemName() + ". Cần " + quantityNeeded + ", chỉ còn " + currentQuantity);
                    }

                    // 3. Nếu đủ, tính số lượng mới và thực hiện PATCH để cập nhật
                    int newQuantity = currentQuantity - quantityNeeded;
                    String updateUrl = SUPABASE_URL + "/rest/v1/Item?itemId=eq." + currentItem.getItemId();
                    String patchBody = "{\"quantity\": " + newQuantity + "}";
                    android.util.Log.d(TAG, "Thực hiện PATCH cho Item ID " + currentItem.getItemId() + " với body: " + patchBody);
                    HttpHelper.patchJson(updateUrl, patchBody);
                }

                android.util.Log.i(TAG, "Trừ kho theo phương pháp cũ thành công.");
                callback.onSuccess();
            } catch (InsufficientStockException stockEx) { // Bắt Exception cụ thể trước
                android.util.Log.e(TAG, "LỖI không đủ hàng trong kho: " + stockEx.getMessage());
                callback.onError(stockEx.getMessage());
            } catch (Exception e) { // Bắt các Exception chung chung khác sau
                android.util.Log.e(TAG, "LỖI không xác định khi trừ kho: " + e.getMessage(), e);
                callback.onError("Lỗi không xác định khi trừ kho: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Lớp Exception nội bộ, mô tả lỗi không đủ hàng trong kho.
     */
    private static class InsufficientStockException extends Exception {
        public InsufficientStockException(String message) {
            super(message);
        }
    }

    @Override
    public void checkStockAvailability(List<ProtocolItem> itemsToCheck, GenericCallback callback) {
        // Chạy trên một thread mới để không làm treo giao diện
        new Thread(() -> {
            try {
                if (itemsToCheck == null || itemsToCheck.isEmpty()) {
                    callback.onSuccess(); // Không yêu cầu vật tư nào, coi như đủ.
                    return;
                }

                // Lặp qua TẤT CẢ các vật tư để kiểm tra trước khi hành động
                for (ProtocolItem requiredItem : itemsToCheck) {
                    // Lấy thông tin số lượng hiện tại của vật tư từ Supabase
                    String endpoint = SUPABASE_URL + "/rest/v1/Item?itemId=eq." + requiredItem.getItemId() + "&select=itemName,quantity";
                    String jsonResponse = HttpHelper.getJson(endpoint);

                    Type listType = new TypeToken<List<Item>>(){}.getType();
                    List<Item> currentItems = gson.fromJson(jsonResponse, listType);

                    if (currentItems == null || currentItems.isEmpty()) {
                        callback.onError("Không tìm thấy vật tư có ID: " + requiredItem.getItemId() + " trong kho.");
                        return; // Dừng lại ngay lập tức
                    }

                    Item currentItemInStock = currentItems.get(0);
                    if (currentItemInStock.getQuantity() < requiredItem.getQuantity()) {
                        // Nếu có bất kỳ vật tư nào không đủ, báo lỗi và dừng lại ngay
                        callback.onError(
                                "Không đủ '" + currentItemInStock.getItemName() + "'. Cần " +
                                        requiredItem.getQuantity() + ", chỉ còn " + currentItemInStock.getQuantity() + "."
                        );
                        return; // Dừng lại ngay lập tức
                    }
                }

                // Nếu vòng lặp hoàn tất mà không có lỗi, nghĩa là tất cả vật tư đều đủ
                callback.onSuccess();

            } catch (Exception e) {
                callback.onError("Lỗi mạng khi kiểm tra kho: " + e.getMessage());
            }
        }).start();
    }
}