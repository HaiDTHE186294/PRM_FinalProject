package com.lkms.data.repository.implement.java;

// ⭐ SỬA LỖI: Import SUPABASE_URL từ BuildConfig giống các repo khác
import static com.lkms.BuildConfig.SUPABASE_URL;

import android.util.Log; // ⭐ SỬA LỖI: Thêm Log để debug

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lkms.data.model.java.Team;
import com.lkms.data.repository.ITeamRepository;

import java.lang.reflect.Type;
import java.util.List;

public class TeamRepositoryImplJava implements ITeamRepository {

    // ⭐ SỬA LỖI: Dùng hằng số static để tránh khởi tạo lại nhiều lần
    private static final String TEAM_TABLE = "Team";
    private static final Gson gson = new Gson();

    @Override
    public void addMember(Team teamMember, TeamMemberCallback callback) {
        new Thread(() -> {
            if (callback == null) return;
            try {
                if (teamMember == null || teamMember.getExperimentId() == null || teamMember.getUserId() == null) {
                    callback.onError("Dữ liệu thành viên không hợp lệ (null).");
                    return;
                }

                // ⭐ SỬA LỖI: Dùng hằng số SUPABASE_URL đã import
                String endpoint = SUPABASE_URL + "/rest/v1/" + TEAM_TABLE + "?select=*";
                String jsonBody = gson.toJson(teamMember);

                // ⭐ SỬA LỖI: Thêm log để biết chính xác dữ liệu gửi đi là gì
                Log.d("TeamRepo", "Đang gửi yêu cầu POST đến: " + endpoint);
                Log.d("TeamRepo", "Với nội dung (body): " + jsonBody);

                // ⭐ SỬA LỖI: Gọi phương thức static của HttpHelper giống các repo khác
                String responseJson = HttpHelper.postJson(endpoint, jsonBody);

                Log.d("TeamRepo", "Phản hồi từ server: " + responseJson);

                Type listType = new TypeToken<List<Team>>() {}.getType();
                List<Team> createdMembers = gson.fromJson(responseJson, listType);

                if (createdMembers != null && !createdMembers.isEmpty()) {
                    callback.onSuccess(createdMembers.get(0));
                } else {
                    // Nếu server trả về mảng rỗng hoặc null, đó cũng là một lỗi
                    callback.onError("Thêm thành viên thất bại. Server không trả về bản ghi đã tạo.");
                }

            } catch (Exception e) {
                // ⭐ SỬA LỖI: Ghi log lỗi chi tiết ra Logcat để debug
                Log.e("TeamRepo", "Lỗi nghiêm trọng khi thêm thành viên: ", e);
                callback.onError("Lỗi khi thêm thành viên: " + e.getMessage());
            }
        }).start();
    }

    // ⭐ --- PHẦN BỔ SUNG BẮT ĐẦU TỪ ĐÂY --- ⭐
    /**
     * Triển khai phương thức thêm một danh sách thành viên.
     */
    @Override
    public void addMembers(List<Team> teamMembers, BulkTeamMemberCallback callback) {
        new Thread(() -> {
            try {
                // Endpoint để POST vào bảng Team.
                // Không cần "?select=*" vì khi POST hàng loạt, chúng ta không cần kết quả trả về để tiết kiệm băng thông.
                String endpoint = SUPABASE_URL + "/rest/v1/" + TEAM_TABLE;

                // Chuyển đổi cả danh sách đối tượng Team thành một chuỗi JSON.
                String jsonBody = gson.toJson(teamMembers);

                Log.d("TeamRepo", "Đang gửi yêu cầu POST hàng loạt đến: " + endpoint);
                Log.d("TeamRepo", "Với nội dung (body): " + jsonBody);

                // Gọi HttpHelper để thực hiện yêu cầu POST.
                HttpHelper.postJson(endpoint, jsonBody);

                // Nếu lệnh postJson không ném ra exception, coi như thành công.
                if (callback != null) {
                    callback.onSuccess();
                }

            } catch (Exception e) {
                Log.e("TeamRepo", "Lỗi nghiêm trọng khi thêm nhiều thành viên: ", e);
                if (callback != null) {
                    // Nếu có lỗi, gọi callback onError để UseCase có thể xử lý.
                    callback.onError("Lỗi khi thêm nhiều thành viên: " + e.getMessage());
                }
            }
        }).start();
    }
}
