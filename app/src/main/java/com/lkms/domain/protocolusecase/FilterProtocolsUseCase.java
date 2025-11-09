package com.lkms.domain.protocolusecase;

import com.lkms.data.repository.IProtocolRepository;


/**
 * Lớp này đại diện cho một chức năng cụ thể: "Lọc danh sách protocol theo nhiều điều kiện".
 */
public class FilterProtocolsUseCase {

    private final IProtocolRepository repository;

    /**
     * Hàm khởi tạo (Constructor).
     * @param repository Một đối tượng triển khai (implement) IProtocolRepository.
     */
    public FilterProtocolsUseCase(IProtocolRepository repository) {
        this.repository = repository;
    }

    /**
     * Hàm chính để thực thi chức năng lọc.
     * @param creatorId ID của người tạo (có thể là null).
     * @param versionNumber Số phiên bản để lọc (có thể là null).
     * @param callback Callback để nhận kết quả trả về.
     */
    public void execute(Integer creatorId, String versionNumber, IProtocolRepository.ProtocolListCallback callback) {
        // Gọi hàm tương ứng từ repository
        repository.filterProtocols(creatorId, versionNumber, callback);
    }
}
