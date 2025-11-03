package com.lkms.data.repository.implement.java;

import static com.lkms.BuildConfig.SUPABASE_URL;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lkms.data.model.java.Project;
import com.lkms.data.repository.IProjectRepository;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Triển khai IProjectRepository sử dụng Supabase REST API.
 */
public class ProjectRepositoryImplJava implements IProjectRepository {

    private static final Gson gson = new Gson();

    @Override
    public void getAllProjects(ProjectListCallback callback) {
        new Thread(() -> {
            try {
                // Endpoint để lấy tất cả các bản ghi từ bảng "Project"
                String endpoint = SUPABASE_URL + "/rest/v1/Project?select=*";
                String json = HttpHelper.getJson(endpoint);

                // Chuyển chuỗi JSON nhận được thành một List<Project>
                Type listType = new TypeToken<List<Project>>() {}.getType();
                List<Project> projects = gson.fromJson(json, listType);

                // Gọi callback onSuccess và truyền vào danh sách project
                callback.onSuccess(projects);

            } catch (Exception e) {
                // Nếu có lỗi, gọi callback onError
                callback.onError("Lỗi khi tải danh sách Project: " + e.getMessage());
            }
        }).start();
    }
}
