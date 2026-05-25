package me.slarker.yunsou.data.model

import me.slarker.yunsou.data.api.dto.ResourceItemDto

data class ResourceItem(
    val url: String,
    val password: String?,
    val note: String?,
    val datetime: String?,
    val source: String?,
    val images: List<String>
)

fun ResourceItemDto.toDomain() = ResourceItem(
    url = url,
    password = password?.takeIf { it.isNotBlank() },
    note = note,
    datetime = datetime,
    source = source,
    images = images
)
