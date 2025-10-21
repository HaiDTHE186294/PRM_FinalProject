package com.lkms.data.model.java;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Đại diện cho một hàng trong bảng "Item".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    @SerializedName("itemId")
    private Integer itemId;

    @SerializedName("itemName")
    private String itemName;

    @SerializedName("casNumber")
    private String casNumber;

    @SerializedName("lotNumber")
    private String lotNumber;

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("unit")
    private String unit;

    @SerializedName("location")
    private String location;

    @SerializedName("expirationDate")
    private String expirationDate;
}
