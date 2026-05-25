package me.slarker.yunsou.data.model

data class SearchResult(
    val total: Int,
    val mergedGroups: List<MergedGroup>
)

data class MergedGroup(
    val cloudType: CloudType,
    val items: List<ResourceItem>
)
