package com.lkms.data.model.kotlin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PeerReview(
    @SerialName("reviewId")
    val reviewId: Int? = null,

    // Khóa ngoại đến Project
    @SerialName("projectId")
    val projectId: Int? = null,

    @SerialName("startTime")
    val startTime: String? = null,

    @SerialName("endTime")
    val endTime: String? = null,

    @SerialName("detail")
    val detail: String? = null
)