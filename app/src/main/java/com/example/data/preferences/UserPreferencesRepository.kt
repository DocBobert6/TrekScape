package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "trekscape_settings")

enum class SearchEngine(val displayName: String, val searchUrl: String, val suggestUrl: String) {
    DUCKDUCKGO(
        "DuckDuckGo",
        "https://duckduckgo.com/?q=%s",
        "https://duckduckgo.com/ac/?q=%s&type=list"
    ),
    GOOGLE(
        "Google",
        "https://www.google.com/search?q=%s",
        "https://suggestqueries.google.com/complete/search?client=chrome&q=%s"
    ),
    BING(
        "Bing",
        "https://www.bing.com/search?q=%s",
        "https://api.bing.com/osjson.aspx?query=%s"
    ),
    ECOSIA(
        "Ecosia",
        "https://www.ecosia.org/search?q=%s",
        "https://ac.ecosia.org/autocomplete?q=%s&type=list"
    ),
    BRAVE(
        "Brave",
        "https://search.brave.com/search?q=%s",
        "https://search.brave.com/api/suggest?q=%s"
    )
}

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class ToolbarLayout(val displayName: String, val description: String) {
    COMPACT("Compact", "One slim floating row with domain capsule, back button, and more options"),
    BOTTOM("Bottom", "Floating address capsule stacked above a full navigation toolbar"),
    TOP("Top", "Floating address capsule at top with navigation toolbar floating at bottom")
}

enum class FingerprintingProtection(val displayName: String, val description: String) {
    ALL("All Browsing", "Active across standard and private tabs"),
    PRIVATE_ONLY("Private Browsing Only", "Only applied in private mode tabs"),
    OFF("Off", "Fingerprinting protection disabled")
}

