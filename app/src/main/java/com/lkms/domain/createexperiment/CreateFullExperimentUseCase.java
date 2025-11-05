package com.lkms.domain.createexperiment;

// Import các lớp cần thiết
import com.lkms.data.model.java.ExperimentStep;
import com.lkms.data.model.java.Protocol;
import com.lkms.data.model.java.ProtocolItem;
import com.lkms.data.model.java.ProtocolStep;
import com.lkms.data.model.java.Team; // ⭐ THÊM IMPORT
import com.lkms.data.repository.IExperimentRepository;
import com.lkms.data.repository.IExperimentStepRepositoryVjet;
import com.lkms.data.repository.IProtocolRepository;
import com.lkms.data.repository.ITeamRepository; // ⭐ THÊM IMPORT
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums; // ⭐ THÊM IMPORT

import java.util.ArrayList;
import java.util.List;

/**
 * UseCase chịu trách nhiệm tạo bản ghi Experiment, các ExperimentStep,
 * và tự động thêm người tạo vào Team.
 */
public class CreateFullExperimentUseCase {

    private final IExperimentRepository experimentRepo;
    private final IProtocolRepository protocolRepo;
    private final IExperimentStepRepositoryVjet experimentStepRepo;
    private final ITeamRepository teamRepo; // ⭐ THÊM REPO MỚI

    /**
     * Constructor được cập nhật để nhận thêm ITeamRepository.
     */
    public CreateFullExperimentUseCase(IExperimentRepository er, IProtocolRepository pr, IExperimentStepRepositoryVjet esr, ITeamRepository tr) {
        this.experimentRepo = er;
        this.protocolRepo = pr;
        this.experimentStepRepo = esr;
        this.teamRepo = tr; // ⭐ THÊM REPO MỚI
    }

    /**
     * Hàm thực thi chính.
     * Khi thành công, callback sẽ trả về ID của Experiment mới được tạo.
     * @param finalCallback Kiểu dữ liệu là IdCallback để có thể trả về ID.
     */
    public void execute(String title, String objective, int protocolId, int userId, int projectId, IExperimentRepository.IdCallback finalCallback) {

        // --- BƯỚC 1: Lấy danh sách các bước (Steps) của Protocol ---
        protocolRepo.getProtocolDetails(protocolId, new IProtocolRepository.ProtocolContentCallback() {
            @Override
            public void onStepsReceived(List<ProtocolStep> steps) {
                // Khi có steps, bắt đầu quy trình tạo thí nghiệm
                createExperimentAndRelatedData(title, objective, userId, protocolId, projectId, steps, finalCallback);
            }

            @Override
            public void onError(String errorMessage) {
                finalCallback.onError("Không thể lấy chi tiết protocol: " + errorMessage);
            }

            // Các hàm không cần dùng đến trong UseCase này
            @Override public void onProtocolReceived(Protocol protocol) { /* Không dùng */ }
            @Override public void onItemsReceived(List<ProtocolItem> items) { /* Không dùng */ }
        });
    }

    /**
     * Hàm nội bộ để phối hợp việc tạo Experiment, thêm thành viên Team và sau đó là tạo các Step.
     */
    private void createExperimentAndRelatedData(String title, String objective, int userId, int protocolId, int projectId,
                                                List<ProtocolStep> steps, IExperimentRepository.IdCallback finalCallback) {
        // --- BƯỚC 2: Tạo bản ghi Experiment chính ---
        experimentRepo.createNewExperiment(title, objective, userId, protocolId, projectId, new IExperimentRepository.IdCallback() {
            @Override
            public void onSuccess(int newExperimentId) {
                // --- BƯỚC 3: Thêm người tạo vào bảng Team với status ACTIVE ---
                addCreatorToTeam(newExperimentId, userId, new ITeamRepository.TeamMemberCallback() {
                    @Override
                    public void onSuccess(Team teamMember) {
                        // --- BƯỚC 4: Nếu thêm team thành công, tiếp tục tạo các Step ---
                        createExperimentSteps(newExperimentId, steps, new IExperimentRepository.GenericCallback() {
                            @Override
                            public void onSuccess() {
                                // Khi tạo Step cũng thành công, toàn bộ quá trình hoàn tất.
                                finalCallback.onSuccess(newExperimentId);
                            }

                            @Override
                            public void onError(String stepErrorMessage) {
                                finalCallback.onError("Tạo thí nghiệm và team thành công, nhưng lỗi khi tạo các bước: " + stepErrorMessage);
                            }
                        });
                    }

                    @Override
                    public void onError(String teamErrorMessage) {
                        finalCallback.onError("Tạo thí nghiệm thành công, nhưng lỗi khi thêm người tạo vào team: " + teamErrorMessage);
                    }
                });
            }

            @Override
            public void onError(String experimentErrorMessage) {
                finalCallback.onError("Lỗi khi tạo bản ghi thí nghiệm: " + experimentErrorMessage);
            }
        });
    }

    /**
     * Hàm nội bộ để thêm người tạo vào bảng Team.
     */
    private void addCreatorToTeam(int experimentId, int creatorId, ITeamRepository.TeamMemberCallback callback) {
        Team creatorAsMember = new Team(
                experimentId,
                creatorId,
                LKMSConstantEnums.TeamStatus.ACTIVE.name() // Status là ACTIVE
        );
        teamRepo.addMember(creatorAsMember, callback);
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
        experimentStepRepo.createExperimentSteps(newSteps, cb);
    }
}
