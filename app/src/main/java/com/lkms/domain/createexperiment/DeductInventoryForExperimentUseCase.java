package com.lkms.domain.createexperiment;

import com.lkms.data.model.java.ProtocolItem;
import com.lkms.data.repository.IInventoryRepository;
import com.lkms.data.repository.IProtocolRepository;

import java.util.List;

/**
 * UseCase này chịu trách nhiệm trừ kho vật tư
 * SAU KHI một Experiment đã được tạo thành công.
 * Nó tách biệt logic trừ kho ra khỏi logic tạo thí nghiệm.
 */
public class DeductInventoryForExperimentUseCase {
    private final IInventoryRepository inventoryRepo;
    private final IProtocolRepository protocolRepo;

    public DeductInventoryForExperimentUseCase(IInventoryRepository inventoryRepo, IProtocolRepository protocolRepo) {
        this.inventoryRepo = inventoryRepo;
        this.protocolRepo = protocolRepo;
    }

    /**
     * Thực thi việc trừ kho cho một thí nghiệm dựa trên Protocol ID của nó.
     * @param protocolId ID của protocol được dùng trong thí nghiệm.
     * @param callback Báo cáo kết quả thành công hay thất bại của việc trừ kho.
     */
    public void execute(int protocolId, IInventoryRepository.GenericCallback callback) {
        // Bước 1: Lấy danh sách vật tư (items) cần dùng từ Protocol ID
        protocolRepo.getProtocolDetails(protocolId, new IProtocolRepository.ProtocolContentCallback() {
            @Override
            public void onItemsReceived(List<ProtocolItem> items) {
                //Gọi hàm mới, an toàn, chỉ để trừ kho.
                inventoryRepo.deductStock(items, callback);
            }

            @Override
            public void onError(String errorMessage) {
                // Nếu không lấy được danh sách vật tư thì không thể trừ kho
                callback.onError("Không thể lấy danh sách vật tư để trừ kho: " + errorMessage);
            }

            // Các hàm callback này không cần thiết cho nhiệm vụ này
            @Override
            public void onProtocolReceived(com.lkms.data.model.java.Protocol protocol) {
            }

            @Override
            public void onStepsReceived(List<com.lkms.data.model.java.ProtocolStep> steps) {
            }
        });
    }
}
