package com.lkms.data.model.java.combine;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportStep {
    @SerializedName("stepOrder")
    int stepOrder;

    @SerializedName("instruction")
    String instruction;

    @SerializedName("logs")
    List<ReportLog> logs;
}
