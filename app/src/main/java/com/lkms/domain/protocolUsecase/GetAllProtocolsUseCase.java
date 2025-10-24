package com.lkms.domain.protocolUsecase;

import com.lkms.data.repository.IProtocolRepository;

/**
 * UseCase cho chức năng: "Lấy tất cả các protocol không phân biệt trạng thái".
 * Thường dùng cho vai trò quản trị viên.
 */
public class GetAllProtocolsUseCase {
    private final IProtocolRepository repository;

    public GetAllProtocolsUseCase(IProtocolRepository repository) {
        this.repository = repository;
    }

    public void execute(IProtocolRepository.ProtocolListCallback callback) {
        repository.getAllProtocols(callback);
    }
}
