package com.lkms.domain.report;

import com.lkms.data.repository.IExperimentRepository;
// Import model dữ liệu trả về
import com.lkms.data.model.java.combine.ExperimentReportData;

/**
 * Use case để lấy toàn bộ dữ liệu báo cáo cho một Experiment.
 */
public class GetExperimentReportUseCase {

    private final IExperimentRepository experimentRepository;

    // 1. Constructor: Chỉ cần IExperimentRepository
    public GetExperimentReportUseCase(IExperimentRepository experimentRepository) {
        this.experimentRepository = experimentRepository;
    }

    // 2. Callback riêng của UseCase
    //    (Dùng để báo cáo kết quả về cho ViewModel)
    public interface GetExperimentReportCallback {
        // Trả về đối tượng data khi thành công
        void onSuccess(ExperimentReportData data);
        void onError(String error);
    }

    // 3. Phương thức "execute"
    public void execute(int experimentId, final GetExperimentReportCallback callback) {

        // Gọi phương thức của repository và "dịch" callback của repository
        // sang callback của UseCase
        experimentRepository.getExperimentReportData(experimentId,
                new IExperimentRepository.ExperimentReportDataCallback() {

                    @Override
                    public void onSuccess(ExperimentReportData data) {
                        // Báo cáo thành công và gửi data cho ViewModel
                        callback.onSuccess(data);
                    }

                    @Override
                    public void onError(String error) {
                        // Báo cáo lỗi cho ViewModel
                        callback.onError(error);
                    }
                });
    }
}