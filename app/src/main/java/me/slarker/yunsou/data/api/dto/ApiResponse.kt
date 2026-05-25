package me.slarker.yunsou.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val code: Int,
    val message: String? = null,
    val data: T? = null
)
