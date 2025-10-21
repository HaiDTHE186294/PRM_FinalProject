// Trong file: data/model/Comment.kt
package com.lkms.data.model.kotlin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Đại diện cho một hàng trong bảng "Comment".
 */
@Serializable
data class Comment(
    @SerialName("commentId")
    val commentId: Int? = null,

    @SerialName("commentType")
    val commentType: String? = null,

    @SerialName("commentText")
    val commentText: String? = null,

    @SerialName("timeStamp")
    val timeStamp: String? = null,

    // Khóa ngoại đến Experiment
    @SerialName("experimentId")
    val experimentId: Int? = null,

    // Khóa ngoại đến User
    @SerialName("userId")
    val userId: Int? = null,

    // Khóa ngoại đến LogEntry
    @SerialName("logId")
    val logId: Int? = null
)