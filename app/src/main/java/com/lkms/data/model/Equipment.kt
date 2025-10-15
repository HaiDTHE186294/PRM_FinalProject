// Trong file: data/model/Equipment.kt
package com.lkms.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Đại diện cho một hàng trong bảng "Equipment".
 */
@Serializable
data class Equipment(
    @SerialName("equipmentId")
    val equipmentId: Int? = null,

    @SerialName("equipmentName")
    val equipmentName: String? = null,

    @SerialName("model")
    val model: String? = null,

    @SerialName("serialNumber")
    val serialNumber: String? = null,

    @SerialName("availability")
    val availability: Boolean? = null
)