package com.lkms.data.repository.implement.java;

import static com.lkms.BuildConfig.SUPABASE_URL;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lkms.data.model.java.Experiment;
import com.lkms.data.model.java.ExperimentStep;
import com.lkms.data.model.java.LogEntry;
import com.lkms.data.model.java.Project;
import com.lkms.data.model.java.combine.ExperimentReportData;
import com.lkms.data.repository.IExperimentRepository;
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ExperimentRepositoryImplJava implements IExperimentRepository {

    private static final Gson gson = new Gson();

    // -------------------- CREATE NEW EXPERIMENT --------------------
    @Override
    public void createNewExperiment(String title, String objective, int userId, int protocolId, int projectId, IdCallback callback) {
        new Thread(() -> {
            try {
                Experiment newExperiment = new Experiment(
                        null,
                        title != null ? title : "Untitled Experiment",
                        objective != null ? objective : "No objective provided",
                        LKMSConstantEnums.ExperimentStatus.ONGOING.toString(),
                        new Date().toString(), // startDate
                        null,                  // finishDate
                        userId,
                        protocolId,
                        projectId
                );

                String endpoint = SUPABASE_URL + "/rest/v1/Experiment?select=*";
                String jsonBody = gson.toJson(newExperiment);

                String response = HttpHelper.postJson(endpoint, jsonBody);

                Type listType = new TypeToken<List<Experiment>>() {}.getType();
                List<Experiment> created = gson.fromJson(response, listType);

                if (created != null && !created.isEmpty() && created.get(0).getExperimentId() != null) {
                    callback.onSuccess(created.get(0).getExperimentId());
                } else {
                    callback.onError("Không thể tạo experiment mới.");
                }

            } catch (Exception e) {
                callback.onError("Lỗi khi tạo experiment: " + e.getMessage());
            }
        }).start();
    }

    // -------------------- GET ONGOING EXPERIMENTS --------------------
    @Override
    public void getOngoingExperiments(int userId, ExperimentListCallback callback) {
        getExperimentIdsByUserId(userId, new IdListCallback() {
            @Override
            public void onSuccess(List<Integer> experimentIds) {
                if (experimentIds == null || experimentIds.isEmpty()) {
                    callback.onSuccess(new ArrayList<>());
                    return;
                }
                getOngoingExperimentsByIds(experimentIds, callback);
            }

            @Override
            public void onError(String message) {
                callback.onError("Lỗi khi lấy danh sách experiment ID: " + message);
            }
        });
    }
    // -------------------- GET EXPERIMENT STEPS --------------------
    @Override
    public void getExperimentStepsList(int experimentId, ExperimentStepListCallback callback) {
        new Thread(() -> {
            try {
                String endpoint = SUPABASE_URL + "/rest/v1/ExperimentStep?select=*"
                        + "&experimentId=eq." + experimentId;

                String json = HttpHelper.getJson(endpoint);

                Type listType = new TypeToken<List<ExperimentStep>>() {}.getType();
                List<ExperimentStep> steps = gson.fromJson(json, listType);

                Log.d("RepoDebug", "getExperimentSteps: Parse JSON xong. Gọi onSuccess.");
                callback.onSuccess(steps);
            } catch (Exception e) {
                callback.onError("Lỗi khi tải danh sách bước: " + e.getMessage());
            }
        }).start();
    }

    // -------------------- GET LOG ENTRIES --------------------
    @Override
    public void getExperimentLogEntries(int experimentStepId, LogEntryListCallback callback) {
        new Thread(() -> {
            try {
                String endpoint = SUPABASE_URL + "/rest/v1/LogEntry?select=*"
                        + "&experimentStepId=eq." + experimentStepId
                        + "&order=logTime.asc";

                String json = HttpHelper.getJson(endpoint);

                Type listType = new TypeToken<List<LogEntry>>() {}.getType();
                List<LogEntry> logEntries = gson.fromJson(json, listType);

                callback.onSuccess(logEntries);
            } catch (Exception e) {
                callback.onError("Lỗi khi tải log entries: " + e.getMessage());
            }
        }).start();
    }

    // -------------------- ADD TEXT NOTE --------------------
    @Override
    public void addTextNote(int experimentStepId, int userId, String content, IdCallback callback) {
        new Thread(() -> {
            try {
                LogEntry newLog = new LogEntry(
                        null,
                        experimentStepId,
                        "Text",
                        userId,
                        content != null ? content : "",
                        null,
                        new Date().toString()
                );

                String endpoint = SUPABASE_URL + "/rest/v1/LogEntry?select=*";
                String jsonBody = gson.toJson(newLog);
                String response = HttpHelper.postJson(endpoint, jsonBody);

                Type listType = new TypeToken<List<LogEntry>>() {}.getType();
                List<LogEntry> created = gson.fromJson(response, listType);

                if (created != null && !created.isEmpty() && created.get(0).getLogId() != null) {
                    callback.onSuccess(created.get(0).getLogId());
                } else {
                    callback.onError("Không thể tạo ghi chú text.");
                }

            } catch (Exception e) {
                callback.onError("Lỗi khi thêm ghi chú: " + e.getMessage());
            }
        }).start();
    }

    // -------------------- ADD FILE ENTRY --------------------
    @Override
    public void addFileEntry(int experimentStepId, int userId, String logType, String content, String fileUrl, IdCallback callback) {
        new Thread(() -> {
            try {
                LogEntry newLog = new LogEntry(
                        null,
                        experimentStepId,
                        logType != null ? logType : "File",
                        userId,
                        content != null ? content : "",
                        fileUrl,
                        new Date().toString()
                );

                String endpoint = SUPABASE_URL + "/rest/v1/LogEntry?select=*";
                String jsonBody = gson.toJson(newLog);
                String response = HttpHelper.postJson(endpoint, jsonBody);

                Type listType = new TypeToken<List<LogEntry>>() {}.getType();
                List<LogEntry> created = gson.fromJson(response, listType);

                if (created != null && !created.isEmpty() && created.get(0).getLogId() != null) {
                    callback.onSuccess(created.get(0).getLogId());
                } else {
                    callback.onError("Không thể tạo log file.");
                }

            } catch (Exception e) {
                callback.onError("Lỗi khi thêm file entry: " + e.getMessage());
            }
        }).start();
    }

    // -------------------- UPLOAD FILE --------------------
    @Override
    public void uploadFileToStorage(File file, StringCallback callback) {
        if (file == null) {
            callback.onError("File là null");
            return;
        }

        new Thread(() -> {
            try {
                String bucketName = "ExperimentLog";
                String path = System.currentTimeMillis() + "_" + file.getName();
                String publicUrl = HttpHelper.uploadFile(bucketName, path, file);
                callback.onSuccess(publicUrl);
            } catch (Exception e) {
                callback.onError("Lỗi upload file: " + e.getMessage());
            }
        }).start();
    }

    // -------------------- NOT IMPLEMENTED YET --------------------
    @Override
    public void requestExperimentReport(int experimentId, StringCallback callback) {
        callback.onError("Chưa được triển khai.");
    }

    @Override
    public void postComment(int experimentId, int userId, String commentText, GenericCallback callback) {
        callback.onError("Chưa được triển khai.");
    }

    @Override
    public void getCommentsForExperiment(int experimentId, CommentListCallback callback) {
        callback.onError("Chưa được triển khai.");
    }

    //Adding
    @Override
    public void getExperimentIdsByUserId(int userId, IdListCallback callback) {
        new Thread(() -> {
            try {
                String endpoint = SUPABASE_URL + "/rest/v1/Team?select=experimentId" + "&userId=eq." + userId;
                String json = HttpHelper.getJson(endpoint);

                Type listType = new TypeToken<List<Map<String, Integer>>>() {}.getType();
                List<Map<String, Integer>> result = gson.fromJson(json, listType);

                List<Integer> experimentIds = new ArrayList<>();
                for (Map<String, Integer> row : result) {
                    if (row.containsKey("experimentId")) {
                        experimentIds.add(row.get("experimentId"));
                    }
                }

                callback.onSuccess(experimentIds);
            } catch (Exception e) {
                callback.onError("Lỗi khi lấy danh sách experimentId: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void getOngoingExperimentsByIds(List<Integer> experimentIds, ExperimentListCallback callback) {
        new Thread(() -> {
            try {
                if (experimentIds == null || experimentIds.isEmpty()) {
                    callback.onSuccess(new ArrayList<>());
                    return;
                }

                String idList = experimentIds.toString().replace("[", "(").replace("]", ")");
                String endpoint = SUPABASE_URL + "/rest/v1/Experiment?select=*"
                        + "&experimentId=in." + idList
                        + "&experimentStatus=eq." + LKMSConstantEnums.ExperimentStatus.ONGOING;

                String json = HttpHelper.getJson(endpoint);

                Type listType = new TypeToken<List<Experiment>>() {}.getType();
                List<Experiment> experiments = gson.fromJson(json, listType);

                callback.onSuccess(experiments);
            } catch (Exception e) {
                callback.onError("Error getting ongoing experiments: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void getExperimentById(int experimentId, ExperimentCallback callback) {
        new Thread(() -> {
            try {
                // placeholder_SUPABASE_URL: Constant for the Supabase project URL
                String endpoint = SUPABASE_URL + "/rest/v1/Experiment?select=*&experimentId=eq." + experimentId;

                // placeholder_HttpHelper: Utility class for making HTTP requests
                String json = HttpHelper.getJson(endpoint);

                // Note: Supabase 'eq' filters still return a JSON array (list),
                // even if it's empty or has only one item.
                Type listType = new TypeToken<List<Experiment>>() {}.getType(); // placeholder_Experiment: Model class

                // placeholder_gson: Gson instance for JSON serialization/deserialization
                List<Experiment> experiments = gson.fromJson(json, listType);

                if (experiments != null && !experiments.isEmpty()) {
                    // Experiment found, return the first item
                    callback.onSuccess(experiments.get(0));
                } else {
                    // Experiment not found
                    callback.onError("Error getting experiment: " + experimentId);
                }
            } catch (Exception e) {
                callback.onError("Error getting experiment: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void getExperimentProject(int projectId, ProjectCallBack callback) {
        new Thread(() -> {
            try {
                // Tạo endpoint Supabase REST
                // projectId=eq.<id> -> filter theo projectId
                String endpoint = SUPABASE_URL + "/rest/v1/Project?select=*&projectId=eq." + projectId;

                // Gửi GET request
                String json = HttpHelper.getJson(endpoint);

                // Vì Supabase API trả về array JSON
                Type listType = new TypeToken<List<Project>>() {}.getType();
                List<Project> projects = gson.fromJson(json, listType);

                if (projects != null && !projects.isEmpty()) {
                    callback.onSuccess(projects.get(0));
                } else {
                    callback.onError("No project found with ID: " + projectId);
                }

            } catch (Exception e) {
                callback.onError("Error getting project: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void getLogEntryById(int logEntryId, LogEntryCallback callback) {
        new Thread(() -> {
            try {
                // Tạo endpoint Supabase REST cho bảng LogEntry
                // Lọc theo logEntryId.
                // LƯU Ý: Nếu cột khóa chính của bạn trong Supabase tên là "id",
                // hãy đổi "logEntryId=eq." thành "id=eq."
                String endpoint = SUPABASE_URL + "/rest/v1/LogEntry?select=*&logId=eq." + logEntryId;

                // Gửi GET request
                String json = HttpHelper.getJson(endpoint);

                // Vì Supabase API trả về array JSON, kể cả khi chỉ tìm 1 item
                Type listType = new TypeToken<List<LogEntry>>() {}.getType();
                List<LogEntry> logEntries = gson.fromJson(json, listType);

                if (logEntries != null && !logEntries.isEmpty()) {
                    // Trả về item đầu tiên (và duy nhất) tìm thấy
                    callback.onSuccess(logEntries.get(0));
                } else {
                    callback.onError("No log entry found with ID: " + logEntryId);
                }

            } catch (Exception e) {
                callback.onError("Error getting log entry: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void getFile(String url, FileCallBack callback) {
        if (url == null || url.isEmpty()) {
            callback.onError("URL không hợp lệ hoặc bị rỗng");
            return;
        }

        new Thread(() -> {
            try {
                // Giả định HttpHelper của bạn có một phương thức
                // 'downloadFile' nhận URL và trả về một đối tượng File
                // (đã được tải về và lưu tạm).
                File downloadedFile = HttpHelper.downloadFile(url);

                if (downloadedFile != null && downloadedFile.exists()) {
                    callback.onSuccess(downloadedFile);
                } else {
                    callback.onError("Tải file thất bại hoặc file không tồn tại từ URL.");
                }
            } catch (Exception e) {
                callback.onError("Lỗi khi tải file: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void completeExperiment(int experimentId, GenericCallback callback) {
        new Thread(() -> {
            try {
                // 1. Tạo endpoint Supabase REST cho bảng "Experiment"
                // Lọc theo experimentId.
                String endpoint = SUPABASE_URL + "/rest/v1/Experiment?experimentId=eq." + experimentId;

                // 2. Lấy giá trị status mới
                String status = LKMSConstantEnums.ExperimentStatus.COMPLETED.toString();

                java.util.Date today = new java.util.Date();
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                String todayString = sdf.format(today);

                // 3. Tạo JSON payload để gửi đi
                // Payload sẽ có dạng: {"experimentStatus": "COMPLETED"}
                // (Giả sử cột trong Supabase tên là "experimentStatus")
                String jsonPayload = "{\"experimentStatus\": \"" + status + "\", " +
                        "\"finishDate\": \"" + todayString + "\"}";

                // 4. Gửi PATCH request
                HttpHelper.patchJson(endpoint, jsonPayload);

                // 5. Nếu không ném ra exception, tức là đã thành công
                callback.onSuccess();

            } catch (Exception e) {
                // 6. Xử lý nếu có lỗi
                callback.onError("Error completing experiment: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void getExperimentReportData(int experimentId, ExperimentReportDataCallback callback) {
        new Thread(() -> {
            try {
                // 1. Lọc Endpoint RPC
                String endpoint = SUPABASE_URL + "/rest/v1/rpc/get_experiment_report_data";

                // 2. Tạo JSON Payload
                String jsonPayload = "{\"p_experiment_id\":" + experimentId + "}";

                String jsonResponse = HttpHelper.postJson(endpoint, jsonPayload);

                // 4. Parse JSON String thành Object
                if (jsonResponse != null && !jsonResponse.isEmpty()) {
                    Gson gson = new Gson();
                    ExperimentReportData data = gson.fromJson(jsonResponse, ExperimentReportData.class);

                    // 5. Thành công
                    callback.onSuccess(data);
                } else {
                    callback.onError("No data received from server.");
                }

            } catch (Exception e) {
                // 6. Xử lý lỗi
                callback.onError("Error fetching report data: " + e.getMessage());
            }
        }).start();
    }


}
