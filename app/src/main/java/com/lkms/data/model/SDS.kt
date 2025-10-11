// Trong file: data/model/SDS.kt
package com.lkms.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Đại diện cho một hàng trong bảng "SDS".
 */
@Serializable
data class SDS(
    @SerialName("sdsId")
    val sdsId: String,

    @SerialName("url")
    val url: String? = null
)