package com.lkms.data.repository.implement.java;

import static com.lkms.BuildConfig.SUPABASE_URL;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;import com.lkms.data.model.java.Protocol;
import com.lkms.data.model.java.ProtocolItem;
import com.lkms.data.model.java.ProtocolStep;
import com.lkms.data.repository.IProtocolRepository;
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums.ProtocolApproveStatus;
import java.lang.reflect.Type;
import java.net.URLEncoder; // <-- ĐÃ THÊM IMPORT
import java.nio.charset.StandardCharsets; // <-- ĐÃ THÊM IMPORT
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Triển khai bằng Java của IProtocolRepository.
 */
public class ProtocolRepositoryImplJava implements IProtocolRepository {

    private static final Gson gson = new Gson();

    // -------------------- LẤY DANH SÁCH TẤT CẢ PROTOCOL --------------------
    @Override
    public void getAllProtocols(ProtocolListCallback callback) {
        new Thread(() -> {
            try {
                String endpoint = SUPABASE_URL + "/rest/v1/Protocol?select=*";
                String json = HttpHelper.getJson(endpoint);

                Type listType = new TypeToken<List<Protocol>>() {}.getType();
                List<Protocol> protocols = gson.fromJson(json, listType);

                callback.onSuccess(protocols);
            } catch (Exception e) {
                callback.onError("Lỗi khi tải danh sách protocol: " + e.getMessage());
            }
        }).start();
    }

    // -------------------- LẤY THƯ VIỆN CHÍNH (ĐÃ DUYỆT & MỚI NHẤT) --------------------
    @Override
    public void getLatestApprovedProtocols(ProtocolListCallback callback) {
        new Thread(() -> {
            try {
                // Xây dựng URL với hai điều kiện lọc:
                // 1. approveStatus phải bằng 'Approved'
                // 2. isLatestVersion phải bằng 'true' (Giả định cột này tồn tại trong DB)
                String endpoint = SUPABASE_URL + "/rest/v1/Protocol?select=*&approveStatus=eq.Approved";

                String json = HttpHelper.getJson(endpoint);
                Type listType = new TypeToken<List<Protocol>>() {}.getType();
                List<Protocol> protocols = gson.fromJson(json, listType);
                callback.onSuccess(protocols);
            } catch (Exception e) {
                callback.onError("Lỗi khi tải thư viện protocol: " + e.getMessage());
            }
        }).start();
    }

