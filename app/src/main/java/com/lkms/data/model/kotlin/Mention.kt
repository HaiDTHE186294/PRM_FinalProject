// Trong file: data/model/Mention.kt
package com.lkms.data.model.kotlin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Đại diện cho một hàng trong bảng "Mention".
 * Bảng này có khóa chính phức hợp (commentId, userId).
 */
@Serializable
data class Mention(
    // Khóa ngoại đến Comment
    @SerialName("commentId")
    val commentId: Int? = null,

    // Khóa ngoại đến User
    @SerialName("userId")
    val userId: Int? = null
)