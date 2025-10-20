package com.lkms.data.model.java;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Đại diện cho một hàng trong bảng "ExperimentStep".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExperimentStep {

    @SerializedName("experimentStepId")
    private Integer experimentStepId;

    // Khóa ngoại đến Experiment
    @SerializedName("experimentId")
    private Integer experimentId;

    // Khóa ngoại đến ProtocolStep
    @SerializedName("protocolStepId")
    private Integer protocolStepId;

    @SerializedName("stepStatus")
    private String stepStatus;

    @SerializedName("stepResult")
    private String stepResult;
}
