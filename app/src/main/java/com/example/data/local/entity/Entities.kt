package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val url: String,
    val folder: String = "Favorites",
    val faviconUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis(),
    val visitCount: Int = 1
)

@Entity(tableName = "reading_list")
data class ReadingListEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val url: String,
    val byline: String? = null,
    val contentHtml: String? = null,
    val readingTimeMinutes: Int = 1,
    val wordCount: Int = 0,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "blocked_trackers")
data class BlockedTrackerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val siteDomain: String,
    val trackerDomain: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "site_settings")
data class SiteSettingsEntity(
    @PrimaryKey
    val domain: String,
    val zoomLevel: Float = 1.0f,
    val desktopMode: Boolean = false,
    val contentBlocking: Boolean = true,
    val darkPages: Boolean = false,
    val cameraAllowed: Boolean? = null,
    val micAllowed: Boolean? = null,
    val locationAllowed: Boolean? = null
)

@Entity(tableName = "browser_tabs")
data class TabEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val url: String,
    val groupName: String = "Default",
    val isPrivate: Boolean = false,
    val isPinned: Boolean = false,
    val position: Int = 0,
    val lastAccessed: Long = System.currentTimeMillis()
)
