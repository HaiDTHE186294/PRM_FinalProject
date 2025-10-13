package com.lkms.data.model

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
    val experimentId: Int,

    // Khóa ngoại đến User
    @SerialName("userId")
    val userId: Int,

    @SerialName("status")
    val status: String? = null
)