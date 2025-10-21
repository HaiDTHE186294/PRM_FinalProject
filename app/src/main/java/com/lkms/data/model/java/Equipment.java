package com.lkms.data.model.java;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Đại diện cho một hàng trong bảng "Equipment".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Equipment {

    @SerializedName("equipmentId")
    private Integer equipmentId;

    @SerializedName("equipmentName")
    private String equipmentName;

    @SerializedName("model")
    private String model;

    @SerializedName("serialNumber")
    private String serialNumber;

    @SerializedName("availability")
    private Boolean availability;
}
