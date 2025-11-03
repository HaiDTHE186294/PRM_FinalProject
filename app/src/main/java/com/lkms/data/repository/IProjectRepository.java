package com.lkms.data.repository;

import com.lkms.data.model.java.Comment;
import com.lkms.data.model.java.Experiment;
import com.lkms.data.model.java.PeerReview;
import com.lkms.data.model.java.Project;
import com.lkms.data.model.java.User;

import java.util.List;

// Giả định có các mô hình dữ liệu Project, Experiment, PeerReview, User, Comment
// và một Interface Callback để xử lý kết quả bất đồng bộ.

public interface IProjectRepository {

    /**
     * Định nghĩa Callback Interface chung (Giả định)
     */
    interface DataCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }

    /**
     * UC 18: Lấy danh sách Project mà một người dùng tham gia.
     * @param userId ID của người dùng hiện tại.
     * @param callback Callback để nhận danh sách Project.
     */
    void getProjectsByUserId(int userId, DataCallback<List<Project>> callback);

    /**
     * UC 18: Lấy chi tiết của một Project.
     * @param projectId ID của Project.
     * @param callback Callback để nhận đối tượng Project chi tiết.
     */
    void getProjectDetails(int projectId, DataCallback<Project> callback);

    /**
     * UC 18: Tạo Project mới. Chức năng dành cho Lab Manager.
     * @param title Tiêu đề Project (projectTitle).
     * @param leaderId ID của người đứng đầu dự án (projectLeaderId).
     * @param callback Callback để nhận ID của Project vừa được tạo.
     *
     * Lưu ý: Trong Task PF-32, có comment gợi ý "Trả về thì trả hẳn id rõ ràng, đừng trả về 0" [2].
     * Việc trả về ID rõ ràng cần được thực hiện trong hàm callback onSuccess.
     */
    void createProject(String title, int leaderId, DataCallback<Integer> callback);

    /**
     * UC 18: Lấy danh sách các thí nghiệm (Experiment) thuộc Project đó.
     * @param projectId ID của Project.
     * @param callback Callback để nhận danh sách Experiment.
     */
    void getExperimentsInProject(int projectId, DataCallback<List<Experiment>> callback);

    /**
     * UC 18: Lấy danh sách thành viên tham gia Project.
     * @param projectId ID của Project.
     * @param callback Callback để nhận danh sách User.
     */
    void getProjectMembers(int projectId, DataCallback<List<User>> callback);

    /**
     * UC 16: Lấy danh sách các buổi Peer Review liên quan đến Project.
     * @param projectId ID của Project.
     * @param callback Callback để nhận danh sách Peer Review.
     */
    void getPeerReviewsByProjectId(int projectId, DataCallback<List<PeerReview>> callback);

    /**
     * UC 16: Thêm mới lịch họp/buổi Peer Review.
     * @param review Đối tượng PeerReview.
     * @param callback Callback thông báo trạng thái thành công/thất bại (Boolean).
     */
    void createPeerReview(PeerReview review, DataCallback<Boolean> callback);

    /**
     * UC 17: Lấy các bình luận Discussion cho Project.
     * @param projectId ID của Project.
     * @param callback Callback để nhận danh sách Comment.
     */
    void getProjectDiscussions(int projectId, DataCallback<List<Comment>> callback);

    /**
     * Cập nhật thông tin Project.
     * @param project Đối tượng Project đã được sửa đổi.
     * @param callback Callback thông báo trạng thái thành công/thất bại (Boolean).
     */
    void updateProject(Project project, DataCallback<Boolean> callback);
}