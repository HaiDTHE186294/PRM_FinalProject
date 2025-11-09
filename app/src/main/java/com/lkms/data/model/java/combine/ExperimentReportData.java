package com.lkms.data.model.java.combine;

// Ngài sẽ cần import thư viện này
import com.google.gson.annotations.SerializedName;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Thêm 'public' để các lớp khác có thể truy cập
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExperimentReportData {
    @SerializedName("projectTitle")
    String projectTitle;

    @SerializedName("projectLeaderName")
    String projectLeaderName;

    @SerializedName("experimentId")
    int experimentId;

    @SerializedName("experimentTitle")
    String experimentTitle;

    @SerializedName("objective")
    String objective;

    @SerializedName("startDate")
    String startDate;

    @SerializedName("finishDate")
    String finishDate;

    @SerializedName("creatorName")
    String creatorName;

    @SerializedName("protocolTitle")
    String protocolTitle;

    @SerializedName("protocolVersionNumber")
    String protocolVersionNumber;

    @SerializedName("protocolIntroduction")
    String protocolIntroduction;

    @SerializedName("steps")
    List<ReportStep> steps;
}

