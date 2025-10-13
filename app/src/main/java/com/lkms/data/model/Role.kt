package com.lkms.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Role(
    @SerialName("roleId")
    val roleId: Int,

    @SerialName("roleName")
    val roleName: String? = null,

    @SerialName("roleStatus")
    val roleStatus: String? = null
)
