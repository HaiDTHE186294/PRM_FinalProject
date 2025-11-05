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
                String endpoint = SUPABASE_URL + "/rest/v1/rpc/get_projects_by_user_id";
                String jsonBody = gson.toJson(Map.of("p_user_id", userId));
                String projectJson = HttpHelper.postJson(endpoint, jsonBody);
                Type projectListType = new TypeToken<List<Project>>() {}.getType();
                List<Project> projects = gson.fromJson(projectJson, projectListType);
                callback.onSuccess(projects);
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi lấy danh sách dự án bằng RPC: " + e.getMessage());
                callback.onError("Lỗi khi lấy danh sách dự án bằng RPC: " + e.getMessage());
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
                String response = HttpHelper.postJson(endpoint, jsonBody);

                Type listType = new TypeToken<List<Project>>() {}.getType();
                List<Project> created = gson.fromJson(response, listType);

                if (created != null && !created.isEmpty() && created.get(0).getProjectId() != null) {
                    callback.onSuccess(created.get(0).getProjectId());
                } else {
                    callback.onError("Không thể tạo dự án mới.");
                }

            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi tạo dự án: " + e.getMessage());
                callback.onError("Lỗi khi tạo dự án: " + e.getMessage());
            }
        }).start();
    }

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


    @Override
    public void getProjectMembers(int projectId, DataCallback<List<User>> callback) {
        new Thread(() -> {
            try {

                String jsonBody = gson.toJson(Map.of("p_project_id", projectId));
                String endpoint = SUPABASE_URL + "/rest/v1/rpc/get_project_members_optimized";
                String userJson = HttpHelper.postJson(endpoint, jsonBody);

                Type userListType = new TypeToken<List<User>>() {}.getType();
                List<User> users = gson.fromJson(userJson, userListType);

                callback.onSuccess(users);

            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi lấy thành viên dự án bằng RPC: " + e.getMessage());
                callback.onError("Lỗi khi lấy thành viên dự án bằng RPC: " + e.getMessage());
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
                String endpoint = SUPABASE_URL + "/rest/v1/PeerReview";
                HttpHelper.postJson(endpoint, jsonBody);
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
                String jsonBody = gson.toJson(project);
                String endpoint = SUPABASE_URL + "/rest/v1/Project?projectId=eq." + project.getProjectId();
                HttpHelper.patchJson(endpoint, jsonBody);
                callback.onSuccess(true);
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi cập nhật dự án: " + e.getMessage());
                callback.onError("Lỗi khi cập nhật dự án: " + e.getMessage());
            }
        }).start();
    }
}