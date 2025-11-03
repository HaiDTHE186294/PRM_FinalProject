package com.lkms.ui.createexperiment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.lkms.data.model.java.Project;
import com.lkms.data.model.java.Protocol;
import com.lkms.data.repository.IExperimentRepository;
import com.lkms.data.repository.IProjectRepository;
import com.lkms.data.repository.IProtocolRepository;
import com.lkms.domain.createexperiment.CreateFullExperimentUseCase;
import com.lkms.domain.createexperiment.GetAvailableProjectsUseCase;
// MỚI: Thêm UseCase để lấy chi tiết một protocol
import com.lkms.domain.protocolusecase.GetProtocolDetailsUseCase;
import java.util.List;

public class CreateNewExperimentViewModel extends ViewModel {

    private final CreateFullExperimentUseCase createFullExperimentUseCase;
    // MỚI: UseCase để lấy chi tiết protocol
    private final GetProtocolDetailsUseCase getProtocolDetailsUseCase;
    private final GetAvailableProjectsUseCase getProjectsUseCase;

    // --- LiveData cho giao diện ---
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    private final MutableLiveData<Boolean> _creationSuccess = new MutableLiveData<>(false);
    public final LiveData<Boolean> creationSuccess = _creationSuccess;

    // THAY ĐỔI: Không cần danh sách protocols, chỉ cần 1 protocol cụ thể
    private final MutableLiveData<Protocol> _protocol = new MutableLiveData<>();
    public final LiveData<Protocol> protocol = _protocol;

    private final MutableLiveData<List<Project>> _projects = new MutableLiveData<>();
    public final LiveData<List<Project>> projects = _projects;

    // Biến để lưu protocolId lại
    private int currentProtocolId = -1;

    // THAY ĐỔI: Cập nhật Constructor
    public CreateNewExperimentViewModel(
            CreateFullExperimentUseCase createFullExperimentUseCase,
            GetProtocolDetailsUseCase getProtocolDetailsUseCase, // <- Sửa ở đây
            GetAvailableProjectsUseCase getProjectsUseCase
    ) {
        this.createFullExperimentUseCase = createFullExperimentUseCase;
        this.getProtocolDetailsUseCase = getProtocolDetailsUseCase; // <- Sửa ở đây
        this.getProjectsUseCase = getProjectsUseCase;
    }

    // THAY ĐỔI: Hàm này giờ sẽ nhận protocolId từ Activity
    public void loadInitialData(int protocolId) {
        if (protocolId == -1) {
            _error.setValue("Protocol ID không hợp lệ.");
            return;
        }
        this.currentProtocolId = protocolId;
        _isLoading.setValue(true);

        // Bước 1: Tải chi tiết protocol trước
        getProtocolDetailsUseCase.execute(protocolId, new IProtocolRepository.ProtocolContentCallback() {
            @Override
            public void onProtocolReceived(Protocol result) {
                _protocol.postValue(result);
                // Bước 2: Sau khi có protocol, tải danh sách project
                loadProjects();
            }

            @Override
            public void onStepsReceived(List<com.lkms.data.model.java.ProtocolStep> steps) { /* Không cần ở màn này */ }

            @Override
            public void onItemsReceived(List<com.lkms.data.model.java.ProtocolItem> items) { /* Không cần ở màn này */ }

            @Override
            public void onError(String errorMessage) {
                _error.postValue("Lỗi tải Protocol: " + errorMessage);
                _isLoading.postValue(false);
            }
        });
    }

    // Tải danh sách projects (giữ nguyên)
    private void loadProjects() {
        getProjectsUseCase.execute(new IProjectRepository.ProjectListCallback() {
            @Override public void onSuccess(List<Project> result) { _projects.postValue(result); _isLoading.postValue(false); }
            @Override public void onError(String errorMessage) { _error.postValue("Lỗi tải Projects: " + errorMessage); _isLoading.postValue(false); }
        });
    }

    // THAY ĐỔI: Hàm tạo thí nghiệm không cần nhận Protocol nữa, vì đã có ID
    public void createExperiment(String title, String objective, Project selectedProject, int userId) {
        if (title == null || title.trim().isEmpty()) { _error.setValue("Tên thí nghiệm không được để trống."); return; }
        if (currentProtocolId == -1) { _error.setValue("Protocol không hợp lệ."); return; }
        if (selectedProject == null) { _error.setValue("Vui lòng chọn một dự án (Project)."); return; }
        if (userId == -1) { _error.setValue("Không thể xác thực người dùng. Vui lòng đăng nhập lại."); return; }

        _isLoading.setValue(true);
        // Dùng `currentProtocolId` đã lưu
        createFullExperimentUseCase.execute(
                title, objective, currentProtocolId, userId, selectedProject.getProjectId(),
                new IExperimentRepository.GenericCallback() {
                    @Override public void onSuccess() { _creationSuccess.postValue(true); _isLoading.postValue(false); }
                    @Override public void onError(String errorMessage) { _error.postValue(errorMessage); _isLoading.postValue(false); }
                }
        );
    }
}
