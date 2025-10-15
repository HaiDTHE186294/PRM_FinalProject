// Trong file: data/model/Protocol.kt
package com.lkms.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Đại diện cho một hàng trong bảng "Protocol".
 */
@Serializable
data class Protocol(
    @SerialName("protocolId")
    val protocolId: Int? = null,

    @SerialName("protocolTitle")
    val protocolTitle: String? = null,

    @SerialName("versionNumber")
    val versionNumber: String? = null,

    @SerialName("introduction")
    val introduction: String? = null,

    @SerialName("safetyWarning")
    val safetyWarning: String? = null,

    @SerialName("approveStatus")
    val approveStatus: String? = null,

    // Khóa ngoại đến User (người tạo)
    @SerialName("creatorUserId")
    val creatorUserId: Int? = null,

    // Khóa ngoại đến User (người duyệt)
    @SerialName("approverUserId")
    val approverUserId: Int? = null
)