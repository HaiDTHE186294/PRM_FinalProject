package com.lkms.data.model.java.combine;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportLog {
    @SerializedName("logType")
    String logType;

    @SerializedName("content")
    String content;

    @SerializedName("fileUrl")
    String fileUrl;

    @SerializedName("logTime")
    String logTime;

    @SerializedName("userName")
    String userName;
}
