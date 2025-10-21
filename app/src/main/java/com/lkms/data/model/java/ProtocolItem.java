package com.lkms.data.model.java;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Đại diện cho một hàng trong bảng "ProtocolItem".
 * Bảng này có khóa chính phức hợp (protocolId, itemId).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProtocolItem {

    // Khóa ngoại đến Protocol
    @SerializedName("protocolId")
    private Integer protocolId;

    // Khóa ngoại đến Item
    @SerializedName("itemId")
    private Integer itemId;

    @SerializedName("quantity")
    private Integer quantity;
}
