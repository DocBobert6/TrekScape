package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.local.dao.BookmarkDao
import com.example.data.local.dao.HistoryDao
import com.example.data.local.dao.ReadingListDao
import com.example.data.local.dao.SiteSettingsDao
import com.example.data.local.dao.SiteTrackerSummary
import com.example.data.local.dao.TrackerDao
import com.example.data.local.dao.TrackerSummary
import com.example.data.local.entity.BlockedTrackerEntity
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.HistoryEntity
import com.example.data.local.entity.ReadingListEntity
import com.example.data.local.entity.SiteSettingsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class BookmarkRepository(private val bookmarkDao: BookmarkDao) {
    val bookmarks: Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()

    suspend fun addBookmark(title: String, url: String, folder: String = "Favorites") = withContext(Dispatchers.IO) {
        val existing = bookmarkDao.getBookmarkByUrl(url)
        if (existing == null) {
            bookmarkDao.insertBookmark(
                BookmarkEntity(
                    title = title.ifBlank { url },
                    url = url,
                    folder = folder
                )
            )
        }
    }

    suspend fun removeBookmark(bookmark: BookmarkEntity) = withContext(Dispatchers.IO) {
        bookmarkDao.deleteBookmark(bookmark)
    }

    suspend fun removeBookmarkById(id: Long) = withContext(Dispatchers.IO) {
        bookmarkDao.deleteBookmarkById(id)
    }

    suspend fun isBookmarked(url: String): Boolean = withContext(Dispatchers.IO) {
        bookmarkDao.getBookmarkByUrl(url) != null
    }
}

class HistoryRepository(private val historyDao: HistoryDao) {
    val allHistory: Flow<List<HistoryEntity>> = historyDao.getAllHistory()
    val frequentlyVisited: Flow<List<HistoryEntity>> = historyDao.getFrequentlyVisited()

    fun search(query: String): Flow<List<HistoryEntity>> = historyDao.searchHistory(query)

    suspend fun recordVisit(title: String, url: String) = withContext(Dispatchers.IO) {
        if (url.isBlank() || url.startsWith("about:") || url.startsWith("data:")) return@withContext
        val existing = historyDao.getHistoryItemByUrl(url)
        if (existing != null) {
            historyDao.incrementVisitCount(existing.id, System.currentTimeMillis())
        } else {
            historyDao.insertHistory(
                HistoryEntity(
                    title = title.ifBlank { url },
                    url = url
                )
            )
        }
    }

    suspend fun deleteHistory(id: Long) = withContext(Dispatchers.IO) {
        historyDao.deleteHistoryById(id)
    }

    suspend fun clearHistoryLastHour() = withContext(Dispatchers.IO) {
        val oneHourAgo = System.currentTimeMillis() - 3600 * 1000
        historyDao.clearHistorySince(oneHourAgo)
    }

    suspend fun clearHistoryToday() = withContext(Dispatchers.IO) {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        historyDao.clearHistorySince(calendar.timeInMillis)
    }

    suspend fun clearAllHistory() = withContext(Dispatchers.IO) {
        historyDao.clearAllHistory()
    }
}

class ReadingListRepository(private val readingListDao: ReadingListDao) {
    val readingList: Flow<List<ReadingListEntity>> = readingListDao.getAllReadingList()
    val unreadItems: Flow<List<ReadingListEntity>> = readingListDao.getUnreadReadingList()

    suspend fun addToReadingList(
        title: String,
        url: String,
        byline: String? = null,
        contentHtml: String? = null,
        readingTimeMinutes: Int = 1,
        wordCount: Int = 0
    ) = withContext(Dispatchers.IO) {
        val existing = readingListDao.getByUrl(url)
        if (existing == null) {
            readingListDao.insertReadingItem(
                ReadingListEntity(
                    title = title.ifBlank { url },
                    url = url,
                    byline = byline,
                    contentHtml = contentHtml,
                    readingTimeMinutes = readingTimeMinutes,
                    wordCount = wordCount,
                    isRead = false
                )
            )
        }
    }

    suspend fun toggleReadStatus(id: Long, currentStatus: Boolean) = withContext(Dispatchers.IO) {
        readingListDao.updateReadStatus(id, !currentStatus)
    }

    suspend fun removeItem(id: Long) = withContext(Dispatchers.IO) {
        readingListDao.deleteReadingItemById(id)
    }
}

class TrackerRepository(private val trackerDao: TrackerDao) {
    val totalBlockedCount: Flow<Int> = trackerDao.getTotalBlockedCount()
    val topTrackers: Flow<List<TrackerSummary>> = trackerDao.getTopTrackers()
    val topSites: Flow<List<SiteTrackerSummary>> = trackerDao.getTopSitesWithTrackers()

    fun getBlockedCountForSite(siteDomain: String): Flow<Int> = trackerDao.getBlockedCountForSite(siteDomain)

    suspend fun recordBlockedTracker(siteDomain: String, trackerDomain: String) = withContext(Dispatchers.IO) {
        trackerDao.insertBlockedTracker(
            BlockedTrackerEntity(
                siteDomain = siteDomain,
                trackerDomain = trackerDomain
            )
        )
    }

