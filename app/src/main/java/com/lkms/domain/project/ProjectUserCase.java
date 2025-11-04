package com.lkms.domain.project;

import com.lkms.data.model.java.Comment;
import com.lkms.data.model.java.Experiment;
import com.lkms.data.model.java.PeerReview;
import com.lkms.data.model.java.Project;
import com.lkms.data.model.java.User;
import com.lkms.data.repository.IProjectRepository;

import java.util.List;

/**
 * Lớp UseCase (hoặc Interactor) cho các nghiệp vụ liên quan đến Project.
 * Lớp này đóng gói logic nghiệp vụ và làm trung gian giữa ViewModel và Repository.
 *
 * Implement các UC:
 * - UC 16: Peer review
 * - UC 17: Discussion
 * - UC 18: Manage Project
 */
public class ProjectUserCase {

    private final IProjectRepository projectRepository;

    /**
     * Khởi tạo UseCase bằng cách tiêm (inject) một IProjectRepository.
     * @param projectRepository implementation của repository
     */
    public ProjectUserCase(IProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    // --- UC 18: Manage Project ---

    /**
     * Lấy danh sách tất cả các dự án mà một người dùng tham gia
     * (bao gồm cả dự án họ làm leader hoặc là thành viên của experiment).
     */
    public void getMyProjects(int userId, IProjectRepository.DataCallback<List<Project>> callback) {
        projectRepository.getProjectsByUserId(userId, callback);
    }

    /**
     * Tạo một dự án mới.
     * Đây là nơi để thêm logic nghiệp vụ (validation) trước khi gọi repository.
     */
    public void createProject(String title, int leaderId, IProjectRepository.DataCallback<Integer> callback) {
        // Business Logic: Kiểm tra xem tiêu đề có hợp lệ không
        if (title == null || title.trim().isEmpty()) {
            callback.onError("Tiêu đề dự án không được để trống.");
            return;
        }

        projectRepository.createProject(title, leaderId, callback);
    }

    /**
     * Lấy thông tin chi tiết (metadata) của một dự án.
     */
    public void getProjectDetails(int projectId, IProjectRepository.DataCallback<Project> callback) {
        projectRepository.getProjectDetails(projectId, callback);
    }

    /**
     * Lấy danh sách thành viên của một dự án.
     * (Bao gồm leader và tất cả thành viên của các experiment).
     */
    public void getProjectMembers(int projectId, IProjectRepository.DataCallback<List<User>> callback) {
        projectRepository.getProjectMembers(projectId, callback);
    }

    /**
     * Lấy danh sách các thí nghiệm thuộc một dự án.
     */
    public void getProjectExperiments(int projectId, IProjectRepository.DataCallback<List<Experiment>> callback) {
        projectRepository.getExperimentsInProject(projectId, callback);
    }

    // --- UC 16: Peer Review ---

    /**
     * Lấy tất cả các buổi Peer Review đã được lên lịch cho dự án.
     */
    public void getProjectPeerReviews(int projectId, IProjectRepository.DataCallback<List<PeerReview>> callback) {
        projectRepository.getPeerReviewsByProjectId(projectId, callback);
    }

    /**
     * Tạo một lịch Peer Review mới.
     */
    public void createPeerReview(PeerReview review, IProjectRepository.DataCallback<Boolean> callback) {
        // Business Logic: Validate dữ liệu đầu vào
        if (review.getProjectId() == null) {
            callback.onError("Cần có ProjectId để tạo Peer Review.");
            return;
        }
        if (review.getStartTime() == null || review.getStartTime().isEmpty()) {
            callback.onError("Thời gian bắt đầu không được để trống.");
            return;
        }
        if (review.getDetail() == null || review.getDetail().trim().isEmpty()) {
            callback.onError("Nội dung/chi tiết buổi họp không được để trống.");
            return;
        }

        projectRepository.createPeerReview(review, callback);
    }

    // --- UC 17: Discussion ---

    /**
     * Lấy tất cả các thảo luận (comment) từ tất cả các thí nghiệm con
     * của một dự án, hoạt động như một "forum" thảo luận chung.
     */
    public void getProjectDiscussions(int projectId, IProjectRepository.DataCallback<List<Comment>> callback) {
        projectRepository.getProjectDiscussions(projectId, callback);
    }

}