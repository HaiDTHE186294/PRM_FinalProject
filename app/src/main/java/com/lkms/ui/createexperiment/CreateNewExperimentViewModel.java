package com.lkms.ui.createexperiment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.lkms.data.model.java.Project;
import com.lkms.data.model.java.Protocol;
import com.lkms.data.repository.IExperimentRepository;
import com.lkms.data.repository.IInventoryRepository;
import com.lkms.data.repository.IProjectRepositoryVjet;
import com.lkms.data.repository.IProtocolRepository;
import com.lkms.domain.createexperiment.CreateFullExperimentUseCase;
import com.lkms.domain.createexperiment.GetAvailableProjectsUseCase;
import com.lkms.domain.createexperiment.CheckInventoryUseCase;
import com.lkms.domain.createexperiment.DeductInventoryForExperimentUseCase;
import com.lkms.domain.protocolusecase.GetProtocolDetailsUseCase;
import java.util.List;

public class CreateNewExperimentViewModel extends ViewModel {
    // --- Khai báo các UseCase ---
    private final CreateFullExperimentUseCase createUseCase;
    private final DeductInventoryForExperimentUseCase deductUseCase;
    private final CheckInventoryUseCase checkUseCase;
    private final GetProtocolDetailsUseCase getProtocolDetailsUseCase;
    private final GetAvailableProjectsUseCase getProjectsUseCase;

    // --- LiveData cho giao diện ---
    // ⭐ SỬA LỖI SONARQUBE: Đổi tên biến theo quy tắc (bỏ dấu gạch dưới, thay bằng 'm') ⭐
    private final MutableLiveData<Boolean> mIsLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = mIsLoading;
    private final MutableLiveData<String> mError = new MutableLiveData<>();
    public final LiveData<String> error = mError;
    private final MutableLiveData<Boolean> mCreationSuccess = new MutableLiveData<>(false);
    public final LiveData<Boolean> creationSuccess = mCreationSuccess;
    private final MutableLiveData<Protocol> mProtocol = new MutableLiveData<>();
    public final LiveData<Protocol> protocol = mProtocol;
    private final MutableLiveData<List<Project>> mProjects = new MutableLiveData<>();
    public final LiveData<List<Project>> projects = mProjects;

    private int currentProtocolId = -1;

    // Constructor đã đúng, không cần sửa
    public CreateNewExperimentViewModel(
            CreateFullExperimentUseCase createUseCase,
            DeductInventoryForExperimentUseCase deductUseCase,
            CheckInventoryUseCase checkUseCase,
            GetProtocolDetailsUseCase getProtocolDetailsUseCase,
            GetAvailableProjectsUseCase getProjectsUseCase
    ) {
        this.createUseCase = createUseCase;
        this.deductUseCase = deductUseCase;
        this.checkUseCase = checkUseCase;
        this.getProtocolDetailsUseCase = getProtocolDetailsUseCase;
        this.getProjectsUseCase = getProjectsUseCase;
    }

    // SỬA: Dùng postValue và biến đã đổi tên
    public void loadInitialData(int protocolId) {
        if (protocolId == -1) {
            mError.postValue("Protocol ID không hợp lệ.");
            return;
        }
        this.currentProtocolId = protocolId;
        mIsLoading.postValue(true);
        getProtocolDetailsUseCase.execute(protocolId, new IProtocolRepository.ProtocolContentCallback() {
            @Override
            public void onProtocolReceived(Protocol result) {
                mProtocol.postValue(result);
                loadProjects();
            }
            @Override
            public void onStepsReceived(List<com.lkms.data.model.java.ProtocolStep> steps) { /* Không cần */ }
            @Override
            public void onItemsReceived(List<com.lkms.data.model.java.ProtocolItem> items) { /* Không cần */ }
            @Override
            public void onError(String errorMessage) {
                mError.postValue("Lỗi tải Protocol: " + errorMessage);
                mIsLoading.postValue(false);
            }
        });
    }

    // SỬA: Dùng postValue và biến đã đổi tên
    private void loadProjects() {
        getProjectsUseCase.execute(new IProjectRepositoryVjet.ProjectListCallback() {
            @Override public void onSuccess(List<Project> result) {
                mProjects.postValue(result);
                mIsLoading.postValue(false);
            }
            @Override public void onError(String errorMessage) {
                mError.postValue("Lỗi tải Projects: " + errorMessage);
                mIsLoading.postValue(false);
            }
        });
    }

    // SỬA: Dùng postValue và biến đã đổi tên
    public void createExperiment(String title, String objective, Project selectedProject, int userId) {
        if (title == null || title.trim().isEmpty()) { mError.postValue("Tên thí nghiệm không được để trống."); return; }
        if (currentProtocolId == -1) { mError.postValue("Protocol không hợp lệ."); return; }
        if (selectedProject == null) { mError.postValue("Vui lòng chọn một dự án (Project)."); return; }
        if (userId == -1) { mError.postValue("Không thể xác thực người dùng. Vui lòng đăng nhập lại."); return; }
        mIsLoading.postValue(true);
        // --- GIAI ĐOẠN 1: KIỂM TRA KHO TRƯỚC TIÊN ---
        checkUseCase.execute(currentProtocolId, new IInventoryRepository.GenericCallback() {
            @Override
            public void onSuccess() {
                // Kho ĐỦ HÀNG. Bắt đầu giai đoạn tiếp theo.
                createAndDeduct(title, objective, selectedProject, userId);
            }
            @Override
            public void onError(String errorMessage) {
                // Kho KHÔNG ĐỦ HÀNG hoặc có lỗi khi kiểm tra.
                mError.postValue(errorMessage);
                mIsLoading.postValue(false);
            }
        });
    }

    // SỬA: Dùng postValue và biến đã đổi tên
    private void createAndDeduct(String title, String objective, Project selectedProject, int userId) {
        // --- GIAI ĐOẠN 2: TẠO EXPERIMENT ---
        createUseCase.execute(
                title, objective, currentProtocolId, userId, selectedProject.getProjectId(),
                new IExperimentRepository.IdCallback() {
                    @Override
                    public void onSuccess(int newExperimentId) {
                        // Tạo thành công, bây giờ mới trừ kho.
                        // --- GIAI ĐOẠN 3: TRỪ KHO ---
                        deductUseCase.execute(currentProtocolId, userId, new IInventoryRepository.GenericCallback() {
                            @Override
                            public void onSuccess() {
                                // Toàn bộ quá trình hoàn tất thành công!
                                mCreationSuccess.postValue(true);
                                mIsLoading.postValue(false);
                            }
                            @Override
                            public void onError(String deductError) {
                                // Lỗi hiếm gặp (Race Condition).
                                mError.postValue("Lỗi nghiêm trọng khi trừ kho: " + deductError + ". Thí nghiệm đã được tạo, vui lòng kiểm tra lại kho.");
                                mIsLoading.postValue(false);
                            }
                        });
                    }
                    @Override
                    public void onError(String createError) {
                        // Lỗi khi tạo experiment, dừng lại.
                        mError.postValue("Lỗi khi tạo thí nghiệm: " + createError);
                        mIsLoading.postValue(false);
                    }
                }
        );
    }
}
