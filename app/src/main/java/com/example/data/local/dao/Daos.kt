package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.HistoryEntity
import com.example.data.local.entity.ReadingListEntity
import com.example.data.local.entity.BlockedTrackerEntity
import com.example.data.local.entity.SiteSettingsEntity
import com.example.data.local.entity.TabEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE folder = :folder ORDER BY timestamp DESC")
    fun getBookmarksByFolder(folder: String): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE url = :url LIMIT 1")
    suspend fun getBookmarkByUrl(url: String): BookmarkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Update
    suspend fun updateBookmark(bookmark: BookmarkEntity)

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: Long)
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history WHERE title LIKE '%' || :query || '%' OR url LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchHistory(query: String): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history ORDER BY visitCount DESC, timestamp DESC LIMIT 8")
    fun getFrequentlyVisited(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history WHERE url = :url LIMIT 1")
    suspend fun getHistoryItemByUrl(url: String): HistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity): Long

    @Query("UPDATE history SET visitCount = visitCount + 1, timestamp = :timestamp WHERE id = :id")
    suspend fun incrementVisitCount(id: Long, timestamp: Long)

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM history WHERE timestamp >= :sinceTimestamp")
    suspend fun clearHistorySince(sinceTimestamp: Long)

    @Query("DELETE FROM history")
    suspend fun clearAllHistory()
}

@Dao
interface ReadingListDao {
    @Query("SELECT * FROM reading_list ORDER BY timestamp DESC")
    fun getAllReadingList(): Flow<List<ReadingListEntity>>

    @Query("SELECT * FROM reading_list WHERE isRead = 0 ORDER BY timestamp DESC")
    fun getUnreadReadingList(): Flow<List<ReadingListEntity>>

    @Query("SELECT * FROM reading_list WHERE url = :url LIMIT 1")
    suspend fun getByUrl(url: String): ReadingListEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadingItem(item: ReadingListEntity): Long

    @Query("UPDATE reading_list SET isRead = :isRead WHERE id = :id")
    suspend fun updateReadStatus(id: Long, isRead: Boolean)

    @Delete
    suspend fun deleteReadingItem(item: ReadingListEntity)

    @Query("DELETE FROM reading_list WHERE id = :id")
    suspend fun deleteReadingItemById(id: Long)
}

data class TrackerSummary(
    val trackerDomain: String,
    val count: Int
)

data class SiteTrackerSummary(
    val siteDomain: String,
    val count: Int
)

@Dao
interface TrackerDao {
    @Query("SELECT COUNT(*) FROM blocked_trackers")
    fun getTotalBlockedCount(): Flow<Int>

    @Query("SELECT trackerDomain, COUNT(*) as count FROM blocked_trackers GROUP BY trackerDomain ORDER BY count DESC LIMIT 10")
    fun getTopTrackers(): Flow<List<TrackerSummary>>

    @Query("SELECT siteDomain, COUNT(*) as count FROM blocked_trackers GROUP BY siteDomain ORDER BY count DESC LIMIT 10")
    fun getTopSitesWithTrackers(): Flow<List<SiteTrackerSummary>>

    @Query("SELECT COUNT(*) FROM blocked_trackers WHERE siteDomain = :siteDomain")
    fun getBlockedCountForSite(siteDomain: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedTracker(tracker: BlockedTrackerEntity)

    @Query("DELETE FROM blocked_trackers")
    suspend fun clearAllTrackerStats()
}

@Dao
interface SiteSettingsDao {
    @Query("SELECT * FROM site_settings WHERE domain = :domain LIMIT 1")
    suspend fun getSettings(domain: String): SiteSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: SiteSettingsEntity)

    @Query("DELETE FROM site_settings WHERE domain = :domain")
    suspend fun deleteSettings(domain: String)
}

@Dao
interface TabDao {
    @Query("SELECT * FROM browser_tabs ORDER BY position ASC")
    fun getAllTabs(): Flow<List<TabEntity>>

    @Query("SELECT * FROM browser_tabs WHERE groupName = :groupName ORDER BY position ASC")
    fun getTabsByGroup(groupName: String): Flow<List<TabEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTab(tab: TabEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTabs(tabs: List<TabEntity>)

    @Query("DELETE FROM browser_tabs WHERE id = :id")
    suspend fun deleteTabById(id: String)

    @Query("DELETE FROM browser_tabs WHERE isPrivate = 1")
    suspend fun clearPrivateTabs()

    @Query("DELETE FROM browser_tabs")
    suspend fun clearAllTabs()
}
