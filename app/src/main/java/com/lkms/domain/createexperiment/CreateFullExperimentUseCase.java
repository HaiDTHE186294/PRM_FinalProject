package com.lkms.domain.createexperiment;

// Thêm các import cần thiết
import com.lkms.data.model.java.Protocol;
import com.lkms.data.model.java.ProtocolItem;
import com.lkms.data.model.java.ProtocolStep;
import com.lkms.data.model.java.ExperimentStep;
import com.lkms.data.repository.IExperimentRepository;
import com.lkms.data.repository.IExperimentStepRepository;
import com.lkms.data.repository.IInventoryRepository;
import com.lkms.data.repository.IProtocolRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * UseCase chịu trách nhiệm điều phối toàn bộ quy trình tạo một thí nghiệm mới.
 * Đây là một "giao dịch" phức tạp bao gồm:
 * 1. Lấy chi tiết Protocol (cả steps và items).
 * 2. Kiểm tra và trừ vật tư trong kho.
 * 3. Tạo bản ghi Experiment.
 * 4. Tạo các ExperimentStep tương ứng.
 * 5. Xử lý hoàn tác (rollback) nếu có lỗi xảy ra để đảm bảo toàn vẹn dữ liệu.
 */
public class CreateFullExperimentUseCase {

    private final IExperimentRepository experimentRepo;
    private final IProtocolRepository protocolRepo;
    private final IExperimentStepRepository experimentStepRepo;
    private final IInventoryRepository inventoryRepo; // <-- Repo quản lý kho

    /**
     * Constructor được "tiêm" đầy đủ các repository cần thiết.
     */
    public CreateFullExperimentUseCase(IExperimentRepository er, IProtocolRepository pr,
                                       IExperimentStepRepository esr, IInventoryRepository ir) {
        this.experimentRepo = er;
        this.protocolRepo = pr;
        this.experimentStepRepo = esr;
        this.inventoryRepo = ir; // Gán giá trị repo kho
    }

    /**
     * Hàm chính để thực thi UseCase.
     */
    public void execute(String title, String objective, int protocolId, int userId, int projectId, IExperimentRepository.GenericCallback finalCallback) {

        // --- BƯỚC 1: Lấy danh sách Step và Item từ Protocol ---
        protocolRepo.getProtocolDetails(protocolId, new IProtocolRepository.ProtocolContentCallback() {
            private List<ProtocolStep> cachedSteps;
            private List<ProtocolItem> cachedItems;
            private boolean stepsReceived = false;
            private boolean itemsReceived = false;

            @Override
            public void onStepsReceived(List<ProtocolStep> steps) {
                this.cachedSteps = steps;
                this.stepsReceived = true;
                if (itemsReceived) startMainProcess(); // Nếu item đã về thì bắt đầu
            }

            @Override
            public void onItemsReceived(List<ProtocolItem> items) {
                this.cachedItems = items;
                this.itemsReceived = true;
                if (stepsReceived) startMainProcess(); // Nếu step đã về thì bắt đầu
            }

            @Override
            public void onError(String errorMessage) {
                finalCallback.onError("Không thể lấy chi tiết protocol: " + errorMessage);
            }

            @Override
            public void onProtocolReceived(Protocol protocol) { /* Không cần ở đây */ }

            /**
             * Bắt đầu quy trình chính sau khi đã có đủ thông tin từ Protocol.
             */
            private void startMainProcess() {
                // --- BƯỚC 2: Kiểm tra và trừ kho ---
                // ✅ SỬA LỖI: Dùng đúng IInventoryRepository.GenericCallback
                inventoryRepo.checkAndDeductStock(cachedItems, new IInventoryRepository.GenericCallback() {
                    @Override
                    public void onSuccess() {
                        // Nếu kho đủ và đã trừ thành công, tiếp tục tạo Experiment
                        createExperimentAndSteps(title, objective, userId, protocolId, projectId, cachedSteps, cachedItems, finalCallback);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        // Nếu kho không đủ hoặc có lỗi khi trừ, dừng lại và báo lỗi ngay
                        finalCallback.onError(errorMessage);
                    }
                });
            }
        });
    }

    /**
     * Thực hiện tạo Experiment và các Steps tương ứng sau khi kho đã được xác nhận.
     */
    private void createExperimentAndSteps(String title, String objective, int userId, int protocolId, int projectId,
                                          List<ProtocolStep> steps, List<ProtocolItem> usedItems, IExperimentRepository.GenericCallback finalCallback) {
        // --- BƯỚC 3: Tạo bản ghi Experiment ---
        experimentRepo.createNewExperiment(title, objective, userId, protocolId, projectId, new IExperimentRepository.IdCallback() {
            @Override
            public void onSuccess(int newExperimentId) {
                // --- BƯỚC 4: Tạo Experiment Steps ---
                createExperimentSteps(newExperimentId, steps, new IExperimentRepository.GenericCallback() {
                    @Override
                    public void onSuccess() {
                        // Giao dịch hoàn tất!
                        finalCallback.onSuccess();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        // Nếu tạo Step thất bại, cũng phải hoàn trả vật tư
                        handleCreationError("Lỗi khi tạo các bước thí nghiệm: " + errorMessage, usedItems, finalCallback);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                // Nếu tạo Experiment thất bại, xử lý lỗi và hoàn trả vật tư
                handleCreationError("Lỗi khi tạo thí nghiệm: " + errorMessage, usedItems, finalCallback);
            }
        });
    }

    /**
     * Tạo các bản ghi ExperimentStep từ danh sách ProtocolStep.
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
        experimentStepRepo.createExperimentSteps(newSteps, cb);
    }

    /**
     * Hàm xử lý lỗi tập trung, thực hiện rollback (hoàn trả vật tư).
     */
    private void handleCreationError(String errorMessage, List<ProtocolItem> itemsToRestore, IExperimentRepository.GenericCallback finalCallback) {
        // --- BƯỚC 5: XỬ LÝ LỖI (ROLLBACK) ---
        finalCallback.onError(errorMessage + ". Đang hoàn trả vật tư vào kho...");

        // ✅ SỬA LỖI: Dùng đúng IInventoryRepository.GenericCallback
        inventoryRepo.restoreStock(itemsToRestore, new IInventoryRepository.GenericCallback() {
            @Override
            public void onSuccess() {
                // Đã hoàn trả thành công. Không cần làm gì thêm ở đây.
            }

            @Override
            public void onError(String restoreError) {
                // Đây là trường hợp lỗi nghiêm trọng nhất!
            }
        });
    }
}
