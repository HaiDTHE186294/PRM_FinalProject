package com.lkms.data.model.java;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Đại diện cho một hàng trong bảng "InventoryTransaction".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransaction {

    @SerializedName("transactionId")
    private Integer transactionId;

    @SerializedName("transactionType")
    private String transactionType;

    // Khóa ngoại đến Item
    @SerializedName("itemId")
    private Integer itemId;

    // Khóa ngoại đến User
    @SerializedName("userId")
    private Integer userId;

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("transactionTime")
    private String transactionTime;

    @SerializedName("transactionStatus")
    private String transactionStatus;

    @SerializedName("rejectReason")
    private String rejectReason;
}
