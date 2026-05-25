package me.slarker.yunsou.data.repository

import me.slarker.yunsou.data.api.PanSouApi
import me.slarker.yunsou.data.api.dto.CheckLinksRequest
import me.slarker.yunsou.data.api.dto.LinkCheckItem
import me.slarker.yunsou.data.api.dto.SearchRequest
import me.slarker.yunsou.data.model.CloudType
import me.slarker.yunsou.data.model.LinkState
import me.slarker.yunsou.data.model.MergedGroup
import me.slarker.yunsou.data.model.ResourceItem
import me.slarker.yunsou.data.model.SearchResult
import me.slarker.yunsou.data.model.toDomain
import javax.inject.Inject
import javax.inject.Singleton

class ApiException(val code: Int, message: String) : Exception(message)

@Singleton
class SearchRepository @Inject constructor(
    private val api: PanSouApi
) {
    suspend fun search(keyword: String, cloudTypes: List<CloudType>): Result<SearchResult> {
        return try {
            val response = api.search(
                SearchRequest(
                    kw = keyword,
                    cloudTypes = cloudTypes.map { it.apiName },
                    res = "merge"
                )
            )
            if (response.code != 0 || response.data == null) {
                return Result.failure(ApiException(response.code, response.message ?: "未知错误"))
            }
            val data = response.data
            val merged = data.mergedByType?.mapNotNull { (apiName, items) ->
                val cloudType = CloudType.fromApiName(apiName)
                if (cloudType != null && items.isNotEmpty()) {
                    MergedGroup(cloudType, items.map { it.toDomain() })
                } else null
            } ?: emptyList()
            Result.success(SearchResult(data.total, merged))
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException(-1, e.message ?: "网络请求失败"))
        }
    }

    suspend fun checkLinksBatch(items: List<LinkCheckItem>): Result<Map<String, LinkState>> {
        if (items.isEmpty()) return Result.success(emptyMap())
        return try {
            val response = api.checkLinks(CheckLinksRequest(items = items))
            val results = response.results ?: emptyList()
            val map = results.mapNotNull { r ->
                r.url?.let { url -> url to LinkState.fromApi(r.state) }
            }.toMap()
            Result.success(map)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkHealth(): Result<Boolean> {
        return try {
            val response = api.health()
            Result.success(response.status == "ok")
        } catch (e: Exception) {
            Result.success(false)
        }
    }
}
