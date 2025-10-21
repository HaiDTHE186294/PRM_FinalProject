package com.lkms.data.model.java;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * Đại diện cho một hàng trong bảng "UserManual".
 */
@Data
public class UserManual {

    @SerializedName("manualId")
    private String manualId;

    @SerializedName("url")
    private String url;
}
