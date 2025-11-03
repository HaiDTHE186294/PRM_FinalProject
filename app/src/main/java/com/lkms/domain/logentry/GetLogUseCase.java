package com.lkms.domain.logentry;

// Import các model và interface cần thiết
import com.lkms.data.model.java.LogEntry; // Giả định import
import com.lkms.data.repository.IExperimentRepository; // Giả định import
import java.io.File;

/**
 * Use Case (Lớp nghiệp vụ) chịu trách nhiệm lấy một LogEntry
 * và file đính kèm (nếu có) của nó.
 *
 * Tiến trình:
 * 1. Lấy LogEntry bằng ID.
 * 2. Nếu LogEntry có file URL, tải file đó về.
 * 3. Trả về cả LogEntry và File (hoặc null) cho ViewModel.
 */
public class GetLogUseCase {

    private final IExperimentRepository experimentRepository;

    public GetLogUseCase(IExperimentRepository experimentRepository) {
        this.experimentRepository = experimentRepository;
    }

    /**
     * Callback interface để trả kết quả về cho UI Layer (ViewModel).
     */
    public interface GetLogCallback {
        /**
         * Được gọi khi lấy LogEntry thành công và file (nếu có) đã được xử lý.
         *
         * @param logEntry       Đối tượng LogEntry được tải.
         * @param downloadedFile Đối tượng File đã tải về. Có thể là null nếu
         * log entry không có file đính kèm.
         */
        void onSuccess(LogEntry logEntry, File downloadedFile);

        /**
         * Được gọi nếu có lỗi xảy ra trong quá trình lấy LogEntry hoặc tải file.
         *
         * @param error Thông báo lỗi.
         */
        void onError(String error);
    }

    /**
     * Thực thi Use Case.
     *
     * @param logEntryId ID của log entry cần lấy.
     * @param callback   Callback để nhận kết quả.
     */
    public void execute(int logEntryId, final GetLogCallback callback) {

        // --- Bước 1: Lấy LogEntry bằng ID ---
        experimentRepository.getLogEntryById(logEntryId, new IExperimentRepository.LogEntryCallback() {

            @Override
            public void onSuccess(LogEntry logEntry) {
                // Bước 1 thành công. Bây giờ kiểm tra file.
                // Giả định hàm getter là getFileUrl()
                String fileUrl = logEntry.getUrl();

                if (fileUrl == null || fileUrl.isEmpty()) {
                    // --- Trường hợp 1: Lấy log thành công, không có file đính kèm ---
                    // Trả về LogEntry và một file null.
                    callback.onSuccess(logEntry, null);
                } else {
                    // --- Trường hợp 2: Lấy log thành công, CÓ file đính kèm ---
                    // Phải thực hiện Bước 2: Tải file.
                    downloadAssociatedFile(logEntry, fileUrl, callback);
                }
            }

            @Override
            public void onError(String error) {
                // --- Thất bại (Ngay từ Bước 1) ---
                // Không thể lấy được LogEntry.
                callback.onError("Error fetching log entry: " + error);
            }
        });
    }

    /**
     * Hàm private helper để xử lý logic tải file (Bước 2).
     */
    private void downloadAssociatedFile(LogEntry logEntry, String fileUrl, final GetLogCallback callback) {

        // --- Bước 2: Tải file từ URL ---
        experimentRepository.getFile(fileUrl, new IExperimentRepository.FileCallBack() {

            @Override
            public void onSuccess(File downloadedFile) {
                // --- Thành công (Hoàn tất) ---
                // Cả LogEntry (từ bước 1) và File (từ bước 2) đều đã sẵn sàng.
                callback.onSuccess(logEntry, downloadedFile);
            }

            @Override
            public void onError(String error) {
                // --- Thất bại (Ở Bước 2) ---
                // Lấy được LogEntry, nhưng tải file bị lỗi.
                // Toàn bộ use case được coi là thất bại.
                callback.onError("Successfully fetched log, but failed to download file: " + error);
            }
        });
    }
}