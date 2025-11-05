package com.lkms.data.repository;

import com.lkms.data.model.java.*;
import com.lkms.data.model.java.combine.ExperimentReportData;

import java.io.File;
import java.util.List;

/**
 * Giao diện Repository cho việc Quản lý Thí nghiệm và Nhật ký (Logbook).
 * Hỗ trợ các chức năng UC5, UC6, UC12, UC13, UC15.
 */
public interface IExperimentRepository {

    // --- Các giao diện Callback ---

    interface IdCallback {
        void onSuccess(int id); // Trả về id mới
        void onError(String errorMessage);
    }

    interface ExperimentListCallback {
        void onSuccess(List<Experiment> experiments);
        void onError(String errorMessage);
    }

    interface ExperimentCallback {
        void onSuccess(Experiment experiment);
        void onError(String errorMessage);
    }

    interface ExperimentStepListCallback {
        void onSuccess(List<ExperimentStep> experimentStep);
        void onError(String errorMessage);
    }

    interface LogEntryCallback {
        void onSuccess(LogEntry entries);
        void onError(String errorMessage);
    }


    interface LogEntryListCallback {
        void onSuccess(List<LogEntry> entries);
        void onError(String errorMessage);
    }

    interface CommentCallback {
        void onSuccess(Comment comment);

        void onError(String errorMessage);
    }

    interface CommentListCallback {
        void onSuccess(List<Comment> comments);
        void onError(String errorMessage);
    }

    interface StringCallback {
        void onSuccess(String url); // Dùng cho URL/Link
        void onError(String errorMessage);
    }

    interface GenericCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    interface IdListCallback {
        void onSuccess(List<Integer> experimentIds);
        void onError(String errorMessage);
    }

    interface ProjectCallBack {
        void onSuccess(Project project);
        void onError(String errorMessage);
    }

    interface FileCallBack {
        void onSuccess(File file);
        void onError(String errorMessage);
    }

    interface ExperimentReportDataCallback {
        void onSuccess(ExperimentReportData data);
        void onError(String errorMessage);
    }



    // --- Chức năng Quản lý Thí nghiệm (UC5, UC34) ---

    /**
     * UC5: Tạo hồ sơ Thí nghiệm mới.
     * Chèn dữ liệu vào bảng "Experiment" [1, 2].
     * @param userId Người tạo thí nghiệm [4].
     * @param protocolId Protocol được sử dụng [4].
     * @param projectId Dự án liên quan [4].
     */
    void createNewExperiment(
            String title,
            String objective,
            int userId,
            int protocolId,
            int projectId,
            IdCallback callback
    );

    /**
     * UC2 (Dashboard): Lấy danh sách các thí nghiệm đang tiến hành của người dùng.
     * Truy vấn bảng "Experiment" dựa trên "userId" và "experimentStatus".
     */
    void getOngoingExperiments(int userId, ExperimentListCallback callback);

    // --- Chức năng Ghi Nhật ký (Logbook) (UC6) ---


    /**
     * UC6: Lấy tất cả các bước cho một thí nghiệm đang diễn ra (Ánh xạ đến các bước của giao thức).
     * Truy vấn bảng "Experiment Step" [3].
     */
    void getExperimentStepsList(int experimentId, ExperimentStepListCallback callback);
    

    /**
     * UC6: Lấy tất cả các mục nhập nhật ký cho một thí nghiệm đang diễn ra.
     * Truy vấn bảng "LogEntry" [3].
     */
    void getExperimentLogEntries(int experimentStepId, LogEntryListCallback callback);

    /**
     * UC6: Thêm mục nhập ghi chú văn bản vào nhật ký.
     * Chèn dữ liệu vào bảng "LogEntry" [3].
     * Giả định cần biết experimentStepId để ghi log [5].
     */
    void addTextNote(int experimentStepId, int userId, String content, IdCallback callback);

    // --- Chức năng Tải lên Dữ liệu (UC12) ---

    /**
     * UC12: Thêm mục nhập Tệp/Hình ảnh vào nhật ký.
     * Ghi lại URL của tệp đã tải lên Cloud Storage vào trường "url" trong bảng "LogEntry" [3].
     */
    void addFileEntry(
            int experimentStepId,
            int userId,
            String logType, // Ví dụ: "Image", "Data"
            String content, // Tên tệp hoặc ghi chú
            String fileUrl,
            IdCallback callback
    );

    void uploadFileToStorage(
            File file,
            StringCallback callback
    );

    // --- Chức năng Báo cáo (UC15) ---

    /**
     * UC15: Yêu cầu server tạo báo cáo PDF cho một thí nghiệm đã hoàn thành.
     * Trả về link tải xuống (String url) [6, 7].
     */
    void requestExperimentReport(int experimentId, StringCallback callback);

    // --- Chức năng Cộng tác (UC13) ---

    /**
     * UC13: Đăng bình luận vào thí nghiệm.
     * Chèn vào bảng "Comment" [3].
     */
    void postComment(int experimentId, int userId, String commentText, GenericCallback callback);

    /**
     * UC13: Lấy danh sách bình luận cho một thí nghiệm.
     * Truy vấn bảng "Comment" [3].
     */
    void getCommentsForExperiment(int experimentId, CommentListCallback callback);


    void getExperimentIdsByUserId(int userId, IdListCallback callback);

    void getOngoingExperimentsByIds(List<Integer> experimentIds, ExperimentListCallback callback);

    void getExperimentById(int experimentId, ExperimentCallback callback);

    void getExperimentProject(int projectId, ProjectCallBack callback);

    void getLogEntryById(int logEntryId, LogEntryCallback callback);

    void getFile(String url, FileCallBack callback);

    void completeExperiment(int experimentId, GenericCallback callback);

    void getExperimentReportData(int experimentId, ExperimentReportDataCallback callback);

}
