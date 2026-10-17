package com.example

import android.app.Application
import com.example.data.local.TrekScapeDatabase
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.BookmarkRepository
import com.example.data.repository.ContentBlocker
import com.example.data.repository.HistoryRepository
import com.example.data.repository.ReadingListRepository
import com.example.data.repository.SearchSuggestionRepository
import com.example.data.repository.SiteSettingsRepository
import com.example.data.repository.TrackerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class BrowserAppContainer(val application: Application) {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val database: TrekScapeDatabase by lazy {
        TrekScapeDatabase.getDatabase(application, applicationScope)
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(application)
    }

    val bookmarkRepository: BookmarkRepository by lazy {
        BookmarkRepository(database.bookmarkDao())
    }

    val historyRepository: HistoryRepository by lazy {
        HistoryRepository(database.historyDao())
    }

    val readingListRepository: ReadingListRepository by lazy {
        ReadingListRepository(database.readingListDao())
    }

    val trackerRepository: TrackerRepository by lazy {
        TrackerRepository(database.trackerDao())
    }

    val siteSettingsRepository: SiteSettingsRepository by lazy {
        SiteSettingsRepository(database.siteSettingsDao())
    }

    val searchSuggestionRepository: SearchSuggestionRepository by lazy {
        SearchSuggestionRepository()
    }

    val contentBlocker: ContentBlocker by lazy {
        ContentBlocker(application, trackerRepository)
    }
}

class TrekScapeApp : Application() {
    lateinit var container: BrowserAppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = BrowserAppContainer(this)
    }
}
