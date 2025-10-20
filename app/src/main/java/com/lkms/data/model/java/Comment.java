package com.lkms.data.model.java;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Đại diện cho một hàng trong bảng "Comment".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @SerializedName("commentId")
    private Integer commentId;

    @SerializedName("commentType")
    private String commentType;

    @SerializedName("commentText")
    private String commentText;

    @SerializedName("timeStamp")
    private String timeStamp;

    // Khóa ngoại đến Experiment
    @SerializedName("experimentId")
    private Integer experimentId;

    // Khóa ngoại đến User
    @SerializedName("userId")
    private Integer userId;

    // Khóa ngoại đến LogEntry
    @SerializedName("logId")
    private Integer logId;
}
