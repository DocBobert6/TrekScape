package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.BookmarkDao
import com.example.data.local.dao.HistoryDao
import com.example.data.local.dao.ReadingListDao
import com.example.data.local.dao.SiteSettingsDao
import com.example.data.local.dao.TabDao
import com.example.data.local.dao.TrackerDao
import com.example.data.local.entity.BlockedTrackerEntity
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.HistoryEntity
import com.example.data.local.entity.ReadingListEntity
import com.example.data.local.entity.SiteSettingsEntity
import com.example.data.local.entity.TabEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BookmarkEntity::class,
        HistoryEntity::class,
        ReadingListEntity::class,
        BlockedTrackerEntity::class,
        SiteSettingsEntity::class,
        TabEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TrekScapeDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun historyDao(): HistoryDao
    abstract fun readingListDao(): ReadingListDao
    abstract fun trackerDao(): TrackerDao
    abstract fun siteSettingsDao(): SiteSettingsDao
    abstract fun tabDao(): TabDao

    companion object {
        @Volatile
        private var INSTANCE: TrekScapeDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): TrekScapeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TrekScapeDatabase::class.java,
                    "trekscape_browser.db"
                ).addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialFavorites(database.bookmarkDao())
                    }
                }
            }

            private suspend fun populateInitialFavorites(dao: BookmarkDao) {
                val defaults = listOf(
                    BookmarkEntity(
                        title = "DuckDuckGo",
                        url = "https://duckduckgo.com",
                        folder = "Favorites"
                    ),
                    BookmarkEntity(
                        title = "Wikipedia",
                        url = "https://en.wikipedia.org",
                        folder = "Favorites"
                    ),
                    BookmarkEntity(
                        title = "OpenStreetMap",
                        url = "https://www.openstreetmap.org",
                        folder = "Favorites"
                    ),
                    BookmarkEntity(
                        title = "NASA",
                        url = "https://www.nasa.gov",
                        folder = "Favorites"
                    ),
                    BookmarkEntity(
                        title = "Hacker News",
                        url = "https://news.ycombinator.com",
                        folder = "Favorites"
                    ),
                    BookmarkEntity(
                        title = "Project Gutenberg",
                        url = "https://www.gutenberg.org",
                        folder = "Favorites"
                    )
                )
                defaults.forEach { dao.insertBookmark(it) }
            }
        }
    }
}
