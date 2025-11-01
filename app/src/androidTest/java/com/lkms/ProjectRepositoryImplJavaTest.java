package com.lkms;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.lkms.data.model.java.*;
import com.lkms.data.repository.IProjectRepository;
import com.lkms.data.repository.implement.java.ProjectRepositoryImplJava;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.List;

/**
 * Đây là một Instrumentation Test cho ProjectRepositoryImplJava.
 * Nó sẽ thực hiện các lệnh gọi mạng THẬT tới backend Supabase của bạn.
 *
 * QUAN TRỌNG:
 * 1. Đảm bảo thiết bị/máy ảo của bạn có kết nối internet.
 * 2. Các bài test này giả định rằng CSDL của bạn có dữ liệu (ví dụ: userId=1, projectId=1).
 * Nếu không, các bài test "get" có thể thất bại hoặc trả về danh sách rỗng.
 */
@RunWith(AndroidJUnit4.class)
public class ProjectRepositoryImplJavaTest {

    private final ProjectRepositoryImplJava repo = new ProjectRepositoryImplJava();
    private final long WAIT_TIME = 3000; // 3 giây

    // ✅ 1. Test tạo mới Project
    @Test
    public void testCreateProject() throws InterruptedException {
        // ID 1 phải tồn tại trong bảng User để làm projectLeader
        int leaderId = 1;

        repo.createProject(
                "Android Test Project " + System.currentTimeMillis(),
                leaderId,
                new IProjectRepository.DataCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer id) {
                        System.out.println("✅ Created Project ID: " + id);
                    }

                    @Override
                    public void onError(String error) {
                        System.out.println("❌ Error creating project: " + error);
                    }
                }
        );
        Thread.sleep(WAIT_TIME);
    }

    // ✅ 2. Test lấy danh sách Project theo UserId
    @Test
    public void testGetProjectsByUserId() throws InterruptedException {
        // User ID 1 phải tồn tại và đang tham gia (làm leader hoặc thành viên)
        // của một số dự án
        int userId = 1;

        repo.getProjectsByUserId(
                userId,
                new IProjectRepository.DataCallback<List<Project>>() {
                    @Override
                    public void onSuccess(List<Project> projects) {
                        System.out.println("✅ Found " + projects.size() + " projects for user " + userId);
                        for (Project p : projects) {
                            System.out.println(" - " + p.getProjectTitle() + " (ID: " + p.getProjectId() + ")");
                        }
                    }

                    @Override
                    public void onError(String error) {
                        System.out.println("❌ Error getting projects by user: " + error);
                    }
                }
        );
        Thread.sleep(WAIT_TIME);
    }

    // ✅ 3. Test lấy chi tiết Project
    @Test
    public void testGetProjectDetails() throws InterruptedException {
        // Project ID 1 phải tồn tại
        int projectId = 1;

        repo.getProjectDetails(
                projectId,
                new IProjectRepository.DataCallback<Project>() {
                    @Override
                    public void onSuccess(Project project) {
                        System.out.println("✅ Got details for: " + project.getProjectTitle());
                        System.out.println(" - Leader ID: " + project.getProjectLeaderId());
                    }

                    @Override
                    public void onError(String error) {
                        System.out.println("❌ Error getting project details: " + error);
                    }
                }
        );
        Thread.sleep(WAIT_TIME);
    }

    // ✅ 4. Test lấy Experiments trong Project
    @Test
    public void testGetExperimentsInProject() throws InterruptedException {
        // Project ID 1 phải tồn tại và có chứa Experiment
        int projectId = 1;

        repo.getExperimentsInProject(
                projectId,
                new IProjectRepository.DataCallback<List<Experiment>>() {
                    @Override
                    public void onSuccess(List<Experiment> experiments) {
                        System.out.println("✅ Found " + experiments.size() + " experiments in project " + projectId);
                        for (Experiment e : experiments) {
                            System.out.println(" - " + e.getExperimentTitle());
                        }
                    }

                    @Override
                    public void onError(String error) {
                        System.out.println("❌ Error getting experiments in project: " + error);
                    }
                }
        );
        Thread.sleep(WAIT_TIME);
    }

    // ✅ 5. Test lấy thành viên Project
    @Test
    public void testGetProjectMembers() throws InterruptedException {
        // Project ID 1 phải tồn tại
        int projectId = 1;

        repo.getProjectMembers(
                projectId,
                new IProjectRepository.DataCallback<List<User>>() {
                    @Override
                    public void onSuccess(List<User> users) {
                        System.out.println("✅ Found " + users.size() + " members in project " + projectId);
                        for (User u : users) {
                            System.out.println(" - " + u.getName() + " (ID: " + u.getUserId() + ")");
                        }
                    }

                    @Override
                    public void onError(String error) {
                        System.out.println("❌ Error getting project members: " + error);
                    }
                }
        );
        Thread.sleep(WAIT_TIME);
    }

    // ✅ 6. Test lấy danh sách Peer Review
    @Test
    public void testGetPeerReviewsByProjectId() throws InterruptedException {
        // Project ID 1 phải tồn tại
        int projectId = 1;

        repo.getPeerReviewsByProjectId(
                projectId,
                new IProjectRepository.DataCallback<List<PeerReview>>() {
                    @Override
                    public void onSuccess(List<PeerReview> reviews) {
                        System.out.println("✅ Found " + reviews.size() + " peer reviews for project " + projectId);
                        for (PeerReview pr : reviews) {
                            System.out.println(" - " + pr.getDetail() + " @ " + pr.getStartTime());
                        }
                    }

                    @Override
                    public void onError(String error) {
                        System.out.println("❌ Error getting peer reviews: " + error);
                    }
                }
        );
        Thread.sleep(WAIT_TIME);
    }

    // ✅ 7. Test tạo Peer Review
    @Test
    public void testCreatePeerReview() throws InterruptedException {
        PeerReview review = new PeerReview(
                null, // reviewId (auto-increment)
                1,    // projectId 1 phải tồn tại
                new Date().toString(),
                null,
                "Test peer review from Android " + System.currentTimeMillis()
        );

        repo.createPeerReview(
                review,
                new IProjectRepository.DataCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean success) {
                        System.out.println("✅ Peer review created: " + success);
                    }

                    @Override
                    public void onError(String error) {
                        System.out.println("❌ Error creating peer review: " + error);
                    }
                }
        );
        Thread.sleep(WAIT_TIME);
    }

    // ✅ 8. Test lấy thảo luận Project
    @Test
    public void testGetProjectDiscussions() throws InterruptedException {
        // Project ID 1 phải tồn tại
        int projectId = 1;

        repo.getProjectDiscussions(
                projectId,
                new IProjectRepository.DataCallback<List<Comment>>() {
                    @Override
                    public void onSuccess(List<Comment> comments) {
                        System.out.println("✅ Found " + comments.size() + " discussion comments for project " + projectId);
                        for (Comment c : comments) {
                            System.out.println(" - " + c.getCommentText());
                        }
                    }

                    @Override
                    public void onError(String error) {
                        System.out.println("❌ Error getting project discussions: " + error);
                    }
                }
        );
        Thread.sleep(WAIT_TIME);
    }

    // ✅ 9. Test cập nhật Project
    @Test
    public void testUpdateProject() throws InterruptedException {
        // Project ID 1 và User ID 1 phải tồn tại
        Project projectToUpdate = new Project(
                1,
                "Updated Title " + System.currentTimeMillis(),
                1
        );

        repo.updateProject(
                projectToUpdate,
                new IProjectRepository.DataCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean success) {
                        System.out.println("✅ Project updated successfully: " + success);
                    }

                    @Override
                    public void onError(String error) {
                        System.out.println("❌ Error updating project: " + error);
                    }
                }
        );
        Thread.sleep(WAIT_TIME);
    }
}