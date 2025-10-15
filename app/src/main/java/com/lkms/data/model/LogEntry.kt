// Trong file: data/model/LogEntry.kt
package com.lkms.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Đại diện cho một hàng trong bảng "LogEntry".
 */
@Serializable
data class LogEntry(
    @SerialName("logId")
    val logId: Int? = null,

    // Khóa ngoại đến ExperimentStep
    @SerialName("experimentStepId")
    val experimentStepId: Int? = null,

    @SerialName("logType")
    val logType: String? = null,

    // Khóa ngoại đến User
    @SerialName("userId")
    val userId: Int? = null,

    @SerialName("content")
    val content: String? = null,

    @SerialName("url")
    val url: String? = null,

    @SerialName("logTime")
    val logTime: String? = null
)