data class BrowserPreferences(
    val searchEngine: SearchEngine = SearchEngine.DUCKDUCKGO,
    val searchSuggestionsEnabled: Boolean = true,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val toolbarLayout: ToolbarLayout = ToolbarLayout.COMPACT,
    val darkPagesEnabled: Boolean = false,
    val contentBlockerEnabled: Boolean = true,
    val httpsOnlyEnabled: Boolean = true,
    val blockThirdPartyCookies: Boolean = true,
    val stripTrackingParams: Boolean = true,
    val fingerprintingProtection: FingerprintingProtection = FingerprintingProtection.ALL,
    val disabledProtectionDomains: Set<String> = emptySet(),
    val showFavorites: Boolean = true,
    val showFrequentlyVisited: Boolean = true,
    val showReadingList: Boolean = true,
    val showPrivacyCard: Boolean = true,
    val openLinksInBackground: Boolean = false,
    val autoCloseTabs: String = "never",
    val paramsStrippedCount: Int = 0,
    val fingerprintNeutralizedCount: Int = 0
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val SEARCH_ENGINE = stringPreferencesKey("search_engine")
        val SEARCH_SUGGESTIONS = booleanPreferencesKey("search_suggestions")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val TOOLBAR_LAYOUT = stringPreferencesKey("toolbar_layout")
        val DARK_PAGES = booleanPreferencesKey("dark_pages")
        val CONTENT_BLOCKER = booleanPreferencesKey("content_blocker")
        val HTTPS_ONLY = booleanPreferencesKey("https_only")
        val BLOCK_3RD_PARTY_COOKIES = booleanPreferencesKey("block_3rd_party_cookies")
        val STRIP_TRACKING_PARAMS = booleanPreferencesKey("strip_tracking_params")
        val FINGERPRINTING_PROTECTION = stringPreferencesKey("fingerprinting_protection")
        val DISABLED_PROTECTIONS = stringSetPreferencesKey("disabled_protections")
        val SHOW_FAVORITES = booleanPreferencesKey("show_favorites")
        val SHOW_FREQUENTLY_VISITED = booleanPreferencesKey("show_frequently_visited")
        val SHOW_READING_LIST = booleanPreferencesKey("show_reading_list")
        val SHOW_PRIVACY_CARD = booleanPreferencesKey("show_privacy_card")
        val OPEN_IN_BACKGROUND = booleanPreferencesKey("open_in_background")
        val AUTO_CLOSE_TABS = stringPreferencesKey("auto_close_tabs")
        val PARAMS_STRIPPED_COUNT = intPreferencesKey("params_stripped_count")
        val FINGERPRINT_COUNT = intPreferencesKey("fingerprint_count")
    }

    val preferencesFlow: Flow<BrowserPreferences> = context.dataStore.data.map { preferences ->
        val engineStr = preferences[PreferencesKeys.SEARCH_ENGINE] ?: SearchEngine.DUCKDUCKGO.name
        val engine = try { SearchEngine.valueOf(engineStr) } catch (_: Exception) { SearchEngine.DUCKDUCKGO }

        val themeStr = preferences[PreferencesKeys.THEME_MODE] ?: AppThemeMode.SYSTEM.name
        val theme = try { AppThemeMode.valueOf(themeStr) } catch (_: Exception) { AppThemeMode.SYSTEM }

        val layoutStr = preferences[PreferencesKeys.TOOLBAR_LAYOUT] ?: ToolbarLayout.COMPACT.name
        val layout = try { ToolbarLayout.valueOf(layoutStr) } catch (_: Exception) { ToolbarLayout.COMPACT }

        val fpStr = preferences[PreferencesKeys.FINGERPRINTING_PROTECTION] ?: FingerprintingProtection.ALL.name
        val fp = try { FingerprintingProtection.valueOf(fpStr) } catch (_: Exception) { FingerprintingProtection.ALL }

        BrowserPreferences(
            searchEngine = engine,
            searchSuggestionsEnabled = preferences[PreferencesKeys.SEARCH_SUGGESTIONS] ?: true,
            themeMode = theme,
            toolbarLayout = layout,
            darkPagesEnabled = preferences[PreferencesKeys.DARK_PAGES] ?: false,
            contentBlockerEnabled = preferences[PreferencesKeys.CONTENT_BLOCKER] ?: true,
            httpsOnlyEnabled = preferences[PreferencesKeys.HTTPS_ONLY] ?: true,
            blockThirdPartyCookies = preferences[PreferencesKeys.BLOCK_3RD_PARTY_COOKIES] ?: true,
            stripTrackingParams = preferences[PreferencesKeys.STRIP_TRACKING_PARAMS] ?: true,
            fingerprintingProtection = fp,
            disabledProtectionDomains = preferences[PreferencesKeys.DISABLED_PROTECTIONS] ?: emptySet(),
            showFavorites = preferences[PreferencesKeys.SHOW_FAVORITES] ?: true,
            showFrequentlyVisited = preferences[PreferencesKeys.SHOW_FREQUENTLY_VISITED] ?: true,
            showReadingList = preferences[PreferencesKeys.SHOW_READING_LIST] ?: true,
            showPrivacyCard = preferences[PreferencesKeys.SHOW_PRIVACY_CARD] ?: true,
            openLinksInBackground = preferences[PreferencesKeys.OPEN_IN_BACKGROUND] ?: false,
            autoCloseTabs = preferences[PreferencesKeys.AUTO_CLOSE_TABS] ?: "never",
            paramsStrippedCount = preferences[PreferencesKeys.PARAMS_STRIPPED_COUNT] ?: 0,
            fingerprintNeutralizedCount = preferences[PreferencesKeys.FINGERPRINT_COUNT] ?: 0
        )
    }

    suspend fun setSearchEngine(engine: SearchEngine) {
        context.dataStore.edit { it[PreferencesKeys.SEARCH_ENGINE] = engine.name }
    }

    suspend fun setSearchSuggestionsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SEARCH_SUGGESTIONS] = enabled }
    }

    suspend fun setThemeMode(theme: AppThemeMode) {
        context.dataStore.edit { it[PreferencesKeys.THEME_MODE] = theme.name }
    }

    suspend fun setToolbarLayout(layout: ToolbarLayout) {
        context.dataStore.edit { it[PreferencesKeys.TOOLBAR_LAYOUT] = layout.name }
    }

    suspend fun setDarkPagesEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DARK_PAGES] = enabled }
    }

    suspend fun setContentBlockerEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.CONTENT_BLOCKER] = enabled }
    }

    suspend fun setHttpsOnlyEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.HTTPS_ONLY] = enabled }
    }

    suspend fun setBlockThirdPartyCookies(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.BLOCK_3RD_PARTY_COOKIES] = enabled }
    }

    suspend fun setStripTrackingParams(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.STRIP_TRACKING_PARAMS] = enabled }
    }

    suspend fun setFingerprintingProtection(fp: FingerprintingProtection) {
        context.dataStore.edit { it[PreferencesKeys.FINGERPRINTING_PROTECTION] = fp.name }
    }

    suspend fun toggleDomainProtection(domain: String, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            val current = (prefs[PreferencesKeys.DISABLED_PROTECTIONS] ?: emptySet()).toMutableSet()
            if (enabled) {
                current.remove(domain)
            } else {
                current.add(domain)
            }
            prefs[PreferencesKeys.DISABLED_PROTECTIONS] = current
        }
    }

    suspend fun incrementParamsStripped(count: Int = 1) {
        context.dataStore.edit { prefs ->
            val cur = prefs[PreferencesKeys.PARAMS_STRIPPED_COUNT] ?: 0
            prefs[PreferencesKeys.PARAMS_STRIPPED_COUNT] = cur + count
        }
    }

    suspend fun incrementFingerprintNeutralized(count: Int = 1) {
        context.dataStore.edit { prefs ->
            val cur = prefs[PreferencesKeys.FINGERPRINT_COUNT] ?: 0
            prefs[PreferencesKeys.FINGERPRINT_COUNT] = cur + count
        }
    }

    suspend fun setShowFavorites(show: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_FAVORITES] = show }
    }

    suspend fun setShowFrequentlyVisited(show: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_FREQUENTLY_VISITED] = show }
    }

    suspend fun setShowReadingList(show: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_READING_LIST] = show }
    }

    suspend fun setShowPrivacyCard(show: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_PRIVACY_CARD] = show }
    }
}
