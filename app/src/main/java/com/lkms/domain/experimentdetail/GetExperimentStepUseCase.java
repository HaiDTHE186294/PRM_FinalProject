package com.lkms.domain.experimentdetail;

import com.lkms.data.model.java.ExperimentStep;
import com.lkms.data.repository.IExperimentRepository;

import java.util.List;

public class GetExperimentStepUseCase {

    private final IExperimentRepository experimentRepository;

    public GetExperimentStepUseCase(IExperimentRepository experimentRepository) {
        this.experimentRepository = experimentRepository;
    }

    public interface getExperimentStepsListCallback {
        void onSuccess(List<ExperimentStep> list);

        void onError(String error);
    }

    public void execute(int experimentId, final getExperimentStepsListCallback callback) {

        // Gọi phương thức của Repository (Data Layer)
        // Chúng ta tạo một Callback mới của Repository để lắng nghe kết quả từ Data Layer
        experimentRepository.getExperimentStepsList(experimentId, new IExperimentRepository.ExperimentStepListCallback() {

            // Đây là lúc nhận được kết quả từ Repository

            @Override
            public void onSuccess(List<ExperimentStep> list) {
                // [Nơi xử lý logic nghiệp vụ - Tùy chọn]

                callback.onSuccess(list);
            }

            @Override
            public void onError(String error) {
                // Trả lỗi về cho ViewModel qua Callback của UseCase
                callback.onError(error);
            }
        });
    }
}