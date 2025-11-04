package com.lkms.domain.protocolusecase; // Khai báo file này thuộc package domain

import com.lkms.data.repository.IProtocolRepository;


/**
 * Lớp này đại diện cho một chức năng cụ thể: "Tìm kiếm protocol theo tiêu đề".
 */
public class SearchProtocolsUseCase {

    private final IProtocolRepository repository;

    /**
     * Hàm khởi tạo (Constructor).
     * @param repository Một đối tượng triển khai (implement) IProtocolRepository.
     */
    public SearchProtocolsUseCase(IProtocolRepository repository) {
        this.repository = repository;
    }

    /**
     * Hàm chính để thực thi chức năng tìm kiếm.
     * @param query Từ khóa tìm kiếm.
     * @param callback Callback để nhận kết quả trả về.
     */
    public void execute(String query, IProtocolRepository.ProtocolListCallback callback) {
        // Gọi hàm tương ứng từ repository
        repository.searchProtocolsByTitle(query, callback);
    }
}
