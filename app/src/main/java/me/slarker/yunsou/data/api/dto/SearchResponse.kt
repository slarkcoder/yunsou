package me.slarker.yunsou.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchResponse(
    val total: Int = 0,
    @SerialName("merged_by_type") val mergedByType: Map<String, List<ResourceItemDto>>? = null
)
