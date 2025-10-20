package com.lkms.data.model.java;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Đại diện cho một hàng trong bảng "LogEntry".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogEntry {

    @SerializedName("logId")
    private Integer logId;

    // Khóa ngoại đến ExperimentStep
    @SerializedName("experimentStepId")
    private Integer experimentStepId;

    @SerializedName("logType")
    private String logType;

    // Khóa ngoại đến User
    @SerializedName("userId")
    private Integer userId;

    @SerializedName("content")
    private String content;

    @SerializedName("url")
    private String url;

    @SerializedName("logTime")
    private String logTime;
}
