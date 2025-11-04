package com.lkms.domain.protocolusecase; // ✅ DÒNG 1: Khai báo package phải ở đầu tiên và đúng cú pháp.

import com.lkms.data.repository.IProtocolRepository;


/**
 * Lớp này đại diện cho một chức năng cụ thể: "Lấy danh sách các protocol đã được duyệt".
 */
public class GetLatestApprovedProtocolsUseCase {

    private final IProtocolRepository repository;

    /**
     * Hàm khởi tạo (Constructor) nhận vào một IProtocolRepository.
     * @param repository Một đối tượng triển khai (implement) IProtocolRepository (ví dụ: ProtocolRepositoryImplJava).
     */
    public GetLatestApprovedProtocolsUseCase(IProtocolRepository repository) {
        this.repository = repository;
    }

    /**
     * Hàm chính để thực thi chức năng của UseCase này.
     * @param callback Callback để nhận kết quả trả về (thành công hoặc thất bại).
     */
    public void execute(IProtocolRepository.ProtocolListCallback callback) {
        // UseCase này chỉ làm một việc duy nhất là gọi hàm tương ứng từ repository.
        repository.getLatestApprovedProtocols(callback);
    }
}
