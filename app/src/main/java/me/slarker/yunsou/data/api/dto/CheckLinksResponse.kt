package me.slarker.yunsou.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckLinksResponse(
    val results: List<LinkCheckResultDto>? = null
)

@Serializable
data class LinkCheckResultDto(
    @SerialName("disk_type") val diskType: String? = null,
    val url: String? = null,
    val state: String = "uncertain",
    val summary: String? = null
)