    // -------------------- TÌM KIẾM PROTOCOL THEO TIÊU ĐỀ --------------------
    @Override
    public void searchProtocolsByTitle(String titleQuery, ProtocolListCallback callback) {
        new Thread(() -> {
            try {
                // Toán tử 'ilike' dùng để tìm kiếm không phân biệt chữ hoa/thường.
                // Ký tự '%' là ký tự đại diện.
                String searchQuery = "%" + titleQuery + "%";

                // QUAN TRỌNG: Phải mã hóa (encode) giá trị tìm kiếm để đảm bảo URL hợp lệ.
                String encodedSearchQuery = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8.toString());

                String endpoint = SUPABASE_URL + "/rest/v1/Protocol?select=*&protocolTitle=ilike." + encodedSearchQuery;

                String json = HttpHelper.getJson(endpoint);
                Type listType = new TypeToken<List<Protocol>>() {}.getType();
                List<Protocol> protocols = gson.fromJson(json, listType);
                callback.onSuccess(protocols);
            } catch (Exception e) {
                callback.onError("Lỗi khi tìm kiếm protocol: " + e.getMessage());
            }
        }).start();
    }

    // -------------------- LỌC PROTOCOL THEO NHIỀU ĐIỀU KIỆN --------------------
    @Override
    public void filterProtocols(Integer creatorId, String versionNumber, ProtocolListCallback callback) {
        new Thread(() -> {
            try {
                // Xây dựng chuỗi tham số một cách "động"
                StringBuilder params = new StringBuilder("select=*");

                // Chỉ thêm điều kiện lọc NẾU giá trị được cung cấp không phải là null
                if (creatorId != null) {
                    params.append("&creatorUserId=eq.").append(creatorId);
                }
                if (versionNumber != null && !versionNumber.trim().isEmpty()) {
                    // Mã hóa versionNumber để phòng trường hợp nó chứa ký tự đặc biệt
                    String encodedVersion = URLEncoder.encode(versionNumber, StandardCharsets.UTF_8.toString());
                    params.append("&versionNumber=eq.").append(encodedVersion);
                }

                String endpoint = SUPABASE_URL + "/rest/v1/Protocol?" + params.toString();

                String json = HttpHelper.getJson(endpoint);
                Type listType = new TypeToken<List<Protocol>>() {}.getType();
                List<Protocol> protocols = gson.fromJson(json, listType);
                callback.onSuccess(protocols);
            } catch (Exception e) {
                callback.onError("Lỗi khi lọc protocol: " + e.getMessage());
            }
        }).start();
    }

    // -------------------- LẤY CHI TIẾT MỘT PROTOCOL --------------------
    @Override
    public void getProtocolDetails(int protocolId, ProtocolContentCallback callback) {
        new Thread(() -> {
            try {
                // 1️⃣ Lấy thông tin chính của Protocol
                String protocolUrl = SUPABASE_URL + "/rest/v1/Protocol?select=*&protocolId=eq." + protocolId;
                String protocolJson = HttpHelper.getJson(protocolUrl);

                Type protocolType = new TypeToken<List<Protocol>>() {}.getType();
                List<Protocol> protocolList = gson.fromJson(protocolJson, protocolType);
                if (protocolList.isEmpty()) {
                    callback.onError("Không tìm thấy protocol ID=" + protocolId);
                    return;
                }
                callback.onProtocolReceived(protocolList.get(0));

                // 2️⃣ Lấy danh sách các bước (ProtocolStep)
                String stepUrl = SUPABASE_URL + "/rest/v1/ProtocolStep?select=*&protocolId=eq." + protocolId;
                String stepJson = HttpHelper.getJson(stepUrl);

                Type stepType = new TypeToken<List<ProtocolStep>>() {}.getType();
                List<ProtocolStep> steps = gson.fromJson(stepJson, stepType);
                callback.onStepsReceived(steps);

                // 3️⃣ Lấy danh sách vật tư (ProtocolItem)
                String itemUrl = SUPABASE_URL + "/rest/v1/ProtocolItem?select=*&protocolId=eq." + protocolId;
                String itemJson = HttpHelper.getJson(itemUrl);

                Type itemType = new TypeToken<List<ProtocolItem>>() {}.getType();
                List<ProtocolItem> items = gson.fromJson(itemJson, itemType);
                callback.onItemsReceived(items);

            } catch (Exception e) {
                callback.onError("Lỗi khi lấy chi tiết protocol: " + e.getMessage());
            }
        }).start();
    }

    // -------------------- TẠO MỚI PROTOCOL --------------------
    @Override
    public void createNewProtocol(
            Protocol protocolData,
            List<ProtocolStep> steps,
            List<ProtocolItem> items,
            int creatorUserId,
            ProtocolIdCallback callback
    ) {
        new Thread(() -> {
            try {
                // Gán thông tin người tạo và trạng thái ban đầu
                protocolData.setCreatorUserId(creatorUserId);
                protocolData.setApproveStatus(ProtocolApproveStatus.PENDING);

                // Bước 1️⃣: Gửi POST tạo Protocol
                String protocolUrl = SUPABASE_URL + "/rest/v1/Protocol?select=*";
                String jsonBody = gson.toJson(protocolData);
                String response = HttpHelper.postJson(protocolUrl, jsonBody);

                Type listType = new TypeToken<List<Protocol>>() {}.getType();
                List<Protocol> createdProtocols = gson.fromJson(response, listType);
                if (createdProtocols == null || createdProtocols.isEmpty()) {
                    callback.onError("Không thể tạo protocol mới.");
                    return;
                }

                Protocol insertedProtocol = createdProtocols.get(0);
                int newProtocolId = insertedProtocol.getProtocolId();
                if (newProtocolId == 0) {
                    callback.onError("Không thể lấy ID protocol vừa tạo.");
                    return;
                }

                // Bước 2️⃣: Gán protocolId cho từng Step
                if (steps != null && !steps.isEmpty()) {
                    for (ProtocolStep s : steps) s.setProtocolId(newProtocolId);
                    String stepUrl = SUPABASE_URL + "/rest/v1/ProtocolStep?select=*";
                    HttpHelper.postJson(stepUrl, gson.toJson(steps));
                }

                // Bước 3️⃣: Gán protocolId cho từng Item
                if (items != null && !items.isEmpty()) {
                    for (ProtocolItem i : items) i.setProtocolId(newProtocolId);
                    String itemUrl = SUPABASE_URL + "/rest/v1/ProtocolItem?select=*";
                    HttpHelper.postJson(itemUrl, gson.toJson(items));
                }

                // ✅ Thành công
                callback.onSuccess(newProtocolId);

            } catch (Exception e) {
                callback.onError("Lỗi khi tạo protocol: " + e.getMessage());
            }
        }).start();
    }

    // -------------------- DUYỆT HOẶC TỪ CHỐI PROTOCOL --------------------
    @Override
    public void approveProtocol(int protocolId, int approverUserId, ProtocolApproveStatus newStatus, String reason, GenericCallback callback) {
        new Thread(() -> {
            try {
                // Dữ liệu update
                // Tạo một Map để chứa các trường cần cập nhật.
                // Cách này linh hoạt hơn việc tạo một class phụ.
                Map<String, Object> updateData = new HashMap<>();

                // Gson sẽ tự động chuyển Enum thành chuỗi (APPROVED hoặc REJECTED)
                updateData.put("approveStatus", newStatus);
                updateData.put("approverUserId", approverUserId);

                // Chỉ thêm lý do từ chối nếu trạng thái là REJECTED và lý do có nội dung
                if (newStatus == ProtocolApproveStatus.REJECTED && reason != null && !reason.isEmpty()) {
                    updateData.put("rejectionReason", reason); // Giả sử cột trong DB là 'rejectionReason'
                }

                // Chuyển Map thành chuỗi JSON
                String jsonBody = gson.toJson(updateData);

                // Gửi yêu cầu PATCH
                String endpoint = SUPABASE_URL + "/rest/v1/Protocol?protocolId=eq." + protocolId;
                HttpHelper.patchJson(endpoint, jsonBody);

                callback.onSuccess();

            } catch (Exception e) {
                callback.onError("Lỗi khi cập nhật trạng thái phê duyệt: " + e.getMessage());
            }
        }).start();
    }

   //  -------------------- Lấy protocol step --------------------
    @Override
    public void getProtocolStep(int protocolStepId, ProtocolStepCallback callback) {
        new Thread(() -> {
            try {
                String tableName = "ProtocolStep";
                String idColumn = "protocolStepId";


                String endpoint = SUPABASE_URL + "/rest/v1/" + tableName +
                        "?select=*&" + idColumn + "=eq." + protocolStepId;

                String json = HttpHelper.getJson(endpoint);

                // Dù chỉ mong đợi 1 object, Supabase API trả về một mảng (List)
                Type listType = new TypeToken<List<ProtocolStep>>() {}.getType();
                List<ProtocolStep> steps = gson.fromJson(json, listType);

                // Kiểm tra xem mảng có dữ liệu không
                if (steps != null && !steps.isEmpty()) {
                    // Lấy phần tử đầu tiên (và duy nhất)
                    ProtocolStep protocolStep = steps.get(0);
                    callback.onSuccess(protocolStep);
                } else {
                    // Không tìm thấy
                    callback.onError("Không tìm thấy protocol step với id: " + protocolStepId);
                }

            } catch (Exception e) {
                callback.onError("Lỗi khi tải protocol step: " + e.getMessage());
            }
        }).start();
    }

    // -------------------- CLASS PHỤ TRỢ --------------------
    private static class ProtocolApprovalUpdate {
        String approveStatus;
        int approverUserId;
        String rejectionReason;

        ProtocolApprovalUpdate(String approveStatus, int approverUserId, String rejectionReason) {
            this.approveStatus = approveStatus;
            this.approverUserId = approverUserId;
            this.rejectionReason = rejectionReason;
        }
    }
}
