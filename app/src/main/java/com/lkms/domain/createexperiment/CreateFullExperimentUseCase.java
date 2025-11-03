package com.lkms.domain.createexperiment;

// Import các lớp cần thiết
import com.lkms.data.model.java.ExperimentStep;
import com.lkms.data.model.java.Protocol;
import com.lkms.data.model.java.ProtocolItem;
import com.lkms.data.model.java.ProtocolStep;
import com.lkms.data.repository.IExperimentRepository;
import com.lkms.data.repository.IExperimentStepRepositoryVjet;
import com.lkms.data.repository.IProtocolRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * UseCase đơn giản hóa: Chỉ chịu trách nhiệm tạo bản ghi Experiment và các ExperimentStep tương ứng.
 * Logic về kiểm tra và trừ kho đã được tách ra khỏi UseCase này.
 */
public class CreateFullExperimentUseCase {

    private final IExperimentRepository experimentRepo;
    private final IProtocolRepository protocolRepo;
    // Chú ý: tên IExperimentStepRepositoryVjet của bạn hơi khác, tôi sẽ giữ nguyên
    private final IExperimentStepRepositoryVjet experimentStepRepo;
    // --- ĐÃ XÓA: IInventoryRepository inventoryRepo; ---

    /**
     * Constructor đã được đơn giản hóa, không còn nhận IInventoryRepository.
     */
    public CreateFullExperimentUseCase(IExperimentRepository er, IProtocolRepository pr, IExperimentStepRepositoryVjet esr) {
        this.experimentRepo = er;
        this.protocolRepo = pr;
        this.experimentStepRepo = esr;
    }

    /**
     * Hàm thực thi chính.
     * Khi thành công, callback sẽ trả về ID của Experiment mới được tạo.
     * @param finalCallback Kiểu dữ liệu là IdCallback để có thể trả về ID.
     */
    public void execute(String title, String objective, int protocolId, int userId, int projectId, IExperimentRepository.IdCallback finalCallback) {

        // --- BƯỚC 1: Chỉ cần lấy danh sách các bước (Steps) của Protocol ---
        protocolRepo.getProtocolDetails(protocolId, new IProtocolRepository.ProtocolContentCallback() {
            @Override
            public void onStepsReceived(List<ProtocolStep> steps) {
                // Khi có steps, bắt đầu quy trình tạo thí nghiệm
                createExperimentAndSteps(title, objective, userId, protocolId, projectId, steps, finalCallback);
            }

            @Override
            public void onError(String errorMessage) {
                finalCallback.onError("Không thể lấy chi tiết protocol: " + errorMessage);
            }

            // Các hàm không cần dùng đến trong UseCase này
            @Override
            public void onProtocolReceived(Protocol protocol) { /* Không dùng */ }

            @Override
            public void onItemsReceived(List<ProtocolItem> items) { /* Không dùng */ }
        });
    }

    /**
     * Hàm nội bộ để phối hợp việc tạo Experiment và sau đó là tạo các Step.
     */
    private void createExperimentAndSteps(String title, String objective, int userId, int protocolId, int projectId,
                                          List<ProtocolStep> steps, IExperimentRepository.IdCallback finalCallback) {
        // --- BƯỚC 2: Tạo bản ghi Experiment chính ---
        experimentRepo.createNewExperiment(title, objective, userId, protocolId, projectId, new IExperimentRepository.IdCallback() {
            @Override
            public void onSuccess(int newExperimentId) {
                // --- BƯỚC 3: Nếu tạo Experiment thành công, tiếp tục tạo các Step ---
                createExperimentSteps(newExperimentId, steps, new IExperimentRepository.GenericCallback() {
                    @Override
                    public void onSuccess() {
                        // Khi tạo Step cũng thành công, toàn bộ quá trình hoàn tất.
                        // Báo thành công và trả về ID của Experiment vừa tạo.
                        finalCallback.onSuccess(newExperimentId);
                    }

                    @Override
                    public void onError(String stepErrorMessage) {
                        // Nếu tạo Step lỗi, toàn bộ quá trình vẫn bị coi là thất bại.
                        finalCallback.onError("Tạo thí nghiệm thành công, nhưng lỗi khi tạo các bước: " + stepErrorMessage);
                    }
                });
            }

            @Override
            public void onError(String experimentErrorMessage) {
                // Nếu tạo Experiment chính đã thất bại, báo lỗi và dừng lại.
                finalCallback.onError("Lỗi khi tạo bản ghi thí nghiệm: " + experimentErrorMessage);
            }
        });
    }

    /**
     * Hàm tạo các bản ghi ExperimentStep từ danh sách ProtocolStep.
     */
    private void createExperimentSteps(int newExperimentId, List<ProtocolStep> steps, IExperimentRepository.GenericCallback cb) {
        if (steps == null || steps.isEmpty()) {
            cb.onSuccess(); // Nếu protocol không có bước nào, coi như thành công
            return;
        }

        List<ExperimentStep> newSteps = new ArrayList<>();
        for (ProtocolStep p : steps) {
            newSteps.add(new ExperimentStep(null, newExperimentId, p.getProtocolStepId(), "PENDING", null));
        }
        // Sử dụng repo của bạn
        experimentStepRepo.createExperimentSteps(newSteps, cb);
    }
}
