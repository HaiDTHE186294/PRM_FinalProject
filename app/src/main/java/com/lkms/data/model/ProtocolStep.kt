// Trong file: data/model/ProtocolStep.kt
package com.lkms.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Đại diện cho một hàng trong bảng "ProtocolStep".
 */
@Serializable
data class ProtocolStep(
    @SerialName("protocolStepId")
    val protocolStepId: Int? = null,

    @SerialName("stepOrder")
    val stepOrder: Int? = null,

    // Khóa ngoại đến Protocol
    @SerialName("protocolId")
    val protocolId: Int? = null,

    @SerialName("instruction")
    val instruction: String? = null
)