package me.slarker.yunsou.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import me.slarker.yunsou.ui.search.ServerStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("pansou_prefs", Context.MODE_PRIVATE)

    fun getBaseUrl(): String =
        prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL

    fun setBaseUrl(url: String) {
        prefs.edit().putString(KEY_BASE_URL, url.trimEnd('/') + "/").apply()
    }

    fun getSearchHistory(): List<String> =
        prefs.getString(KEY_SEARCH_HISTORY, null)
            ?.split("|")
            ?.filter { it.isNotBlank() }
            ?: emptyList()

    fun addSearchKeyword(keyword: String) {
        val history = getSearchHistory().toMutableList()
        history.remove(keyword)
        history.add(0, keyword)
        if (history.size > MAX_HISTORY) {
            history.removeAt(history.lastIndex)
        }
        prefs.edit().putString(KEY_SEARCH_HISTORY, history.joinToString("|")).apply()
    }

    fun getCachedServerStatus(): ServerStatus? {
        val cachedAt = prefs.getLong(KEY_SERVER_STATUS_TIME, 0L)
        if (cachedAt == 0L) return null
        val elapsed = System.currentTimeMillis() - cachedAt
        if (elapsed > STATUS_CACHE_DURATION_MS) return null
        val statusName = prefs.getString(KEY_SERVER_STATUS, null) ?: return null
        return try {
            ServerStatus.valueOf(statusName)
        } catch (_: Exception) {
            null
        }
    }

    fun cacheServerStatus(status: ServerStatus) {
        prefs.edit()
            .putString(KEY_SERVER_STATUS, status.name)
            .putLong(KEY_SERVER_STATUS_TIME, System.currentTimeMillis())
            .apply()
    }

    fun clearCachedServerStatus() {
        prefs.edit()
            .remove(KEY_SERVER_STATUS)
            .remove(KEY_SERVER_STATUS_TIME)
            .apply()
    }

    companion object {
        const val DEFAULT_BASE_URL = "https://pan.slarker.me/"
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_SEARCH_HISTORY = "search_history"
        private const val KEY_SERVER_STATUS = "server_status"
        private const val KEY_SERVER_STATUS_TIME = "server_status_time"
        private const val MAX_HISTORY = 10
        private const val STATUS_CACHE_DURATION_MS = 3600_000L // 1 hour
    }
}
