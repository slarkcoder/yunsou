package me.slarker.yunsou.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import me.slarker.yunsou.data.api.dto.GithubRelease
import me.slarker.yunsou.data.api.dto.LinkCheckItem
import me.slarker.yunsou.data.local.CacheManager
import me.slarker.yunsou.data.local.PreferencesManager
import me.slarker.yunsou.data.model.CloudType
import me.slarker.yunsou.data.model.LinkState
import me.slarker.yunsou.data.model.MergedGroup
import me.slarker.yunsou.data.model.ResourceItem
import me.slarker.yunsou.data.repository.ApiException
import me.slarker.yunsou.data.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import javax.inject.Inject

enum class ServerStatus { UNKNOWN, CHECKING, ONLINE, OFFLINE }
enum class UpdateStatus { UNCHECKED, CHECKING, AVAILABLE, UNAVAILABLE, ERROR }

data class SearchUiState(
    val query: String = "",
    val selectedCloudTypes: Set<CloudType> = setOf(CloudType.QUARK),
    val isLoading: Boolean = false,
    val isChecking: Boolean = false,
    val error: String? = null,
    val totalCount: Int = 0,
    val mergedGroups: List<MergedGroup> = emptyList(),
    val linkCheckResults: Map<String, LinkState> = emptyMap(),
    val hasSearched: Boolean = false,
    val currentTab: Int = 0,
    val baseUrl: String = PreferencesManager.DEFAULT_BASE_URL,
    val searchHistory: List<String> = emptyList(),
    val serverStatus: ServerStatus = ServerStatus.UNKNOWN,
    val updateStatus: UpdateStatus = UpdateStatus.UNCHECKED,
    val latestVersion: String? = null,
    val releaseUrl: String? = null,
    val releaseNotes: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SearchRepository,
    private val prefs: PreferencesManager,
    private val cache: CacheManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SearchUiState(
            baseUrl = prefs.getBaseUrl(),
            searchHistory = prefs.getSearchHistory(),
            selectedCloudTypes = prefs.getCloudTypes()
        )
    )
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun onCloudTypeToggle(type: CloudType) {
        _uiState.update { state ->
            val next = if (type in state.selectedCloudTypes)
                state.selectedCloudTypes - type
            else
                state.selectedCloudTypes + type
            prefs.setCloudTypes(next)
            state.copy(selectedCloudTypes = next)
        }
    }

    fun onTabChange(tab: Int) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun onBaseUrlChange(url: String) {
        prefs.setBaseUrl(url)
        prefs.clearCachedServerStatus()
        _uiState.update { it.copy(baseUrl = prefs.getBaseUrl(), serverStatus = ServerStatus.UNKNOWN) }
        checkServerStatus()
    }

    fun onSearchHistoryItemClick(keyword: String) {
        _uiState.update { it.copy(query = keyword) }
        onSearch()
    }

    fun onSearch() {
        val state = _uiState.value
        val query = state.query.trim()
        val cloudTypes = state.selectedCloudTypes.toList()
        if (query.isBlank() || cloudTypes.isEmpty()) return

        prefs.addSearchKeyword(query)
        _uiState.update {
            it.copy(searchHistory = prefs.getSearchHistory())
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, error = null, hasSearched = true, mergedGroups = emptyList())
            }

            // 1. 优先检查本地缓存
            val cached = cache.get(query, cloudTypes)
            if (cached != null) {
                val cachedGroups = cache.toDomain(cached)
                if (cachedGroups.isNotEmpty()) {
                    _uiState.update {
                        val okMap = cachedGroups
                            .flatMap { it.items }
                            .associate { it.url to LinkState.OK }
                        it.copy(
                            isLoading = false,
                            isChecking = false,
                            totalCount = cached.totalCount,
                            mergedGroups = cachedGroups,
                            linkCheckResults = okMap,
                            error = null
                        )
                    }
                    return@launch
                }
            }

            // 2. 缓存未命中，请求 API
            repository.search(query, cloudTypes)
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            totalCount = result.total,
                            mergedGroups = result.mergedGroups,
                            error = null
                        )
                    }
                    autoCheckAndFilter(result.mergedGroups, query, cloudTypes)
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = (e as? ApiException)?.message ?: e.message ?: "搜索失败"
                        )
                    }
                }
        }
    }

    fun clearCache() {
        cache.clearAll()
    }

    fun checkUpdate() {
        if (_uiState.value.updateStatus == UpdateStatus.CHECKING) return
        viewModelScope.launch {
            _uiState.update { it.copy(updateStatus = UpdateStatus.CHECKING) }
            repository.checkUpdate()
                .onSuccess { release ->
                    val latestTag = release.tag_name.trimStart('v')
                    val current = try {
                        context.packageManager.getPackageInfo(context.packageName, 0).versionName
                            ?: "1.0.0"
                    } catch (_: Exception) {
                        "1.0.0"
                    }
                    val hasUpdate = compareVersion(latestTag, current) > 0
                    _uiState.update {
                        it.copy(
                            updateStatus = if (hasUpdate) UpdateStatus.AVAILABLE else UpdateStatus.UNAVAILABLE,
                            latestVersion = release.tag_name,
                            releaseUrl = release.html_url,
                            releaseNotes = release.body
                        )
                    }
                }
                .onFailure { e ->
                    val status = if (e is me.slarker.yunsou.data.repository.NoReleaseException)
                        UpdateStatus.UNAVAILABLE
                    else
                        UpdateStatus.ERROR
                    _uiState.update { it.copy(updateStatus = status) }
                }
        }
    }

    fun dismissUpdate() {
        _uiState.update { it.copy(updateStatus = UpdateStatus.UNCHECKED) }
    }

    /** 简单语义化版本比较，返回 >0 表示 v1 > v2 */
    private fun compareVersion(v1: String, v2: String): Int {
        val parts1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = v2.split(".").map { it.toIntOrNull() ?: 0 }
        val maxLen = maxOf(parts1.size, parts2.size)
        for (i in 0 until maxLen) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }
            if (p1 != p2) return p1 - p2
        }
        return 0
    }

    fun checkServerStatus() {
        if (_uiState.value.serverStatus == ServerStatus.CHECKING) return

        // 优先读取缓存
        val cached = prefs.getCachedServerStatus()
        if (cached != null) {
            _uiState.update { it.copy(serverStatus = cached) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(serverStatus = ServerStatus.CHECKING) }
            repository.checkHealth()
                .onSuccess { ok ->
                    val status = if (ok) ServerStatus.ONLINE else ServerStatus.OFFLINE
                    prefs.cacheServerStatus(status)
                    _uiState.update { it.copy(serverStatus = status) }
                }
                .onFailure {
                    prefs.cacheServerStatus(ServerStatus.OFFLINE)
                    _uiState.update { it.copy(serverStatus = ServerStatus.OFFLINE) }
                }
        }
    }

    private suspend fun autoCheckAndFilter(groups: List<MergedGroup>, query: String, cloudTypes: List<CloudType>) {
        val allItems = groups.flatMap { group ->
            group.items.map { Triple(group.cloudType.apiName, it, it.url) }
        }
        if (allItems.isEmpty()) return

        _uiState.update { it.copy(isChecking = true) }

        val checkItems = allItems.map { (diskType, item, _) ->
            LinkCheckItem(diskType, item.url, item.password)
        }

        coroutineScope {
            val semaphore = Semaphore(10)
            checkItems.map { item ->
                async {
                    semaphore.acquire()
                    try {
                        repository.checkLinksBatch(listOf(item))
                            .onSuccess { linkStates ->
                                _uiState.update { state ->
                                    val merged = state.linkCheckResults + linkStates
                                    val filteredGroups = state.mergedGroups.mapNotNull { group ->
                                        val validItems = group.items.filter { resource ->
                                            val s = merged[resource.url]
                                            s != LinkState.BAD && s != LinkState.UNCERTAIN
                                        }
                                        if (validItems.isNotEmpty()) {
                                            MergedGroup(group.cloudType, validItems)
                                        } else null
                                    }
                                    state.copy(
                                        linkCheckResults = merged,
                                        mergedGroups = filteredGroups,
                                        totalCount = filteredGroups.sumOf { it.items.size }
                                    )
                                }
                            }
                    } finally {
                        semaphore.release()
                    }
                }
            }.awaitAll()
        }

        // 有效性检测完成后，按画质关键词优先级排序
        _uiState.update { state ->
            val qualityKeywords = listOf("蓝光", "原盘", "4K", "1080p", "高清")
            val sortedGroups = state.mergedGroups.map { group ->
                val sorted = group.items.sortedByDescending { item ->
                    val title = item.note ?: ""
                    qualityKeywords.indexOfFirst { title.contains(it, ignoreCase = true) }
                        .let { if (it >= 0) qualityKeywords.size - it else 0 }
                }
                MergedGroup(group.cloudType, sorted)
            }
            state.copy(mergedGroups = sortedGroups)
        }

        // 缓存有效的搜索结果（数量 > 0）
        val finalState = _uiState.value
        if (finalState.totalCount > 0) {
            cache.put(query, cloudTypes, finalState.mergedGroups, finalState.totalCount)
        }

        _uiState.update { it.copy(isChecking = false) }
    }
}
