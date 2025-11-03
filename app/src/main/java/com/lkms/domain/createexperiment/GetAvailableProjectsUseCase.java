package com.lkms.domain.createexperiment;

// Import interface của Repository và Callback tương ứng
import com.lkms.data.repository.IProjectRepository;

/**
 * UseCase cho chức năng: "Lấy danh sách tất cả các Project đang có".
 * Dữ liệu này sẽ được dùng để đổ vào Spinner cho người dùng chọn.
 */
public class GetAvailableProjectsUseCase {

    private final IProjectRepository repository;

    /**
     * Hàm khởi tạo (Constructor) nhận vào một IProjectRepository.
     * @param repository Một đối tượng triển khai IProjectRepository (ví dụ: ProjectRepositoryImplJava).
     */
    public GetAvailableProjectsUseCase(IProjectRepository repository) {
        this.repository = repository;
    }

    /**
     * Hàm chính để thực thi chức năng của UseCase này.
     * @param callback Callback để nhận kết quả trả về (thành công hoặc thất bại).
     */
    public void execute(IProjectRepository.ProjectListCallback callback) {
        // UseCase này chỉ làm một việc duy nhất là gọi hàm tương ứng từ repository.
        // Giả định rằng IProjectRepository có hàm getAllProjects().
        repository.getAllProjects(callback);
    }
}
