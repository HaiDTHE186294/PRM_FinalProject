package com.lkms.domain.report;

import com.lkms.data.repository.IExperimentRepository;

/**
 * Use case để hoàn thành một Experiment (chuyển status sang COMPLETED)
 * theo logic nghiệp vụ.
 */
public class CompleteExperimentUseCase {

    private final IExperimentRepository experimentRepository;

    // 1. Constructor: Chỉ cần IExperimentRepository
    public CompleteExperimentUseCase(IExperimentRepository experimentRepository) {
        this.experimentRepository = experimentRepository;
    }

    // 2. Callback riêng của UseCase
    //    (Giống như GenericCallback, dùng để báo cáo kết quả về cho ViewModel)
    public interface CompleteExperimentCallback {
        void onSuccess();
        void onError(String error);
    }

    // 3. Phương thức "execute"
    public void execute(int experimentId, final CompleteExperimentCallback callback) {

        // Gọi phương thức của repository và "dịch" callback của repository
        // sang callback của UseCase
        experimentRepository.completeExperiment(experimentId,
                new IExperimentRepository.GenericCallback() { // Giả sử GenericCallback nằm trong IExperimentRepository

                    @Override
                    public void onSuccess() {
                        // Báo cáo thành công cho người gọi UseCase (ViewModel)
                        callback.onSuccess();
                    }

                    @Override
                    public void onError(String error) {
                        // Báo cáo lỗi cho người gọi UseCase (ViewModel)
                        callback.onError(error);
                    }
                });
    }
}