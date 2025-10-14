// Trong file: data/model/Booking.kt
package com.lkms.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Đại diện cho một hàng trong bảng "Booking".
 */
@Serializable
data class Booking(
    @SerialName("bookingId")
    val bookingId: Int? = null,

    // Khóa ngoại đến User
    @SerialName("userId")
    val userId: Int? = null,

    // Khóa ngoại đến Equipment
    @SerialName("equipmentId")
    val equipmentId: Int? = null,

    // Khóa ngoại đến Experiment
    @SerialName("experimentId")
    val experimentId: Int? = null,

    @SerialName("startTime")
    val startTime: String? = null,

    @SerialName("endTime")
    val endTime: String? = null,

    @SerialName("bookingStatus")
    val bookingStatus: String? = null,

    @SerialName("rejectReason")
    val rejectReason: String? = null
)