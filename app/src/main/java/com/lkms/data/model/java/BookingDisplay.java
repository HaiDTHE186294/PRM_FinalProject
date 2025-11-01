package com.lkms.data.model.java;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDisplay {
    @SerializedName("equipmentId")
    private int equipmentId;

    @SerializedName("equipmentName")
    private String equipmentName;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("endTime")
    private String endTime;
}
