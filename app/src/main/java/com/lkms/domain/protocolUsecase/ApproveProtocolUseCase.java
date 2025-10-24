package com.lkms.domain.protocolUsecase;

import com.lkms.data.repository.IProtocolRepository;
// ✅ BƯỚC 1: IMPORT ENUM VÀO USECASE
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums.ProtocolApproveStatus;

/**
 * UseCase cho chức năng: "Phê duyệt hoặc từ chối một protocol".
 * Thường dùng cho vai trò Lab Manager.
 * Đã được cập nhật để làm việc với Enum.
 */
public class ApproveProtocolUseCase {
    private final IProtocolRepository repository;

    public ApproveProtocolUseCase(IProtocolRepository repository) {
        this.repository = repository;
    }

    /**
     * ✅ BƯỚC 2: THAY ĐỔI CHỮ KÝ PHƯƠNG THỨC
     * Phương thức execute vẫn nhận vào boolean để giữ cho tầng trên (ViewModel) đơn giản.
     * UseCase sẽ chịu trách nhiệm chuyển đổi sang Enum.
     */
    public void execute(int protocolId, int approverUserId, boolean approved, String reason, IProtocolRepository.GenericCallback callback) {
        // ✅ BƯỚC 3: THÊM LOGIC CHUYỂN ĐỔI
        // Chuyển đổi giá trị boolean thành Enum tương ứng.
        ProtocolApproveStatus newStatus = approved ? ProtocolApproveStatus.APPROVED : ProtocolApproveStatus.REJECTED;

        // ✅ BƯỚC 4: GỌI REPOSITORY VỚI THAM SỐ ENUM
        // Bây giờ, tham số truyền vào đã khớp với chữ ký mới của hàm trong IProtocolRepository.
        repository.approveProtocol(protocolId, approverUserId, newStatus, reason, callback);
    }
}
