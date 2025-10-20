package com.lkms.data.model.java;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Đại diện cho một hàng trong bảng "Team".
 * Bảng này có khóa chính phức hợp (experimentId, userId).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Team {

    // Khóa ngoại đến Experiment
    @SerializedName("experimentId")
    private Integer experimentId;

    // Khóa ngoại đến User
    @SerializedName("userId")
    private Integer userId;

    @SerializedName("status")
    private String status;
}
