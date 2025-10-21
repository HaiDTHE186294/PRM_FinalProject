package com.lkms.data.model.java;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Đại diện cho một hàng trong bảng "ProtocolStep".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProtocolStep {

    @SerializedName("protocolStepId")
    private Integer protocolStepId;

    @SerializedName("stepOrder")
    private Integer stepOrder;

    // Khóa ngoại đến Protocol
    @SerializedName("protocolId")
    private Integer protocolId;

    @SerializedName("instruction")
    private String instruction;
}
