// Trong file: data/model/InventoryTransaction.kt
package com.lkms.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Đại diện cho một hàng trong bảng "InventoryTransaction".
 */
@Serializable
data class InventoryTransaction(
    @SerialName("transactionId")
    val transactionId: Int,

    @SerialName("transactionType")
    val transactionType: String? = null,

    // Khóa ngoại đến Item
    @SerialName("itemId")
    val itemId: Int? = null,

    // Khóa ngoại đến User
    @SerialName("userId")
    val userId: Int? = null,

    @SerialName("quantity")
    val quantity: Int? = null,

    @SerialName("transactionTime")
    val transactionTime: String? = null,

    @SerialName("transactionStatus")
    val transactionStatus: String? = null,

    @SerialName("rejectReason")
    val rejectReason: String? = null
)