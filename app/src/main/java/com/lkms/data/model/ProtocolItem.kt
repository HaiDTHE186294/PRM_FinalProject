// Trong file: data/model/ProtocolItem.kt
package com.lkms.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Đại diện cho một hàng trong bảng "ProtocolItem".
 * Bảng này có khóa chính phức hợp (protocolId, itemId).
 */
@Serializable
data class ProtocolItem(
    // Khóa ngoại đến Protocol
    @SerialName("protocolId")
    val protocolId: Int,

    // Khóa ngoại đến Item
    @SerialName("itemId")
    val itemId: Int,

    @SerialName("quantity")
    val quantity: Int? = null
)