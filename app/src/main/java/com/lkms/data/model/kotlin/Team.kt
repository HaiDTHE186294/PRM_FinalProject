package com.lkms.data.model.kotlin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Đại diện cho một hàng trong bảng "Team".
 * Bảng này có khóa chính phức hợp (experimentId, userId).
 */
@Serializable
data class Team(
    // Khóa ngoại đến Experiment
    @SerialName("experimentId")
    val experimentId: Int? = null,

    // Khóa ngoại đến User
    @SerialName("userId")
    val userId: Int? = null,

    @SerialName("status")
    val status: String? = null
)