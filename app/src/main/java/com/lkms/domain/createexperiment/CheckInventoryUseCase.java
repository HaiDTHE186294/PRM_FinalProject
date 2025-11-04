package com.lkms.domain.createexperiment;

import com.lkms.data.model.java.ProtocolItem;
import com.lkms.data.repository.IInventoryRepository;
import com.lkms.data.repository.IProtocolRepository;
import java.util.List;

/**
 * UseCase này CHỈ KIỂM TRA xem kho có đủ vật tư cho một protocol hay không.
 * Nó KHÔNG thực hiện việc trừ kho.
 */
public class CheckInventoryUseCase {
    private final IInventoryRepository inventoryRepo;
    private final IProtocolRepository protocolRepo;

    public CheckInventoryUseCase(IInventoryRepository inventoryRepo, IProtocolRepository protocolRepo) {
        this.inventoryRepo = inventoryRepo;
        this.protocolRepo = protocolRepo;
    }

    public void execute(int protocolId, IInventoryRepository.GenericCallback callback) {
        // Lấy danh sách vật tư từ Protocol
        protocolRepo.getProtocolDetails(protocolId, new IProtocolRepository.ProtocolContentCallback() {
            @Override
            public void onItemsReceived(List<ProtocolItem> items) {
                // Gọi hàm mới, an toàn, chỉ để kiểm tra.
                inventoryRepo.checkStockAvailability(items, callback);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError("Không thể lấy danh sách vật tư để kiểm tra: " + errorMessage);
            }

            @Override public void onProtocolReceived(com.lkms.data.model.java.Protocol p) {// Không cần thiết cho UseCase này, bỏ qua.
            }
            @Override public void onStepsReceived(List<com.lkms.data.model.java.ProtocolStep> s) {// Không cần thiết cho UseCase này, bỏ qua.
            }
        });
    }
}
