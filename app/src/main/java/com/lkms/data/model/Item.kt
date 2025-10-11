// Trong file: data/model/Item.kt
package com.lkms.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Đại diện cho một hàng trong bảng "Item".
 */
@Serializable
data class Item(
    @SerialName("itemId")
    val itemId: Int,

    @SerialName("itemName")
    val itemName: String? = null,

    @SerialName("casNumber")
    val casNumber: String? = null,

    @SerialName("lotNumber")
    val lotNumber: String? = null,

    @SerialName("quantity")
    val quantity: Int? = null,

    @SerialName("unit")
    val unit: String? = null,

    @SerialName("location")
    val location: String? = null,

    @SerialName("expirationDate")
    val expirationDate: String? = null
)