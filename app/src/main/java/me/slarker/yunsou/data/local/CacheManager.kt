package me.slarker.yunsou.data.local

import android.content.Context
import me.slarker.yunsou.data.model.CloudType
import me.slarker.yunsou.data.model.MergedGroup
import me.slarker.yunsou.data.model.ResourceItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Serializable
data class CachedResourceItem(
    val url: String,
    val password: String? = null,
    val note: String? = null,
    val datetime: String? = null,
    val source: String? = null,
    val images: List<String> = emptyList()
)

@Serializable
data class CachedMergedGroup(
    val cloudTypeName: String,
    val items: List<CachedResourceItem>
)

@Serializable
data class CachedSearchResult(
    val query: String,
    val cloudTypes: List<String>,
    val mergedGroups: List<CachedMergedGroup>,
    val totalCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Singleton
class CacheManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val cacheDir = File(context.filesDir, "search_cache")
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private const val MAX_AGE_MS = 7 * 24 * 60 * 60 * 1000L // 7 days
    }

    init {
        cacheDir.mkdirs()
    }

    private fun cacheKey(query: String, cloudTypes: List<CloudType>): String {
        val typeKey = cloudTypes.map { it.apiName }.sorted().joinToString("_")
        return abs("$query|$typeKey".hashCode()).toString(16)
    }

    fun get(query: String, cloudTypes: List<CloudType>): CachedSearchResult? {
        val file = File(cacheDir, cacheKey(query, cloudTypes))
        if (!file.exists()) return null
        return try {
            val cached = json.decodeFromString<CachedSearchResult>(file.readText())
            if (System.currentTimeMillis() - cached.timestamp < MAX_AGE_MS) cached else {
                file.delete()
                null
            }
        } catch (_: Exception) {
            file.delete()
            null
        }
    }

    fun put(
        query: String,
        cloudTypes: List<CloudType>,
        mergedGroups: List<MergedGroup>,
        totalCount: Int
    ) {
        val file = File(cacheDir, cacheKey(query, cloudTypes))
        val cached = CachedSearchResult(
            query = query,
            cloudTypes = cloudTypes.map { it.apiName },
            mergedGroups = mergedGroups.map { group ->
                CachedMergedGroup(
                    cloudTypeName = group.cloudType.apiName,
                    items = group.items.map { item ->
                        CachedResourceItem(
                            url = item.url,
                            password = item.password,
                            note = item.note,
                            datetime = item.datetime,
                            source = item.source,
                            images = item.images
                        )
                    }
                )
            },
            totalCount = totalCount
        )
        try {
            file.writeText(json.encodeToString(cached))
        } catch (_: Exception) {}
    }

    fun toDomain(cached: CachedSearchResult): List<MergedGroup> {
        return cached.mergedGroups.mapNotNull { cachedGroup ->
            val cloudType = CloudType.fromApiName(cachedGroup.cloudTypeName) ?: return@mapNotNull null
            val items = cachedGroup.items.map { item ->
                ResourceItem(
                    url = item.url,
                    password = item.password,
                    note = item.note,
                    datetime = item.datetime,
                    source = item.source,
                    images = item.images
                )
            }
            MergedGroup(cloudType, items)
        }
    }

    fun clearAll() {
        cacheDir.listFiles()?.forEach { it.delete() }
    }
}
