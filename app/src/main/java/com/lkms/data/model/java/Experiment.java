package com.lkms.data.model.java;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Đại diện cho một hàng trong bảng "Experiment".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Experiment {

    @SerializedName("experimentId")
    private Integer experimentId;

    @SerializedName("experimentTitle")
    private String experimentTitle;

    @SerializedName("objective")
    private String objective;

    @SerializedName("experimentStatus")
    private String experimentStatus;

    // Kiểu 'date' trong SQL thường được xử lý như String trong client
    @SerializedName("startDate")
    private String startDate;

    @SerializedName("finishDate")
    private String finishDate;

    // Khóa ngoại đến User
    @SerializedName("userId")
    private Integer userId;

    // Khóa ngoại đến Protocol
    @SerializedName("protocolId")
    private Integer protocolId;

    // Khóa ngoại đến Project
    @SerializedName("projectId")
    private Integer projectId;
}
