package com.lkms.data.model.java;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import com.google.gson.annotations.JsonAdapter;
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums.ProtocolApproveStatus;
import com.lkms.data.repository.enumPackage.java.ProtocolStatusAdapter;

import java.util.Objects;

/**
 * Đại diện cho một hàng trong bảng "Protocol".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
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
    @JsonAdapter(ProtocolStatusAdapter.class)
    private ProtocolApproveStatus approveStatus;

    // Khóa ngoại đến User (người tạo)
    @SerializedName("creatorUserId")
    private Integer creatorUserId;

    // Khóa ngoại đến User (người duyệt)
    @SerializedName("approverUserId")
    private Integer approverUserId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Protocol protocol = (Protocol) o;
        return Objects.equals(getProtocolId(), protocol.getProtocolId()) &&
                Objects.equals(getProtocolTitle(), protocol.getProtocolTitle()) &&
                Objects.equals(getVersionNumber(), protocol.getVersionNumber()) &&
                getApproveStatus() == protocol.getApproveStatus();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProtocolId(), getProtocolTitle(), getVersionNumber(), getApproveStatus());
    }
}
