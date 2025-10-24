package com.lkms.domain.protocolUsecase;

import com.lkms.data.model.java.Protocol;
import com.lkms.data.model.java.ProtocolItem;
import com.lkms.data.model.java.ProtocolStep;
import com.lkms.data.repository.IProtocolRepository;
import java.util.List;

/**
 * UseCase cho chức năng: "Tạo một protocol mới cùng với các bước và vật tư".
 */
public class CreateNewProtocolUseCase {
    private final IProtocolRepository repository;

    public CreateNewProtocolUseCase(IProtocolRepository repository) {
        this.repository = repository;
    }

    public void execute(Protocol protocolData, List<ProtocolStep> steps, List<ProtocolItem> items, int creatorUserId, IProtocolRepository.ProtocolIdCallback callback) {
        repository.createNewProtocol(protocolData, steps, items, creatorUserId, callback);
    }
}
