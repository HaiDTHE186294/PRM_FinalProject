package com.lkms.domain.logentry;

// Giả định import cho các thành phần
import com.lkms.data.repository.IExperimentRepository;
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums;
import java.io.File;

/**
 * Use Case (Lớp nghiệp vụ) chịu trách nhiệm thêm một LogEntry mới.
 *
 * Tiến trình:
 * 1. Quyết định LogType (từ LMKSConstantEnums) dựa trên File.
 * 2. Nếu LogType là TEXT (vì File == null):
 * a. Gọi repository.addTextNote().
 * 3. Nếu LogType là loại file (IMAGE, PDF, v.v.):
 * a. Gọi repository.uploadFileToStorage().
 * b. Khi thành công, lấy fileUrl.
 * c. Gọi repository.addFileEntry() với fileUrl và LogType (dưới dạng String).
 * 4. Trả về ID của log mới (hoặc lỗi) qua callback.
 */
public class AddLogUseCase {

    private final IExperimentRepository experimentRepository;

    /**
     * Callback interface để trả kết quả về cho UI Layer (ViewModel).
     */
    public interface AddLogCallback {
        /**
         * Được gọi khi log entry được thêm thành công.
         *
         * @param newLogEntryId ID của log entry vừa được tạo.
         */
        void onSuccess(int newLogEntryId);

        /**
         * Được gọi nếu có lỗi xảy ra trong quá trình thêm.
         *
         * @param error Thông báo lỗi.
         */
        void onError(String error);
    }

    public AddLogUseCase(IExperimentRepository experimentRepository) {
        this.experimentRepository = experimentRepository;
    }

    /**
     * Thực thi Use Case.
     *
     * @param experimentStepId ID của bước thí nghiệm mà log này thuộc về.
     * @param userId           ID của người dùng tạo log.
     * @param content          Nội dung text (nếu là note) hoặc tên/mô tả file (nếu là file).
     * @param file             Đối tượng File cần upload (có thể là null nếu chỉ là text note).
     * @param callback         Callback để nhận kết quả.
     */
    public void execute(int experimentStepId, int userId, String content, File file, final AddLogCallback callback) {

        // Bước 1: Quyết định LogType
        final LKMSConstantEnums.LogType logType = determineLogTypeFromFile(file);

        if (logType == LKMSConstantEnums.LogType.TEXT) {
            // --- Trường hợp 1: Thêm Ghi chú (TEXT) ---
            addTextNoteInternal(experimentStepId, userId, content, callback);
        } else {
            // --- Trường hợp 2: Thêm File (IMAGE, PDF, v.v.) ---
            // Cần upload file trước, sau đó mới thêm entry
            // Chuyển enum thành String theo yêu cầu của repository
            final String logTypeString = logType.name();
            uploadAndAddFileEntry(experimentStepId, userId, content, file, logTypeString, callback);
        }
    }

    /**
     * Hàm helper để xác định LogType từ đối tượng File.
     * @param file File (có thể là null).
     * @return LMKSConstantEnums.LogType tương ứng.
     */
    private LKMSConstantEnums.LogType determineLogTypeFromFile(File file) {
        if (file == null) {
            return LKMSConstantEnums.LogType.TEXT;
        }

        String fileName = file.getName().toLowerCase();

        // Giả định tên các enum trong LMKSConstantEnums.LogType
        if (fileName.endsWith(".txt")) {
            return LKMSConstantEnums.LogType.TXT;
        }
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") || fileName.endsWith(".gif")) {
            return LKMSConstantEnums.LogType.IMAGE;
        }
        if (fileName.endsWith(".pdf")) {
            return LKMSConstantEnums.LogType.PDF;
        }
        if (fileName.endsWith(".html") || fileName.endsWith(".htm") || fileName.endsWith(".css") || fileName.endsWith(".js")) {
            return LKMSConstantEnums.LogType.WEBFILE;
        }

        // Giá trị mặc định nếu không khớp
        return LKMSConstantEnums.LogType.OTHER;
    }

    /**
     * Hàm helper xử lý logic cho Trường hợp 1 (Thêm text note).
     */
    private void addTextNoteInternal(int experimentStepId, int userId, String content, final AddLogCallback callback) {

        // Gọi hàm repository đã có
        experimentRepository.addTextNote(experimentStepId, userId, content, new IExperimentRepository.IdCallback() {
            @Override
            public void onSuccess(int newId) {
                callback.onSuccess(newId);
            }

            @Override
            public void onError(String error) {
                callback.onError("Failed to add text note: " + error);
            }
        });
    }

    /**
     * Hàm helper xử lý logic cho Trường hợp 2 (Upload file và thêm file entry).
     */
    private void uploadAndAddFileEntry(int experimentStepId, int userId, String content, File file, String logTypeString, final AddLogCallback callback) {

        // --- Bước 2a: Upload file lên Storage ---
        experimentRepository.uploadFileToStorage(file, new IExperimentRepository.StringCallback() {

            @Override
            public void onSuccess(String fileUrl) {
                // Upload thành công, đã có fileUrl.
                // --- Bước 2b: Thêm file entry vào database ---
                addFileEntryInternal(experimentStepId, userId, content, fileUrl, logTypeString, callback);
            }

            @Override
            public void onError(String error) {
                // Thất bại ngay từ bước upload
                callback.onError("Failed to upload file: " + error);
            }
        });
    }

    /**
     * Hàm helper cho Bước 2b (Thêm file entry vào DB sau khi đã có URL).
     */
    private void addFileEntryInternal(int experimentStepId, int userId, String content, String fileUrl, String logType, final AddLogCallback callback) {

        // Gọi hàm repository đã có
        experimentRepository.addFileEntry(experimentStepId, userId, logType, content, fileUrl, new IExperimentRepository.IdCallback() {

            @Override
            public void onSuccess(int newId) {
                callback.onSuccess(newId);
            }

            @Override
            public void onError(String error) {
                // File đã upload nhưng không tạo được entry
                callback.onError("File uploaded, but failed to create database entry: " + error);
            }
        });
    }
}