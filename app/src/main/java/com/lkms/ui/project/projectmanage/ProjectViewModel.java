package com.lkms.ui.project.projectmanage; // Gói (package) này có thể cần được điều chỉnh

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lkms.data.model.java.Comment;
import com.lkms.data.model.java.Experiment;
import com.lkms.data.model.java.PeerReview;
import com.lkms.data.model.java.Project;
import com.lkms.data.model.java.User;
import com.lkms.data.repository.IProjectRepository; // Cần thiết cho các DataCallback
import com.lkms.domain.project.ProjectUserCase;

import java.util.List;

/**
 * ViewModel cho các chức năng liên quan đến Project (UC 16, 17, 18).
 * Lớp này giao tiếp với ProjectUserCase và cung cấp LiveData cho UI.
 */
public class ProjectViewModel extends ViewModel {

    private final ProjectUserCase projectUserCase;

    // --- LiveData cho Màn hình Danh sách Project (UC 18) ---
    private final MutableLiveData<List<Project>> _myProjects = new MutableLiveData<>();
    public LiveData<List<Project>> myProjects = _myProjects;

    private final MutableLiveData<Integer> _newProjectId = new MutableLiveData<>();
    public LiveData<Integer> newProjectId = _newProjectId;

    // --- LiveData cho Màn hình Chi tiết Project (UC 16, 17, 18) ---
    private final MutableLiveData<Project> _currentProjectDetails = new MutableLiveData<>();
    public LiveData<Project> currentProjectDetails = _currentProjectDetails;

    private final MutableLiveData<List<User>> _projectMembers = new MutableLiveData<>();
    public LiveData<List<User>> projectMembers = _projectMembers;

    private final MutableLiveData<List<Experiment>> _projectExperiments = new MutableLiveData<>();
    public LiveData<List<Experiment>> projectExperiments = _projectExperiments;

    private final MutableLiveData<List<PeerReview>> _peerReviews = new MutableLiveData<>();
    public LiveData<List<PeerReview>> peerReviews = _peerReviews;

    private final MutableLiveData<List<Comment>> _discussions = new MutableLiveData<>();
    public LiveData<List<Comment>> discussions = _discussions;

    // --- LiveData cho Trạng thái (Loading & Error) ---
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    private final MutableLiveData<Boolean> _peerReviewCreated = new MutableLiveData<>();
    public LiveData<Boolean> peerReviewCreated = _peerReviewCreated;


    /**
     * Khởi tạo ViewModel. ProjectUserCase sẽ được tiêm vào (Dependency Injection).
     */
    public ProjectViewModel(ProjectUserCase projectUserCase) {
        this.projectUserCase = projectUserCase;
    }

    // --- HÀM CHO UI GỌI (UC 18) ---

    /**
     * Tải danh sách các dự án mà người dùng tham gia.
     * UI sẽ quan sát (observe) 'myProjects'.
     */
    public void loadMyProjects(int userId) {
        _isLoading.setValue(true);
        projectUserCase.getMyProjects(userId, new IProjectRepository.DataCallback<List<Project>>() {
            @Override
            public void onSuccess(List<Project> projects) {
                _myProjects.postValue(projects);
                _isLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
            }
        });
    }

    public void createProject(String title, int leaderId) {
        _isLoading.setValue(true);
        projectUserCase.createProject(title, leaderId, new IProjectRepository.DataCallback<Integer>() {
            @Override
            public void onSuccess(Integer id) {
                _newProjectId.postValue(id);
                _isLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
            }
        });
    }
    public void loadProjectDetailsScreen(int projectId) {
        _isLoading.setValue(true);

        // Tải chi tiết dự án (Tên, Leader)
        projectUserCase.getProjectDetails(projectId, new IProjectRepository.DataCallback<Project>() {
            @Override
            public void onSuccess(Project project) {
                _currentProjectDetails.postValue(project);
            }

            @Override
            public void onError(String error) {
                _errorMessage.postValue(error);
            }
        });

        // Tải danh sách thành viên
        projectUserCase.getProjectMembers(projectId, new IProjectRepository.DataCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                _projectMembers.postValue(users);
            }
            @Override
            public void onError(String error) { /* Bỏ qua hoặc gộp lỗi */ }
        });

        // Tải danh sách thí nghiệm
        projectUserCase.getProjectExperiments(projectId, new IProjectRepository.DataCallback<List<Experiment>>() {
            @Override
            public void onSuccess(List<Experiment> experiments) {
                _projectExperiments.postValue(experiments);
            }
            @Override
            public void onError(String error) { /* Bỏ qua hoặc gộp lỗi */ }
        });

        // Giả định tải xong hết (có thể cải thiện bằng cách đợi tất cả)
        _isLoading.postValue(false);
    }

    // --- HÀM CHO UI GỌI (UC 16 & 17) ---

    /**
     * Tải tab Peer Review (UC 16).
     */
    public void loadPeerReviews(int projectId) {
        _isLoading.setValue(true);
        projectUserCase.getProjectPeerReviews(projectId, new IProjectRepository.DataCallback<List<PeerReview>>() {
            @Override
            public void onSuccess(List<PeerReview> peerReviews) {
                _peerReviews.postValue(peerReviews);
                _isLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
            }
        });
    }

    /**
     * Tải tab Discussion (UC 17).
     */
    public void loadDiscussions(int projectId) {
        _isLoading.setValue(true);
        projectUserCase.getProjectDiscussions(projectId, new IProjectRepository.DataCallback<List<Comment>>() {
            @Override
            public void onSuccess(List<Comment> comments) {
                _discussions.postValue(comments);
                _isLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
            }
        });
    }

    public void loadProjectDetails(int projectId) {
    }

    public void createPeerReview(PeerReview newReview) {
        _isLoading.setValue(true);
        projectUserCase.createPeerReview(newReview, new IProjectRepository.DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                _peerReviewCreated.postValue(true);
                _isLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
            }
        });
    }

}