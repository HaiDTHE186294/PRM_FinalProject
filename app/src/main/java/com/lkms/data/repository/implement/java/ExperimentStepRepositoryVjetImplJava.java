package com.lkms.data.repository.implement.java;

import static com.lkms.BuildConfig.SUPABASE_URL;

import com.google.gson.Gson;
import com.lkms.data.model.java.ExperimentStep;
import com.lkms.data.repository.IExperimentRepository;
import com.lkms.data.repository.IExperimentStepRepositoryVjet;
import java.util.List;

public class ExperimentStepRepositoryVjetImplJava implements IExperimentStepRepositoryVjet {
    private static final Gson gson = new Gson();

    @Override
    public void createExperimentSteps(List<ExperimentStep> steps, IExperimentRepository.GenericCallback callback) {
        new Thread(() -> {
            try {
                // Nếu danh sách rỗng, không cần gọi API, coi như thành công.
                if (steps == null || steps.isEmpty()) {
                    callback.onSuccess();
                    return;
                }

                // Endpoint của bảng ExperimentStep
                String endpoint = SUPABASE_URL + "/rest/v1/ExperimentStep";

                // Chuyển danh sách thành một chuỗi JSON. Supabase hỗ trợ insert một mảng object.
                String jsonBody = gson.toJson(steps);

                // Gọi API và không cần xử lý kết quả trả về, vì postJson sẽ báo lỗi nếu thất bại.
                HttpHelper.postJson(endpoint, jsonBody);

                // Nếu không có lỗi, gọi onSuccess.
                callback.onSuccess();

            } catch (Exception e) {
                // Nếu có lỗi mạng hoặc lỗi từ server, báo lỗi về.
                callback.onError("Lỗi khi tạo Experiment Steps: " + e.getMessage());
            }
        }).start();
    }
}
