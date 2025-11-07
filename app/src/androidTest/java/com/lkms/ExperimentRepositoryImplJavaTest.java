package com.lkms;

import androidx.test.platform.app.InstrumentationRegistry;

import com.lkms.data.model.java.*;
import com.lkms.data.model.java.combine.ExperimentReportData;
import com.lkms.data.repository.IExperimentRepository;
import com.lkms.data.repository.implement.java.ExperimentRepositoryImplJava;

import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class ExperimentRepositoryImplJavaTest {

    private final ExperimentRepositoryImplJava repo = new ExperimentRepositoryImplJava();

    // ✅ 1. Test tạo mới Experiment
    @Test
    public void testCreateNewExperiment() throws InterruptedException {
        repo.createNewExperiment(
                "Mock Test",
                "Mock Test",
                1,
                1,
                1,
                new IExperimentRepository.IdCallback() {
                    @Override
                    public void onSuccess(int id) {
                        System.out.println("✅ Created Experiment ID: " + id);
                    }

                    @Override
                    public void onError(String error) {
                        System.out.println("❌ Error: " + error);
                    }
                }
        );
        Thread.sleep(3000);
    }

    // ✅ 2. Test lấy danh sách Experiment đang thực hiện (INPROCESS)
    @Test
    public void testGetOngoingExperiments() throws InterruptedException {
        repo.getOngoingExperiments(
                1,
                new IExperimentRepository.ExperimentListCallback() {
                    @Override
                    public void onSuccess(List<Experiment> experimentList) {
                        System.out.println("✅ Ongoing Experiments: " + experimentList.size());
                        for (Experiment e : experimentList) {
                            System.out.println(" - " + e.getExperimentTitle() + " (" + e.getExperimentStatus() + ")");
                        }
                    }

                    @Override
                    public void onError(String error) {
                        System.out.println("❌ Error: " + error);
                    }
                }
        );
        Thread.sleep(3000);
    }

    // ✅ 3. Test lấy danh sách Step theo Experiment
    @Test
    public void testGetExperimentStepsList() throws InterruptedException {
        int experimentId = 1;
        repo.getExperimentStepsList(
                experimentId,
                new IExperimentRepository.ExperimentStepListCallback() {
                    @Override
                    public void onSuccess(List<ExperimentStep> stepList) {
                        System.out.println("✅ Step count: " + stepList.size());
                        for (ExperimentStep s : stepList) {
                            System.out.println(" - Step ID " + s.getExperimentStepId() +
                                    ", ProtocolStep: " + s.getProtocolStepId() +
                                    ", Status: " + s.getStepStatus());
                        }
                    }

                    @Override
                    public void onError(String error) {
                        System.out.println("❌ Error: " + error);
                    }
                }
        );
        Thread.sleep(3000);
    }

    // ✅ 4. Test lấy danh sách LogEntry theo Step
    @Test
    public void testGetExperimentLogEntries() throws InterruptedException {
        int stepId = 1;
        repo.getExperimentLogEntries(
                stepId,
                new IExperimentRepository.LogEntryListCallback() {
                    @Override
                    public void onSuccess(List<LogEntry> logList) {
                        System.out.println("✅ Log entries: " + logList.size());
                        for (LogEntry l : logList) {
                            System.out.println(" - LogID: " + l.getLogId() +
                                    " | Type: " + l.getLogType() +
                                    " | Content: " + l.getContent());
                        }
                    }

                    @Override
                    public void onError(String error) {
                        System.out.println("❌ Error: " + error);
                    }
                }
        );
        Thread.sleep(3000);
    }

    // ✅ 5. Test thêm text note (log type = Text)
    @Test
    public void testAddTextNote() throws InterruptedException {
        repo.addTextNote(
                1,
                1,
                "Checked sample A, stable at room temperature.",
                new IExperimentRepository.IdCallback() {
                    @Override
                    public void onSuccess(int id) {
                        System.out.println("✅ Text note added with Log ID: " + id);
                    }

                    @Override
                    public void onError(String error) {
                        System.out.println("❌ Error: " + error);
                    }
                }
        );
        Thread.sleep(3000);
    }

    // ✅ 6. Test upload file + thêm file entry vào log
    @Test
    public void testAddFileEntry() throws Exception {
        var context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // ✅ 1. Tạo file mẫu trong thư mục hợp lệ
        File cacheFile = new File(context.getFilesDir(), "samplejava.txt");
        if (cacheFile.exists()) cacheFile.delete();
        try (FileWriter writer = new FileWriter(cacheFile)) {
            writer.write("This is a sample log file for upload test.");
        }

        // ✅ 2. Kiểm tra file tồn tại thật sự
        System.out.println("File path: " + cacheFile.getAbsolutePath() +
                ", exists = " + cacheFile.exists() +
                ", size = " + cacheFile.length() + " bytes");

        // Bước 1: Upload file
        repo.uploadFileToStorage(
                cacheFile,
                new IExperimentRepository.StringCallback() {
                    @Override
                    public void onSuccess(String result) {
                        System.out.println("✅ Uploaded file URL: " + result);

                        // Bước 2: Lưu log entry vào DB
                        repo.addFileEntry(
                                1,
                                1,
                                "TXT",
                                "Uploaded supporting document.",
                                result,
                                new IExperimentRepository.IdCallback() {
                                    @Override
                                    public void onSuccess(int id) {
                                        System.out.println("✅ File log created with ID: " + id);
                                    }

                                    @Override
                                    public void onError(String error) {
                                        System.out.println("❌ Error saving log entry: " + error);
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(String error) {
                        System.out.println("❌ Error uploading file: " + error);
                    }
                }
        );

        Thread.sleep(5000);
    }

    @Test
    public void testGetExperimentReportData() throws InterruptedException {
        // 1. Dùng testId = 1 như ngài yêu cầu
        int testId = 1;

        // 2. Gọi hàm repo.getExperimentReportData
        repo.getExperimentReportData(
                testId,
                // 3. Implement callback (Giả sử tên là IExperimentRepository.ExperimentReportDataCallback)
                new IExperimentRepository.ExperimentReportDataCallback() {

                    @Override
                    public void onSuccess(ExperimentReportData data) {
                        System.out.println("✅ Experiment Report Data Received for ID: " + testId);

                        // 4. In toàn bộ dữ liệu ra console
                        // (Lombok @Data sẽ tự động tạo hàm toString() để in ra toàn bộ)
                        System.out.println(data);
                    }

                    @Override
                    public void onError(String error) {
                        System.out.println("❌ Error fetching report: " + error);
                    }
                }
        );

        // 5. Chờ 3 giây (giống hệt hàm mẫu) để thread async có thời gian hoàn thành
        Thread.sleep(3000);
    }

    // ✅ 8. Test lấy danh sách Experiment đang thực hiện theo danh sách ID
    @Test
    public void testGetOngoingExperimentsByIds() throws InterruptedException {
        // Danh sách ID bạn cung cấp
        List<Integer> experimentIds = List.of(1, 49, 50, 51, 52, 53, 54, 55, 63, 2, 3);

        System.out.println("🔍 Testing getOngoingExperimentsByIds() with IDs: " + experimentIds);

        repo.getOngoingExperimentsByIds(
                experimentIds,
                new IExperimentRepository.ExperimentListCallback() {
                    @Override
                    public void onSuccess(List<Experiment> experiments) {
                        System.out.println("✅ Returned " + experiments.size() + " ongoing experiments.");
                        for (Experiment e : experiments) {
                            System.out.println(" - ID: " + e.getExperimentId()
                                    + " | Title: " + e.getExperimentTitle()
                                    + " | Status: " + e.getExperimentStatus());
                        }
                    }

                    @Override
                    public void onError(String error) {
                        System.out.println("❌ Error fetching experiments: " + error);
                    }
                }
        );

        // Chờ thread async chạy xong
        Thread.sleep(4000);
    }
}
