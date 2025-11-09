package com.lkms.data.repository.implement.java;

import static com.lkms.BuildConfig.SUPABASE_URL;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lkms.data.model.java.Team;
import com.lkms.data.repository.ITeamRepository;

import java.lang.reflect.Type;
import java.util.List;

public class TeamRepositoryImplJava implements ITeamRepository {

    private static final String TAG = "TeamRepository";
    private static final String TEAM_TABLE = "Team";
    private static final Gson gson = new Gson();

    @Override
    public void addMember(Team teamMember, TeamMemberCallback callback) {
        new Thread(() -> {
            // Đảm bảo callback không null ngay từ đầu
            if (callback == null) return;

            try {
                if (teamMember == null || teamMember.getExperimentId() == null || teamMember.getUserId() == null) {
                    throw new IllegalArgumentException("Dữ liệu thành viên không hợp lệ (null).");
                }

                // Endpoint yêu cầu trả về bản ghi đã tạo
                String endpoint = SUPABASE_URL + "/rest/v1/" + TEAM_TABLE + "?select=*";
                String jsonBody = gson.toJson(teamMember);

                String responseJson = HttpHelper.postJson(endpoint, jsonBody);

                Type listType = new TypeToken<List<Team>>() {}.getType();
                List<Team> createdMembers = gson.fromJson(responseJson, listType);

                if (createdMembers != null && !createdMembers.isEmpty()) {
                    Team createdMember = createdMembers.get(0);
                    // Đảm bảo callback được gọi trên Main Thread
                    new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(createdMember));
                } else {
                    throw new Exception("Thêm thành viên thất bại. Server không trả về bản ghi đã tạo.");
                }

            } catch (Exception e) {
                // Giữ lại Log.e để ghi lại các lỗi quan trọng trong quá trình debug
                Log.e(TAG, "Lỗi nghiêm trọng khi thêm thành viên: ", e);
                final String errorMessage = "Lỗi khi thêm thành viên: " + e.getMessage();
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(errorMessage));
            }
        }).start();
    }

    @Override
    public void addMembers(List<Team> teamMembers, BulkTeamMemberCallback callback) {
        new Thread(() -> {
            // Đảm bảo callback không null ngay từ đầu
            if (callback == null) return;

            try {
                if (teamMembers == null || teamMembers.isEmpty()) {
                    throw new IllegalArgumentException("Danh sách thành viên để thêm không được rỗng.");
                }

                // Khi thêm hàng loạt, không cần `?select=*` để tối ưu băng thông
                String endpoint = SUPABASE_URL + "/rest/v1/" + TEAM_TABLE;
                String jsonBody = gson.toJson(teamMembers);

                HttpHelper.postJson(endpoint, jsonBody);

                // Nếu không có exception, coi như thành công
                new Handler(Looper.getMainLooper()).post(callback::onSuccess);

            } catch (Exception e) {
                // Giữ lại Log.e để ghi lại các lỗi quan trọng
                Log.e(TAG, "Lỗi nghiêm trọng khi thêm nhiều thành viên: ", e);
                final String errorMessage = "Lỗi khi thêm nhiều thành viên: " + e.getMessage();
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(errorMessage));
            }
        }).start();
    }
}
