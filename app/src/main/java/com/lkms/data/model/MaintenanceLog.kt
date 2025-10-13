// Trong file: data/model/MaintenanceLog.kt
package com.lkms.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Đại diện cho một hàng trong bảng "MaintenanceLog".
 */
@Serializable
data class MaintenanceLog(
    @SerialName("maintenanceId")
    val maintenanceId: Int,

    // Khóa ngoại đến User
    @SerialName("userId")
    val userId: Int? = null,

    // Khóa ngoại đến Equipment
    @SerialName("equipmentId")
    val equipmentId: Int? = null,

    @SerialName("maintenanceTime")
    val maintenanceTime: String? = null,

    @SerialName("maintenanceType")
    val maintenanceType: String? = null,

    @SerialName("detail")
    val detail: String? = null
)