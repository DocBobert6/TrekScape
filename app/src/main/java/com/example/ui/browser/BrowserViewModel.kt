package com.example.ui.browser

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.view.View
import android.webkit.WebStorage
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.TrekScapeApp
import com.example.data.preferences.BrowserPreferences
import com.example.data.preferences.SearchEngine
import com.example.model.BrowserTab
import com.example.model.CertErrorRequest
import com.example.model.ContextMenuTarget
import com.example.model.JsDialogRequest
import com.example.model.PermissionRequestItem
import com.example.model.ReaderArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class SuggestionType {
    TOP_HIT,
    BOOKMARK,
    HISTORY,
    SEARCH
}

data class SuggestionItem(
    val title: String,
    val subtitle: String?,
    val type: SuggestionType,
    val urlOrQuery: String
)

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as TrekScapeApp).container
    val preferencesRepository = container.userPreferencesRepository
    val bookmarkRepository = container.bookmarkRepository
    val historyRepository = container.historyRepository
    val readingListRepository = container.readingListRepository
    val trackerRepository = container.trackerRepository
    val siteSettingsRepository = container.siteSettingsRepository
    val contentBlocker = container.contentBlocker
    private val searchSuggestionRepository = container.searchSuggestionRepository

    val preferences: StateFlow<BrowserPreferences> = preferencesRepository.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = BrowserPreferences()
        )

    // Tabs
    private val _tabs = MutableStateFlow<List<BrowserTab>>(listOf(BrowserTab(id = "init_tab", title = "TrekScape", url = "")))
    val tabs: StateFlow<List<BrowserTab>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow<String>("init_tab")
    val activeTabId: StateFlow<String> = _activeTabId.asStateFlow()

    private val _recentlyClosedTabs = MutableStateFlow<List<BrowserTab>>(emptyList())
    val recentlyClosedTabs: StateFlow<List<BrowserTab>> = _recentlyClosedTabs.asStateFlow()

    private val _activeGroup = MutableStateFlow("All")
    val activeGroup: StateFlow<String> = _activeGroup.asStateFlow()

    private val _tabGroups = MutableStateFlow(listOf("All", "Default", "Work", "Research", "Personal"))
    val tabGroups: StateFlow<List<String>> = _tabGroups.asStateFlow()

    private val _isPrivateMode = MutableStateFlow(false)
    val isPrivateMode: StateFlow<Boolean> = _isPrivateMode.asStateFlow()

    // Navigation & Capsule
    private val _isCapsuleFocused = MutableStateFlow(false)
    val isCapsuleFocused: StateFlow<Boolean> = _isCapsuleFocused.asStateFlow()

    private val _capsuleInputText = MutableStateFlow("")
    val capsuleInputText: StateFlow<String> = _capsuleInputText.asStateFlow()

    private val _suggestions = MutableStateFlow<List<SuggestionItem>>(emptyList())
    val suggestions: StateFlow<List<SuggestionItem>> = _suggestions.asStateFlow()

    private val _isBarsCollapsed = MutableStateFlow(false)
    val isBarsCollapsed: StateFlow<Boolean> = _isBarsCollapsed.asStateFlow()

    // Overlays / Sheets
    private val _isTabOverviewOpen = MutableStateFlow(false)
    val isTabOverviewOpen: StateFlow<Boolean> = _isTabOverviewOpen.asStateFlow()

    private val _isPageOptionsOpen = MutableStateFlow(false)
    val isPageOptionsOpen: StateFlow<Boolean> = _isPageOptionsOpen.asStateFlow()

    private val _isLibraryOpen = MutableStateFlow(false)
    val isLibraryOpen: StateFlow<Boolean> = _isLibraryOpen.asStateFlow()

    private val _libraryTab = MutableStateFlow(0) // 0: Bookmarks, 1: Reading List, 2: History
    val libraryTab: StateFlow<Int> = _libraryTab.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _isDownloadsOpen = MutableStateFlow(false)
    val isDownloadsOpen: StateFlow<Boolean> = _isDownloadsOpen.asStateFlow()

    private val _isPrivacyReportOpen = MutableStateFlow(false)
    val isPrivacyReportOpen: StateFlow<Boolean> = _isPrivacyReportOpen.asStateFlow()

    private val _isWebsiteSettingsOpen = MutableStateFlow(false)
    val isWebsiteSettingsOpen: StateFlow<Boolean> = _isWebsiteSettingsOpen.asStateFlow()

    // Reader Mode
    private val _activeReaderArticle = MutableStateFlow<ReaderArticle?>(null)
    val activeReaderArticle: StateFlow<ReaderArticle?> = _activeReaderArticle.asStateFlow()

    private val _isReaderOpen = MutableStateFlow(false)
    val isReaderOpen: StateFlow<Boolean> = _isReaderOpen.asStateFlow()

    private val _readerTheme = MutableStateFlow("sepia") // white, sepia, slate, black
    val readerTheme: StateFlow<String> = _readerTheme.asStateFlow()

    private val _readerFontSize = MutableStateFlow(18)
    val readerFontSize: StateFlow<Int> = _readerFontSize.asStateFlow()

    private val _readerFontFamily = MutableStateFlow("serif") // sans, serif, mono
    val readerFontFamily: StateFlow<String> = _readerFontFamily.asStateFlow()

    // Find on Page
    private val _isFindOnPageOpen = MutableStateFlow(false)
    val isFindOnPageOpen: StateFlow<Boolean> = _isFindOnPageOpen.asStateFlow()

    private val _findQuery = MutableStateFlow("")
    val findQuery: StateFlow<String> = _findQuery.asStateFlow()

    private val _findMatchCount = MutableStateFlow(Pair(0, 0)) // current, total
    val findMatchCount: StateFlow<Pair<Int, Int>> = _findMatchCount.asStateFlow()

    // Dialogs & Context menus
    private val _contextMenu = MutableStateFlow<ContextMenuTarget?>(null)
    val contextMenu: StateFlow<ContextMenuTarget?> = _contextMenu.asStateFlow()

    private val _jsDialog = MutableStateFlow<JsDialogRequest?>(null)
    val jsDialog: StateFlow<JsDialogRequest?> = _jsDialog.asStateFlow()

    private val _permissionRequest = MutableStateFlow<PermissionRequestItem?>(null)
    val permissionRequest: StateFlow<PermissionRequestItem?> = _permissionRequest.asStateFlow()

    private val _certError = MutableStateFlow<CertErrorRequest?>(null)
    val certError: StateFlow<CertErrorRequest?> = _certError.asStateFlow()

    // Fullscreen Custom View (video)
    private val _customView = MutableStateFlow<View?>(null)
    val customView: StateFlow<View?> = _customView.asStateFlow()

    // Tab Action / Trigger
    private val _navigationTrigger = MutableStateFlow<String?>(null)
    val navigationTrigger: StateFlow<String?> = _navigationTrigger.asStateFlow()

    fun getActiveTab(): BrowserTab? {
        val currentTabs = _tabs.value
        val id = _activeTabId.value
        return currentTabs.find { it.id == id } ?: currentTabs.firstOrNull()
    }

    fun openTab(url: String = "", isPrivate: Boolean = _isPrivateMode.value, group: String = "Default"): BrowserTab {
        val newTab = BrowserTab(
            title = if (url.isBlank()) "TrekScape" else url,
            url = url,
            isPrivate = isPrivate,
            groupName = group
        )
        val list = _tabs.value.toMutableList()
        list.add(newTab)
        _tabs.value = list
        _activeTabId.value = newTab.id
        _isTabOverviewOpen.value = false
        if (url.isNotBlank()) {
            _navigationTrigger.value = url
        }
        return newTab
    }

    fun selectTab(id: String) {
        val tab = _tabs.value.find { it.id == id }
        if (tab != null) {
            _activeTabId.value = id
            _isPrivateMode.value = tab.isPrivate
            _isTabOverviewOpen.value = false
        }
    }

    fun closeTab(id: String) {
        val list = _tabs.value.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index != -1) {
            val removed = list.removeAt(index)
            if (!removed.isPrivate && removed.url.isNotBlank()) {
                val recent = _recentlyClosedTabs.value.toMutableList()
                recent.add(0, removed)
                if (recent.size > 15) recent.removeAt(recent.lastIndex)
                _recentlyClosedTabs.value = recent
            }
            if (list.isEmpty()) {
                // Always have at least one tab
                val fallback = BrowserTab(title = "TrekScape", isPrivate = _isPrivateMode.value)
                list.add(fallback)
                _activeTabId.value = fallback.id
            } else if (_activeTabId.value == id) {
                val nextIndex = if (index < list.size) index else list.lastIndex
                _activeTabId.value = list[nextIndex].id
            }
            _tabs.value = list
        }
    }

    fun duplicateTab(tab: BrowserTab) {
        openTab(tab.url, tab.isPrivate, tab.groupName)
    }

    fun togglePinTab(id: String) {
        _tabs.value = _tabs.value.map {
            if (it.id == id) it.copy(isPinned = !it.isPinned) else it
        }
    }

    fun moveTabToGroup(id: String, newGroup: String) {
        _tabs.value = _tabs.value.map {
            if (it.id == id) it.copy(groupName = newGroup) else it
        }
    }

    fun closeOtherTabs(exceptId: String) {
        val kept = _tabs.value.filter { it.id == exceptId || it.isPinned }
        _tabs.value = if (kept.isNotEmpty()) kept else listOf(BrowserTab(title = "TrekScape"))
        _activeTabId.value = exceptId
    }

    fun closeAllTabs(isPrivateOnly: Boolean = false) {
        val remaining = _tabs.value.filter {
            if (isPrivateOnly) !it.isPrivate else it.isPinned
        }
        if (remaining.isEmpty()) {
            val newTab = BrowserTab(title = "TrekScape", isPrivate = _isPrivateMode.value)
            _tabs.value = listOf(newTab)
            _activeTabId.value = newTab.id
        } else {
            _tabs.value = remaining
            _activeTabId.value = remaining.first().id
        }
    }

    fun restoreRecentlyClosedTab(tab: BrowserTab) {
        val recent = _recentlyClosedTabs.value.toMutableList()
        recent.remove(tab)
        _recentlyClosedTabs.value = recent
        openTab(tab.url, tab.isPrivate, tab.groupName)
    }

    fun togglePrivateMode() {
        val newPrivateState = !_isPrivateMode.value
        _isPrivateMode.value = newPrivateState
        val existingPrivateTab = _tabs.value.find { it.isPrivate == newPrivateState }
        if (existingPrivateTab != null) {
            _activeTabId.value = existingPrivateTab.id
        } else {
            openTab(url = "", isPrivate = newPrivateState)
        }
    }

    fun selectGroup(group: String) {
        _activeGroup.value = group
    }

    fun addGroup(name: String) {
        if (name.isNotBlank() && !_tabGroups.value.contains(name)) {
            _tabGroups.value = _tabGroups.value + name
        }
    }

    // Navigation logic
    fun submitInput(raw: String) {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return
        val finalUrl = resolveSearchOrUrl(trimmed)
        _capsuleInputText.value = ""
        _isCapsuleFocused.value = false
        _suggestions.value = emptyList()
        loadUrl(finalUrl)
    }

    fun loadUrl(url: String) {
        val active = getActiveTab()
        if (active != null) {
            active.url = url
            _navigationTrigger.value = url
        }
    }

    fun clearNavigationTrigger() {
        _navigationTrigger.value = null
    }

    fun resolveSearchOrUrl(input: String): String {
        val trimmed = input.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("about:") || trimmed.startsWith("file:")) {
            return trimmed
        }
        val isDomainLike = !trimmed.contains(" ") && (trimmed.contains(".") || trimmed.startsWith("localhost"))
        return if (isDomainLike) {
            "https://$trimmed"
        } else {
            val engine = preferences.value.searchEngine
            String.format(engine.searchUrl, java.net.URLEncoder.encode(trimmed, "UTF-8"))
        }
    }

    fun updateCapsuleInput(text: String) {
        _capsuleInputText.value = text
        if (text.isBlank()) {
            _suggestions.value = emptyList()
            return
        }
        viewModelScope.launch {
            val list = mutableListOf<SuggestionItem>()

            // Top hit
            val topHitUrl = resolveSearchOrUrl(text)
            list.add(
                SuggestionItem(
                    title = text,
                    subtitle = topHitUrl,
                    type = SuggestionType.TOP_HIT,
                    urlOrQuery = topHitUrl
                )
            )

            // Search suggestions if enabled
            if (preferences.value.searchSuggestionsEnabled) {
                val engineSuggestUrl = preferences.value.searchEngine.suggestUrl
                val engineSuggestions = searchSuggestionRepository.fetchSuggestions(text, engineSuggestUrl)
                engineSuggestions.take(4).forEach { suggest ->
                    list.add(
                        SuggestionItem(
                            title = suggest,
                            subtitle = "Search ${preferences.value.searchEngine.displayName}",
                            type = SuggestionType.SEARCH,
                            urlOrQuery = resolveSearchOrUrl(suggest)
                        )
                    )
                }
            }
            _suggestions.value = list
        }
    }

    fun setCapsuleFocused(focused: Boolean) {
        _isCapsuleFocused.value = focused
        if (focused) {
            val currentUrl = getActiveTab()?.url.orEmpty()
            _capsuleInputText.value = currentUrl
            updateCapsuleInput(currentUrl)
        } else {
            _suggestions.value = emptyList()
        }
    }

    fun setBarsCollapsed(collapsed: Boolean) {
        _isBarsCollapsed.value = collapsed
    }

    // Context Menu
    fun showContextMenu(target: ContextMenuTarget) {
        _contextMenu.value = target
    }

    fun dismissContextMenu() {
        _contextMenu.value = null
    }

    // Dialogs
    fun showJsDialog(request: JsDialogRequest) {
        _jsDialog.value = request
    }

    fun dismissJsDialog() {
        _jsDialog.value = null
    }

    fun showPermissionRequest(item: PermissionRequestItem) {
        _permissionRequest.value = item
    }

    fun dismissPermissionRequest() {
        _permissionRequest.value = null
    }

    fun showCertError(request: CertErrorRequest) {
        _certError.value = request
    }

    fun dismissCertError() {
        _certError.value = null
    }

    // Reader Mode
    fun openReaderMode(article: ReaderArticle) {
        _activeReaderArticle.value = article
        _isReaderOpen.value = true
        _isPageOptionsOpen.value = false
    }

    fun closeReaderMode() {
        _isReaderOpen.value = false
    }

    fun setReaderTheme(theme: String) {
        _readerTheme.value = theme
    }

    fun setReaderFontSize(size: Int) {
        _readerFontSize.value = size.coerceIn(12, 32)
    }

    fun setReaderFontFamily(family: String) {
        _readerFontFamily.value = family
    }

    // Find on Page
    fun openFindOnPage() {
        _isFindOnPageOpen.value = true
        _isPageOptionsOpen.value = false
    }

    fun closeFindOnPage() {
        _isFindOnPageOpen.value = false
        _findQuery.value = ""
        _findMatchCount.value = Pair(0, 0)
    }

    fun updateFindQuery(query: String) {
        _findQuery.value = query
    }

    fun updateFindMatchCount(current: Int, total: Int) {
        _findMatchCount.value = Pair(current, total)
    }

    // Fullscreen View
    fun setCustomView(view: View?) {
        _customView.value = view
    }

    // Overlays
    fun setTabOverviewOpen(open: Boolean) {
        _isTabOverviewOpen.value = open
    }

    fun setPageOptionsOpen(open: Boolean) {
        _isPageOptionsOpen.value = open
    }

    fun openLibrary(tabIndex: Int = 0) {
        _libraryTab.value = tabIndex
        _isLibraryOpen.value = true
        _isPageOptionsOpen.value = false
    }

    fun closeLibrary() {
        _isLibraryOpen.value = false
    }

    fun setSettingsOpen(open: Boolean) {
        _isSettingsOpen.value = open
        _isPageOptionsOpen.value = false
    }

    fun setDownloadsOpen(open: Boolean) {
        _isDownloadsOpen.value = open
    }

    fun setPrivacyReportOpen(open: Boolean) {
        _isPrivacyReportOpen.value = open
        _isPageOptionsOpen.value = false
    }

    fun setWebsiteSettingsOpen(open: Boolean) {
        _isWebsiteSettingsOpen.value = open
        _isPageOptionsOpen.value = false
    }

    // Tab Updates
    fun updateActiveTabState(
        title: String? = null,
        url: String? = null,
        canGoBack: Boolean? = null,
        canGoForward: Boolean? = null,
        isLoading: Boolean? = null,
        progress: Float? = null,
        favicon: Bitmap? = null,
        isSecure: Boolean? = null,
        hasCertError: Boolean? = null,
        thumbnail: Bitmap? = null
    ) {
        val currentId = _activeTabId.value
        _tabs.value = _tabs.value.map { tab ->
            if (tab.id == currentId) {
                tab.copy(
                    title = title ?: tab.title,
                    url = url ?: tab.url,
                    canGoBack = canGoBack ?: tab.canGoBack,
                    canGoForward = canGoForward ?: tab.canGoForward,
                    isLoading = isLoading ?: tab.isLoading,
                    progress = progress ?: tab.progress,
                    favicon = favicon ?: tab.favicon,
                    isSecure = isSecure ?: tab.isSecure,
                    hasCertError = hasCertError ?: tab.hasCertError,
                    thumbnailBitmap = thumbnail ?: tab.thumbnailBitmap
                )
            } else {
                tab
            }
        }

        // Record history if url loaded successfully and not private
        if (url != null && !url.startsWith("about:") && isLoading == false) {
            val active = getActiveTab()
            if (active != null && !active.isPrivate) {
                viewModelScope.launch {
                    historyRepository.recordVisit(title ?: active.title, url)
                }
            }
        }
    }

    fun toggleDesktopMode() {
        val active = getActiveTab() ?: return
        val newMode = !active.desktopMode
        _tabs.value = _tabs.value.map {
            if (it.id == active.id) it.copy(desktopMode = newMode) else it
        }
        _navigationTrigger.value = active.url
    }

    fun adjustZoom(deltaPercent: Int) {
        val active = getActiveTab() ?: return
        val newZoom = (active.zoomPercent + deltaPercent).coerceIn(50, 200)
        _tabs.value = _tabs.value.map {
            if (it.id == active.id) it.copy(zoomPercent = newZoom) else it
        }
    }

    fun switchTabByOffset(offset: Int) {
        val list = _tabs.value
        val currentIndex = list.indexOfFirst { it.id == _activeTabId.value }
        if (currentIndex != -1 && list.isNotEmpty()) {
            val nextIndex = (currentIndex + offset + list.size) % list.size
            selectTab(list[nextIndex].id)
        }
    }
}
