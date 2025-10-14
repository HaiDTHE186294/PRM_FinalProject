package com.lkms.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Project(
    @SerialName("projectId")
    val projectId: Int? = null,

    @SerialName("projectTitle")
    val projectTitle: String? = null,

    // Khóa ngoại, liên kết đến userId trong bảng User
    @SerialName("projectLeaderId")
    val projectLeaderId: Int? = null
)