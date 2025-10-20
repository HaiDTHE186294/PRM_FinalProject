package com.lkms.data.model.java;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Đại diện cho một hàng trong bảng "Protocol".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Protocol {

    @SerializedName("protocolId")
    private Integer protocolId;

    @SerializedName("protocolTitle")
    private String protocolTitle;

    @SerializedName("versionNumber")
    private String versionNumber;

    @SerializedName("introduction")
    private String introduction;

    @SerializedName("safetyWarning")
    private String safetyWarning;

    @SerializedName("approveStatus")
    private String approveStatus;

    // Khóa ngoại đến User (người tạo)
    @SerializedName("creatorUserId")
    private Integer creatorUserId;

    // Khóa ngoại đến User (người duyệt)
    @SerializedName("approverUserId")
    private Integer approverUserId;
}
