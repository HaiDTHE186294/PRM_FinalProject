package com.lkms.data.model.java;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Đại diện cho một hàng trong bảng "User".
 * Lưu ý: Trường "password" chỉ nên được dùng khi tạo user hoặc xác nhận mật khẩu.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @SerializedName("userId")
    private Integer userId;

    @SerializedName("email")
    private String email;

    // Bắt buộc phải decrypt, chỉ dùng khi tạo user hoặc xác nhận mật khẩu
    @SerializedName("password")
    private String password;

    @SerializedName("name")
    private String name;

    @SerializedName("contactInfo")
    private String contactInfo;

    @SerializedName("roleId")
    private Integer roleId;

    @SerializedName("userStatus")
    private String userStatus;
}
