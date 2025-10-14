package com.lkms.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Experiment(
    @SerialName("experimentId")
    val experimentId: Int? = null,

    @SerialName("experimentTitle")
    val experimentTitle: String? = null,

    @SerialName("objective")
    val objective: String? = null,

    @SerialName("experimentStatus")
    val experimentStatus: String? = null,

    // Kiểu 'date' trong SQL thường được xử lý như String trong client
    @SerialName("startDate")
    val startDate: String? = null,

    @SerialName("finishDate")
    val finishDate: String? = null,

    // Khóa ngoại đến User
    @SerialName("userId")
    val userId: Int? = null,

    // Khóa ngoại đến Protocol
    @SerialName("protocolId")
    val protocolId: Int? = null,

    // Khóa ngoại đến Project
    @SerialName("projectId")
    val projectId: Int? = null
)