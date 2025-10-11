// Trong file: data/model/UserManual.kt
package com.lkms.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Đại diện cho một hàng trong bảng "UserManual".
 */
@Serializable
data class UserManual(
    @SerialName("manualId")
    val manualId: String,

    @SerialName("url")
    val url: String? = null
)