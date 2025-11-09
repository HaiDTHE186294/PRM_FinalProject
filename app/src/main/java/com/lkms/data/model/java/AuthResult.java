package com.lkms.data.model.java;

import com.google.gson.annotations.SerializedName;
import com.lkms.data.model.java.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResult {
        // 🔹 Token xác thực (JWT)
        @SerializedName("auth_token")
        private String authToken;

        // 🔹 ID của vai trò người dùng
        @SerializedName("role_id")
        private int roleId;

        // 🔹 ID người dùng
        @SerializedName("user_id")
        private int userId;
}
