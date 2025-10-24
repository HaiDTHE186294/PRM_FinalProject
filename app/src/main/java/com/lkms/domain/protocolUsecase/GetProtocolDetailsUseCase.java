package com.lkms.domain.protocolUsecase;

import com.lkms.data.repository.IProtocolRepository;

/**
 * UseCase cho chức năng: "Lấy toàn bộ nội dung chi tiết của một protocol".
 */
public class GetProtocolDetailsUseCase {
    private final IProtocolRepository repository;

    public GetProtocolDetailsUseCase(IProtocolRepository repository) {
        this.repository = repository;
    }

    public void execute(int protocolId, IProtocolRepository.ProtocolContentCallback callback) {
        repository.getProtocolDetails(protocolId, callback);
    }
}