    suspend fun clearStats() = withContext(Dispatchers.IO) {
        trackerDao.clearAllTrackerStats()
    }
}

class SiteSettingsRepository(private val siteSettingsDao: SiteSettingsDao) {
    suspend fun getSettings(domain: String): SiteSettingsEntity = withContext(Dispatchers.IO) {
        siteSettingsDao.getSettings(domain) ?: SiteSettingsEntity(domain = domain)
    }

    suspend fun updateSettings(settings: SiteSettingsEntity) = withContext(Dispatchers.IO) {
        siteSettingsDao.saveSettings(settings)
    }
}

class SearchSuggestionRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .build()

    suspend fun fetchSuggestions(query: String, engineUrl: String): List<String> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        try {
            val encoded = URLEncoder.encode(query.trim(), "UTF-8")
            val url = String.format(engineUrl, encoded)
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                parseSuggestionsJson(body)
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseSuggestionsJson(json: String): List<String> {
        val list = mutableListOf<String>()
        try {
            val root = JSONArray(json)
            if (root.length() > 1) {
                val second = root.get(1)
                if (second is JSONArray) {
                    for (i in 0 until second.length()) {
                        val item = second.get(i)
                        if (item is String) {
                            list.add(item)
                        } else if (item is org.json.JSONObject && item.has("phrase")) {
                            list.add(item.getString("phrase"))
                        }
                    }
                }
            }
        } catch (_: Exception) {
            try {
                val root = JSONArray(json)
                for (i in 0 until root.length()) {
                    val obj = root.optJSONObject(i)
                    if (obj != null && obj.has("phrase")) {
                        list.add(obj.getString("phrase"))
                    }
                }
            } catch (_: Exception) {}
        }
        return list
    }
}

data class StripResult(
    val strippedUrl: String,
    val removedCount: Int
)

object TrackingParamStripper {
    // Known tracking parameters to strip
    private val TRACKING_PARAMS = setOf(
        "fbclid", "gclid", "msclkid", "mc_eid", "igshid", "dclid", "yclid",
        "wickedid", "twclid", "hsctstracking", "mkt_tok", "vero_id",
        "sc_customer", "ad_id", "campaign_id", "pixel_id"
    )

    // Protected auth and functional parameters that must NEVER be stripped
    private val PROTECTED_PARAMS = setOf(
        "code", "state", "response_type", "redirect_uri", "client_id",
        "scope", "nonce", "access_token", "id_token", "token_type",
        "authuser", "login_hint", "prompt", "session_state", "hd", "continue"
    )

    fun stripTrackingParameters(url: String): StripResult {
        if (!url.contains("?")) return StripResult(url, 0)
        try {
            val qIndex = url.indexOf('?')
            val base = url.substring(0, qIndex)
            val queryWithHash = url.substring(qIndex + 1)
            val hashIndex = queryWithHash.indexOf('#')
            val queryString = if (hashIndex != -1) queryWithHash.substring(0, hashIndex) else queryWithHash
            val hashFragment = if (hashIndex != -1) queryWithHash.substring(hashIndex) else ""

            val pairs = queryString.split("&")
            var removed = 0
            val keptPairs = mutableListOf<String>()

            for (pair in pairs) {
                if (pair.isBlank()) continue
                val eqIndex = pair.indexOf('=')
                val key = if (eqIndex != -1) pair.substring(0, eqIndex) else pair
                val lowerKey = key.lowercase()

                val isTracking = (lowerKey.startsWith("utm_") || TRACKING_PARAMS.contains(lowerKey)) && !PROTECTED_PARAMS.contains(lowerKey)
                if (isTracking) {
                    removed++
                } else {
                    keptPairs.add(pair)
                }
            }

            if (removed == 0) return StripResult(url, 0)

            val newQuery = if (keptPairs.isNotEmpty()) "?" + keptPairs.joinToString("&") else ""
            val newUrl = base + newQuery + hashFragment
            return StripResult(newUrl, removed)
        } catch (_: Exception) {
            return StripResult(url, 0)
        }
    }
}

class ContentBlocker(
    private val context: Context,
    private val trackerRepository: TrackerRepository
) {
    private val blockedDomains = HashSet<String>()

    init {
        loadBlocklist()
    }

    private fun loadBlocklist() {
        try {
            val json = context.assets.open("blocklist.json").bufferedReader().use { it.readText() }
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                blockedDomains.add(array.getString(i).lowercase())
            }
        } catch (_: Exception) {
            blockedDomains.addAll(
                listOf(
                    "doubleclick.net", "google-analytics.com", "googlesyndication.com",
                    "connect.facebook.net", "scorecardresearch.com", "outbrain.com",
                    "taboola.com", "hotjar.com", "branch.io", "clarity.ms"
                )
            )
        }
    }

    fun shouldBlock(requestHost: String, pageHost: String): Boolean {
        val lowerHost = requestHost.lowercase()
        for (blocked in blockedDomains) {
            if (lowerHost == blocked || lowerHost.endsWith(".$blocked")) {
                return true
            }
        }
        return false
    }

    suspend fun recordBlocked(pageHost: String, requestHost: String) {
        trackerRepository.recordBlockedTracker(pageHost, requestHost)
    }
}
