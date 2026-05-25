package me.slarker.yunsou.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class ResourceItemDto(
    val url: String,
    val password: String? = null,
    val note: String? = null,
    val datetime: String? = null,
    val source: String? = null,
    val images: List<String> = emptyList()
)
