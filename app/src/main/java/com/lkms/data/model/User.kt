package com.lkms.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("userId")
    val userId: Int,

    @SerialName("email")
    val email: String? = null,

    // Bắt buộc phải decrypt, chỉ dùng khi tạo user hoặc xác nhận mật khẩu
    @SerialName("password")
    val password: String? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("contactInfo")
    val contactInfo: String? = null,

    @SerialName("roleId")
    val roleId: Int? = null,

    @SerialName("userStatus")
    val userStatus: String? = null
)
