package me.slarker.yunsou.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckLinksRequest(
    val items: List<LinkCheckItem>
)

@Serializable
data class LinkCheckItem(
    @SerialName("disk_type") val diskType: String,
    val url: String,
    val password: String? = null
)
