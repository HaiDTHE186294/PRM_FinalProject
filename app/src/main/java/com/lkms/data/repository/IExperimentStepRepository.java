package com.lkms.data.repository;

import com.lkms.data.model.java.ExperimentStep;
import java.util.List;

/**
 * Interface Repository chịu trách nhiệm quản lý các bản ghi ExperimentStep. * Tách ra từ IExperimentRepository để tuân thủ Nguyên tắc Đơn trách nhiệm (SRP).
 */
public interface IExperimentStepRepository {

    /**
     * Tạo nhiều bản ghi ExperimentStep cùng lúc.
     * Thường được gọi sau khi một Experiment mới được tạo.
     * @param steps Danh sách các đối tượng ExperimentStep cần chèn vào DB.
     * @param callback Callback để thông báo thành công hoặc thất bại.
     */
    void createExperimentSteps(List<ExperimentStep> steps, IExperimentRepository.GenericCallback callback);
}
