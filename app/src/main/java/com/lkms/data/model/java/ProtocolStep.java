package com.lkms.data.model.java;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Đại diện cho một hàng trong bảng "ProtocolStep".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProtocolStep that = (ProtocolStep) o;

        return Objects.equals(getProtocolStepId(), that.getProtocolStepId()) &&
                Objects.equals(getStepOrder(), that.getStepOrder()) &&
                Objects.equals(getInstruction(), that.getInstruction());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProtocolStepId(), getStepOrder(), getInstruction());
    }
}
