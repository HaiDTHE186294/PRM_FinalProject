package com.lkms.domain.experimentdetail; // Giả định package của bạn

// Import các model và interface cần thiết
import com.lkms.data.model.java.LogEntry;
import com.lkms.data.repository.IExperimentRepository;
import java.util.List;

/**
 * Use Case (Lớp nghiệp vụ) chịu trách nhiệm lấy Log Entries cho một Step cụ thể.
 * Lớp này kết nối ViewModel (UI Layer) với IExperimentRepository (Data Layer).
 */
public class GetLogEntryUseCase {

    private final IExperimentRepository experimentRepository;

    public GetLogEntryUseCase(IExperimentRepository experimentRepository) {
        this.experimentRepository = experimentRepository;
    }

    public interface GetLogEntryCallback {
        void onSuccess(List<LogEntry> logs);
        void onError(String error);
    }

    public void execute(int experimentStepId, final GetLogEntryCallback callback) {

        // Gọi phương thức của Repository (Data Layer)
        // Chúng ta tạo một Callback mới của Repository để lắng nghe kết quả từ Data Layer
        experimentRepository.getExperimentLogEntries(experimentStepId, new IExperimentRepository.LogEntryListCallback() {

            // Đây là lúc nhận được kết quả từ Repository

            @Override
            public void onSuccess(List<LogEntry> logs) {
                // [Nơi xử lý logic nghiệp vụ - Tùy chọn]
                // Ví dụ: Lọc, sắp xếp, hoặc biến đổi 'logs' trước khi gửi về UI.
                // Hiện tại, chúng ta chỉ chuyển tiếp dữ liệu.

                // Trả kết quả về cho ViewModel qua Callback của UseCase
                callback.onSuccess(logs);
            }

            @Override
            public void onError(String error) {
                // Trả lỗi về cho ViewModel qua Callback của UseCase
                callback.onError(error);
            }
        });
    }
}