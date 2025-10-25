package com.lkms.data.model.java;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Đại diện cho một hàng trong bảng "Booking".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @SerializedName("bookingId")
    private Integer bookingId;

    // Khóa ngoại đến User
    @SerializedName("userId")
    private Integer userId;

    // Khóa ngoại đến Equipment
    @SerializedName("equipmentId")
    private Integer equipmentId;

    // Khóa ngoại đến Experiment
    @SerializedName("experimentId")
    private Integer experimentId;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("endTime")
    private String endTime;

    @SerializedName("bookingStatus")
    private String bookingStatus;

    @SerializedName("rejectReason")
    private String rejectReason;

    @SerializedName("equipmentName")
    private String equipmentName;
}
