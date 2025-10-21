// Trong file: data/model/ExperimentStep.kt
package com.lkms.data.model.kotlin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Đại diện cho một hàng trong bảng "ExperimentStep".
 */
@Serializable
data class ExperimentStep(
    @SerialName("experimentStepId")
    val experimentStepId: Int? = null,

    // Khóa ngoại đến Experiment
    @SerialName("experimentId")
    val experimentId: Int? = null,

    // Khóa ngoại đến ProtocolStep
    @SerialName("protocolStepId")
    val protocolStepId: Int? = null,

    @SerialName("stepStatus")
    val stepStatus: String? = null,

    @SerialName("stepResult")
    val stepResult: String? = null
)