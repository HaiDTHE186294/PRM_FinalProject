package com.lkms.data.model.java;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Đại diện cho một hàng trong bảng "MaintenanceLog".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceLog {

    @SerializedName("maintenanceId")
    private Integer maintenanceId;

    // Khóa ngoại đến User
    @SerializedName("userId")
    private Integer userId;

    // Khóa ngoại đến Equipment
    @SerializedName("equipmentId")
    private Integer equipmentId;

    @SerializedName("maintenanceTime")
    private String maintenanceTime;

    @SerializedName("maintenanceType")
    private String maintenanceType;

    @SerializedName("detail")
    private String detail;
}
