package me.slarker.yunsou.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchRequest(
    val kw: String,
    @SerialName("cloud_types") val cloudTypes: List<String>,
    val res: String = "merge"
)
