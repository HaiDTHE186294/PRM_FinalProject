package com.lkms.data.model.java;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Đại diện cho một hàng trong bảng "SDS".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SDS {

    @SerializedName("sdsId")
    private String sdsId;

    @SerializedName("url")
    private String url;
}
