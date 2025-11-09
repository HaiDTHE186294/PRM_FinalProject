package com.lkms.data.repository;

import com.lkms.data.model.java.Project;
import java.util.List;

/**
 * Interface Repository cho việc quản lý các Project.
 */
public interface IProjectRepositoryVjet {

    /**
     * Định nghĩa một Callback để trả về danh sách các Project.
     */
    interface ProjectListCallback {
        void onSuccess(List<Project> projects);
        void onError(String errorMessage);
    }

    /**
     * Lấy tất cả các project từ cơ sở dữ liệu.
     * @param callback Callback để nhận kết quả.
     */
    void getAllProjects(ProjectListCallback callback);

}
