package com.lkms.data.model.java;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Đại diện cho một hàng trong bảng "PeerReview".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PeerReview {

    @SerializedName("reviewId")
    private Integer reviewId;

    // Khóa ngoại đến Project
    @SerializedName("projectId")
    private Integer projectId;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("endTime")
    private String endTime;

    @SerializedName("detail")
    private String detail;
}
