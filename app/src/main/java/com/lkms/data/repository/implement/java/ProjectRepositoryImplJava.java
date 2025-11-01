package com.lkms.data.repository.implement.java;

import static com.lkms.BuildConfig.SUPABASE_URL;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lkms.data.model.java.Comment;
import com.lkms.data.model.java.Experiment;
import com.lkms.data.model.java.PeerReview;
import com.lkms.data.model.java.Project;
import com.lkms.data.model.java.User;
import com.lkms.data.repository.IProjectRepository;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProjectRepositoryImplJava implements IProjectRepository {

    private static final Gson gson = new Gson();
    private static final String TAG = "ProjectRepository";

    /**
     * Lấy các dự án mà một người dùng tham gia.
     * Một người dùng được coi là "tham gia" nếu họ là:
     * 1. Người lãnh đạo dự án (projectLeaderId).
     * 2. Là thành viên "Team" của bất kỳ "Experiment" nào thuộc dự án đó.
     */
    @Override
    public void getProjectsByUserId(int userId, DataCallback<List<Project>> callback) {
        new Thread(() -> {
            try {
                // Sử dụng HashSet để lưu trữ các projectId duy nhất
                HashSet<Integer> projectIds = new HashSet<>();

                // 1. Lấy các dự án mà người dùng là Leader
                String leaderEndpoint = SUPABASE_URL + "/rest/v1/Project?select=projectId&projectLeaderId=eq." + userId;
                String leaderJson = HttpHelper.getJson(leaderEndpoint);
                Type idMapListType = new TypeToken<List<Map<String, Integer>>>() {}.getType();
                List<Map<String, Integer>> leaderProjects = gson.fromJson(leaderJson, idMapListType);
                for (Map<String, Integer> map : leaderProjects) {
                    projectIds.add(map.get("projectId"));
                }

                // 2. Lấy các experimentId mà người dùng là thành viên "Team"
                String teamEndpoint = SUPABASE_URL + "/rest/v1/Team?select=experimentId&userId=eq." + userId;
                String teamJson = HttpHelper.getJson(teamEndpoint);
                List<Map<String, Integer>> teamExperiments = gson.fromJson(teamJson, idMapListType);

                List<Integer> experimentIds = teamExperiments.stream()
                        .map(map -> map.get("experimentId"))
                        .collect(Collectors.toList());

                // 3. Từ experimentIds, lấy các projectId liên quan
                if (!experimentIds.isEmpty()) {
                    String expIdList = experimentIds.toString().replace("[", "(").replace("]", ")");
                    String expEndpoint = SUPABASE_URL + "/rest/v1/Experiment?select=projectId&experimentId=in." + expIdList;
                    String expJson = HttpHelper.getJson(expEndpoint);
                    List<Map<String, Integer>> expProjects = gson.fromJson(expJson, idMapListType);

                    for (Map<String, Integer> map : expProjects) {
                        projectIds.add(map.get("projectId"));
                    }
                }

                // 4. Lấy thông tin chi tiết của tất cả các project đã tìm thấy
                if (projectIds.isEmpty()) {
                    callback.onSuccess(new ArrayList<>()); // Trả về danh sách rỗng nếu không tìm thấy
                    return;
                }

                String finalIdList = projectIds.toString().replace("[", "(").replace("]", ")");
                String finalEndpoint = SUPABASE_URL + "/rest/v1/Project?select=*&projectId=in." + finalIdList;
                String finalJson = HttpHelper.getJson(finalEndpoint);

                Type projectListType = new TypeToken<List<Project>>() {}.getType();
                List<Project> projects = gson.fromJson(finalJson, projectListType);
                callback.onSuccess(projects);

            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi lấy danh sách dự án: " + e.getMessage());
                callback.onError("Lỗi khi lấy danh sách dự án: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Lấy thông tin chi tiết của một dự án bằng ID.
     */
    @Override
    public void getProjectDetails(int projectId, DataCallback<Project> callback) {
        new Thread(() -> {
            try {
                // Lấy một dự án bằng PK, giới hạn 1
                String endpoint = SUPABASE_URL + "/rest/v1/Project?select=*&projectId=eq." + projectId + "&limit=1";
                String json = HttpHelper.getJson(endpoint);

                Type listType = new TypeToken<List<Project>>() {}.getType();
                List<Project> projects = gson.fromJson(json, listType);

                if (projects != null && !projects.isEmpty()) {
                    callback.onSuccess(projects.get(0));
                } else {
                    callback.onError("Không tìm thấy dự án với ID: " + projectId);
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi lấy chi tiết dự án: " + e.getMessage());
                callback.onError("Lỗi khi lấy chi tiết dự án: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Tạo một dự án mới.
     */
    @Override
    public void createProject(String title, int leaderId, DataCallback<Integer> callback) {
        new Thread(() -> {
            try {
                Project newProject = new Project(null, title, leaderId);
                String jsonBody = gson.toJson(newProject);

                String endpoint = SUPABASE_URL + "/rest/v1/Project?select=*";
                String response = HttpHelper.postJson(endpoint, jsonBody); // Bước 1: Thành công

                // --- ĐÂY LÀ PHẦN SỬA LỖI ---
                // Code này sẽ đọc phản hồi là một MẢNG [...]
                Type listType = new TypeToken<List<Project>>() {}.getType();
                List<Project> created = gson.fromJson(response, listType); // Bước 2: Sẽ thành công

                if (created != null && !created.isEmpty() && created.get(0).getProjectId() != null) {
                    callback.onSuccess(created.get(0).getProjectId());
                } else {
                    callback.onError("Không thể tạo dự án mới.");
                }
                // --- KẾT THÚC ---

            } catch (Exception e) {
                // Lỗi của bạn đang xảy ra ở đây, vì gson.fromJson thất bại
                Log.e(TAG, "Lỗi khi tạo dự án: " + e.getMessage());
                callback.onError("Lỗi khi tạo dự án: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Lấy tất cả các thí nghiệm thuộc một dự án.
     */
    @Override
    public void getExperimentsInProject(int projectId, DataCallback<List<Experiment>> callback) {
        new Thread(() -> {
            try {
                String endpoint = SUPABASE_URL + "/rest/v1/Experiment?select=*&projectId=eq." + projectId;
                String json = HttpHelper.getJson(endpoint);

                Type listType = new TypeToken<List<Experiment>>() {}.getType();
                List<Experiment> experiments = gson.fromJson(json, listType);

                callback.onSuccess(experiments);
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi lấy thí nghiệm của dự án: " + e.getMessage());
                callback.onError("Lỗi khi lấy thí nghiệm của dự án: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Lấy danh sách thành viên của một dự án.
     * Thành viên = Leader + tất cả các thành viên "Team" của các thí nghiệm trong dự án.
     */
    @Override
    public void getProjectMembers(int projectId, DataCallback<List<User>> callback) {
        new Thread(() -> {
            try {
                HashSet<Integer> userIds = new HashSet<>();

                // 1. Lấy Project Leader
                String leaderEndpoint = SUPABASE_URL + "/rest/v1/Project?select=projectLeaderId&projectId=eq." + projectId + "&limit=1";
                String leaderJson = HttpHelper.getJson(leaderEndpoint);
                Type leaderMapList = new TypeToken<List<Map<String, Integer>>>() {}.getType();
                List<Map<String, Integer>> leaderResult = gson.fromJson(leaderJson, leaderMapList);
                if (leaderResult != null && !leaderResult.isEmpty()) {
                    userIds.add(leaderResult.get(0).get("projectLeaderId"));
                }

                // 2. Lấy danh sách experimentId của dự án
                String expEndpoint = SUPABASE_URL + "/rest/v1/Experiment?select=experimentId&projectId=eq." + projectId;
                String expJson = HttpHelper.getJson(expEndpoint);
                Type idMapListType = new TypeToken<List<Map<String, Integer>>>() {}.getType();
                List<Map<String, Integer>> expList = gson.fromJson(expJson, idMapListType);

                List<Integer> experimentIds = expList.stream()
                        .map(map -> map.get("experimentId"))
                        .collect(Collectors.toList());

                // 3. Lấy tất cả userId từ bảng "Team" cho các experimentId đó
                if (!experimentIds.isEmpty()) {
                    String expIdList = experimentIds.toString().replace("[", "(").replace("]", ")");
                    String teamEndpoint = SUPABASE_URL + "/rest/v1/Team?select=userId&experimentId=in." + expIdList;
                    String teamJson = HttpHelper.getJson(teamEndpoint);
                    List<Map<String, Integer>> teamMembers = gson.fromJson(teamJson, idMapListType);

                    for (Map<String, Integer> map : teamMembers) {
                        userIds.add(map.get("userId"));
                    }
                }

                // 4. Lấy thông tin User chi tiết từ danh sách userId đã tổng hợp
                if (userIds.isEmpty()) {
                    callback.onSuccess(new ArrayList<>());
                    return;
                }

                String userIdList = userIds.toString().replace("[", "(").replace("]", ")");
                String userEndpoint = SUPABASE_URL + "/rest/v1/User?select=*&userId=in." + userIdList;
                String userJson = HttpHelper.getJson(userEndpoint);

                Type userListType = new TypeToken<List<User>>() {}.getType();
                List<User> users = gson.fromJson(userJson, userListType);
                callback.onSuccess(users);

            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi lấy thành viên dự án: " + e.getMessage());
                callback.onError("Lỗi khi lấy thành viên dự án: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Lấy các buổi Peer Review của một dự án.
     */
    @Override
    public void getPeerReviewsByProjectId(int projectId, DataCallback<List<PeerReview>> callback) {
        new Thread(() -> {
            try {
                String endpoint = SUPABASE_URL + "/rest/v1/PeerReview?select=*&projectId=eq." + projectId;
                String json = HttpHelper.getJson(endpoint);

                Type listType = new TypeToken<List<PeerReview>>() {}.getType();
                List<PeerReview> reviews = gson.fromJson(json, listType);

                callback.onSuccess(reviews);
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi lấy Peer Reviews: " + e.getMessage());
                callback.onError("Lỗi khi lấy Peer Reviews: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Tạo một buổi Peer Review mới.
     */
    @Override
    public void createPeerReview(PeerReview review, DataCallback<Boolean> callback) {
        new Thread(() -> {
            try {
                String jsonBody = gson.toJson(review);
                // Chỉ cần POST, không cần select=*.
                // HttpHelper.postJson sẽ throw Exception nếu mã http không phải 2xx
                String endpoint = SUPABASE_URL + "/rest/v1/PeerReview";
                HttpHelper.postJson(endpoint, jsonBody);

                // Nếu không có lỗi, coi như thành công
                callback.onSuccess(true);
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi tạo Peer Review: " + e.getMessage());
                callback.onError("Lỗi khi tạo Peer Review: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Lấy các thảo luận (Comment) của một dự án.
     * Logic: Thảo luận dự án là TẤT CẢ các comment từ TẤT CẢ các thí nghiệm thuộc dự án đó.
     */
    @Override
    public void getProjectDiscussions(int projectId, DataCallback<List<Comment>> callback) {
        new Thread(() -> {
            try {
                // 1. Lấy danh sách experimentId của dự án
                String expEndpoint = SUPABASE_URL + "/rest/v1/Experiment?select=experimentId&projectId=eq." + projectId;
                String expJson = HttpHelper.getJson(expEndpoint);
                Type idMapListType = new TypeToken<List<Map<String, Integer>>>() {}.getType();
                List<Map<String, Integer>> expList = gson.fromJson(expJson, idMapListType);

                List<Integer> experimentIds = expList.stream()
                        .map(map -> map.get("experimentId"))
                        .collect(Collectors.toList());

                if (experimentIds.isEmpty()) {
                    callback.onSuccess(new ArrayList<>());
                    return;
                }

                // 2. Lấy tất cả Comment có experimentId nằm trong danh sách
                String expIdList = experimentIds.toString().replace("[", "(").replace("]", ")");
                String commentEndpoint = SUPABASE_URL + "/rest/v1/Comment?select=*&experimentId=in." + expIdList;
                String commentJson = HttpHelper.getJson(commentEndpoint);

                Type commentListType = new TypeToken<List<Comment>>() {}.getType();
                List<Comment> comments = gson.fromJson(commentJson, commentListType);
                callback.onSuccess(comments);

            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi lấy thảo luận dự án: " + e.getMessage());
                callback.onError("Lỗi khi lấy thảo luận dự án: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Cập nhật thông tin một dự án (ví dụ: title, leader).
     * Sử dụng phương thức PATCH.
     */
    @Override
    public void updateProject(Project project, DataCallback<Boolean> callback) {
        new Thread(() -> {
            try {
                // Giả định `HttpHelper` có phương thức `patchJson`
                String jsonBody = gson.toJson(project);
                String endpoint = SUPABASE_URL + "/rest/v1/Project?projectId=eq." + project.getProjectId();

                HttpHelper.patchJson(endpoint, jsonBody);

                // Nếu patchJson không ném lỗi, coi như thành công
                callback.onSuccess(true);
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi cập nhật dự án: " + e.getMessage());
                callback.onError("Lỗi khi cập nhật dự án: " + e.getMessage());
            }
        }).start();
    }
}