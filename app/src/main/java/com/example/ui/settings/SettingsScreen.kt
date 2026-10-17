package com.example.ui.settings

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.ViewQuilt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.preferences.AppThemeMode
import com.example.data.preferences.FingerprintingProtection
import com.example.data.preferences.SearchEngine
import com.example.data.preferences.ToolbarLayout
import com.example.ui.browser.BrowserViewModel
import com.example.ui.theme.TrekTheme
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: BrowserViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val preferences by viewModel.preferences.collectAsState()

    var showSearchEngineDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showFpDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TrekTheme.colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.settings),
                    style = MaterialTheme.typography.titleLarge,
                    color = TrekTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )

                TextButton(onClick = onClose) {
                    Text(
                        text = stringResource(R.string.done),
                        style = MaterialTheme.typography.titleMedium,
                        color = TrekTheme.colors.accent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Toolbar Layout Preview Cards
            SettingsSectionHeader(title = "Toolbar Layout")
            Text(
                text = "Choose your preferred address bar and control layout",
                style = MaterialTheme.typography.bodySmall,
                color = TrekTheme.colors.textSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ToolbarLayout.entries.forEach { layout ->
                    ToolbarPreviewCard(
                        layout = layout,
                        isSelected = preferences.toolbarLayout == layout,
                        onClick = {
                            coroutineScope.launch {
                                viewModel.preferencesRepository.setToolbarLayout(layout)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Search & Navigation
            SettingsSectionHeader(title = "Search & Navigation")

            SettingsRow(
                icon = Icons.Outlined.Search,
                title = stringResource(R.string.search_engine),
                subtitle = preferences.searchEngine.displayName,
                onClick = { showSearchEngineDialog = true }
            )

            SettingsToggleRow(
                icon = Icons.Outlined.Language,
                title = stringResource(R.string.search_suggestions),
                subtitle = "Query suggestions from ${preferences.searchEngine.displayName}",
                checked = preferences.searchSuggestionsEnabled,
                onCheckedChange = { coroutineScope.launch { viewModel.preferencesRepository.setSearchSuggestionsEnabled(it) } }
            )

            SettingsRow(
                icon = Icons.Outlined.Explore,
                title = stringResource(R.string.set_as_default_browser),
                subtitle = "Open web links in TrekScape by default",
                onClick = {
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        Toast.makeText(context, "Select TrekScape in Default Apps settings", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            // Appearance Section
            SettingsSectionHeader(title = "Appearance")

            SettingsRow(
                icon = Icons.Outlined.DarkMode,
                title = stringResource(R.string.appearance),
                subtitle = when (preferences.themeMode) {
                    AppThemeMode.SYSTEM -> "Follow System"
                    AppThemeMode.LIGHT -> "Always Light"
                    AppThemeMode.DARK -> "Always Dark"
                },
                onClick = { showThemeDialog = true }
            )

            SettingsToggleRow(
                icon = Icons.Outlined.DarkMode,
                title = stringResource(R.string.dark_pages),
                subtitle = "Force dark theme algorithmically on web pages",
                checked = preferences.darkPagesEnabled,
                onCheckedChange = { coroutineScope.launch { viewModel.preferencesRepository.setDarkPagesEnabled(it) } }
            )

            // Enhanced Privacy & Security by Default
            SettingsSectionHeader(title = "Privacy & Security (Active by Default)")

            SettingsToggleRow(
                icon = Icons.Outlined.Security,
                title = "Content & Tracker Blocker",
                subtitle = "Blocks requests to tracking and ad networks using the bundled domain list",
                checked = preferences.contentBlockerEnabled,
                onCheckedChange = { coroutineScope.launch { viewModel.preferencesRepository.setContentBlockerEnabled(it) } }
            )

            SettingsToggleRow(
                icon = Icons.Outlined.LinkOff,
                title = "Tracking Parameter Stripping",
                subtitle = "Removes tracking query parameters (utm_*, fbclid, gclid) while preserving logins",
                checked = preferences.stripTrackingParams,
                onCheckedChange = { coroutineScope.launch { viewModel.preferencesRepository.setStripTrackingParams(it) } }
            )

            SettingsRow(
                icon = Icons.Outlined.Fingerprint,
                title = "Advanced Fingerprinting Protection",
                subtitle = "${preferences.fingerprintingProtection.displayName} — Spoofs canvas, audio, and device memory",
                onClick = { showFpDialog = true }
            )

            SettingsToggleRow(
                icon = Icons.Outlined.Lock,
                title = "HTTPS-Only Upgrades",
                subtitle = "Automatically upgrades all insecure web requests to encrypted HTTPS",
                checked = preferences.httpsOnlyEnabled,
                onCheckedChange = { coroutineScope.launch { viewModel.preferencesRepository.setHttpsOnlyEnabled(it) } }
            )

            SettingsToggleRow(
                icon = Icons.Outlined.Lock,
                title = "Block Third-Party Cookies",
                subtitle = "Prevents cross-site tracking via third-party storage cookies",
                checked = preferences.blockThirdPartyCookies,
                onCheckedChange = { coroutineScope.launch { viewModel.preferencesRepository.setBlockThirdPartyCookies(it) } }
            )

            SettingsRow(
                icon = Icons.Outlined.Password,
                title = "Passwords & Autofill",
                subtitle = "Manage credentials securely via system autofill provider",
                onClick = {
                    try {
                        val intent = Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE)
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        Toast.makeText(context, "System Autofill available in device settings", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            SettingsRow(
                icon = Icons.Outlined.Delete,
                title = "Clear History and Website Data",
                subtitle = "Remove browsing history, cache, cookies, and tracker logs",
                onClick = { showClearDataDialog = true }
            )

            // Start Page Options
            SettingsSectionHeader(title = "Start Page Layout")

            SettingsToggleRow(
                icon = Icons.Outlined.Explore,
                title = "Favorites Section",
                checked = preferences.showFavorites,
                onCheckedChange = { coroutineScope.launch { viewModel.preferencesRepository.setShowFavorites(it) } }
            )

            SettingsToggleRow(
                icon = Icons.Outlined.Explore,
                title = "Frequently Visited Section",
                checked = preferences.showFrequentlyVisited,
                onCheckedChange = { coroutineScope.launch { viewModel.preferencesRepository.setShowFrequentlyVisited(it) } }
            )

            SettingsToggleRow(
                icon = Icons.Outlined.Explore,
                title = "Reading List Preview",
                checked = preferences.showReadingList,
                onCheckedChange = { coroutineScope.launch { viewModel.preferencesRepository.setShowReadingList(it) } }
            )

            SettingsToggleRow(
                icon = Icons.Outlined.Security,
                title = "Privacy Report Card",
                checked = preferences.showPrivacyCard,
                onCheckedChange = { coroutineScope.launch { viewModel.preferencesRepository.setShowPrivacyCard(it) } }
            )

            // About Section
            SettingsSectionHeader(title = "About")

            SettingsRow(
                icon = Icons.Outlined.Info,
                title = stringResource(R.string.about_trekscape),
                subtitle = "Version 1.0.0 • Chromium Engine • Zero Telemetry",
                onClick = { showAboutDialog = true }
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Search Engine Selection Dialog
    if (showSearchEngineDialog) {
        Dialog(onDismissRequest = { showSearchEngineDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TrekTheme.colors.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = stringResource(R.string.search_engine),
                        style = MaterialTheme.typography.titleLarge,
                        color = TrekTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    SearchEngine.entries.forEach { engine ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    coroutineScope.launch { viewModel.preferencesRepository.setSearchEngine(engine) }
                                    showSearchEngineDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = engine.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TrekTheme.colors.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            if (preferences.searchEngine == engine) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = TrekTheme.colors.accent
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Fingerprinting Protection Dialog
    if (showFpDialog) {
        Dialog(onDismissRequest = { showFpDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TrekTheme.colors.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Fingerprinting Protection",
                        style = MaterialTheme.typography.titleLarge,
                        color = TrekTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Randomizes canvas/audio noise and normalizes device hardware specs to prevent cross-site user identification.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TrekTheme.colors.textSecondary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )
                    FingerprintingProtection.entries.forEach { fp ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    coroutineScope.launch { viewModel.preferencesRepository.setFingerprintingProtection(fp) }
                                    showFpDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = fp.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TrekTheme.colors.textPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = fp.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TrekTheme.colors.textSecondary
                                )
                            }
                            if (preferences.fingerprintingProtection == fp) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = TrekTheme.colors.accent
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Appearance Selection Dialog
    if (showThemeDialog) {
        Dialog(onDismissRequest = { showThemeDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TrekTheme.colors.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = stringResource(R.string.appearance),
                        style = MaterialTheme.typography.titleLarge,
                        color = TrekTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    listOf(
                        AppThemeMode.SYSTEM to "Follow System",
                        AppThemeMode.LIGHT to "Always Light",
                        AppThemeMode.DARK to "Always Dark"
                    ).forEach { (mode, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    coroutineScope.launch { viewModel.preferencesRepository.setThemeMode(mode) }
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TrekTheme.colors.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            if (preferences.themeMode == mode) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = TrekTheme.colors.accent
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Clear Data Confirmation Dialog
    if (showClearDataDialog) {
        Dialog(onDismissRequest = { showClearDataDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TrekTheme.colors.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Clear Browsing Data",
                        style = MaterialTheme.typography.titleLarge,
                        color = TrekTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "This will permanently remove your browsing history and tracker statistics.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TrekTheme.colors.textSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showClearDataDialog = false }) {
                            Text("Cancel", color = TrekTheme.colors.textSecondary)
                        }
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.historyRepository.clearAllHistory()
                                    viewModel.trackerRepository.clearStats()
                                    Toast.makeText(context, "History and data cleared", Toast.LENGTH_SHORT).show()
                                }
                                showClearDataDialog = false
                            }
                        ) {
                            Text("Clear", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // About TrekScape Dialog
    if (showAboutDialog) {
        Dialog(onDismissRequest = { showAboutDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TrekTheme.colors.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(TrekTheme.colors.accent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Explore,
                            contentDescription = null,
                            tint = TrekTheme.colors.accent,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "TrekScape Browser",
                        style = MaterialTheme.typography.titleLarge,
                        color = TrekTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Version 1.0.0 (Build 2026)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TrekTheme.colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "A minimalist, gesture-driven web browser built with Android System WebView (Chromium) and Jetpack Compose. Designed with zero telemetry, privacy-first tracker blocking, parameter stripping, and fingerprint defenses.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TrekTheme.colors.textSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    TextButton(onClick = { showAboutDialog = false }) {
                        Text("Close", color = TrekTheme.colors.accent, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ToolbarPreviewCard(
    layout: ToolbarLayout,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) TrekTheme.colors.accent else TrekTheme.colors.separator,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) TrekTheme.colors.accent.copy(alpha = 0.08f) else TrekTheme.colors.cardBg
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Illustrated phone mockup
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TrekTheme.colors.surfaceVariant)
                    .padding(4.dp)
            ) {
                when (layout) {
                    ToolbarLayout.COMPACT -> {
                        // Slim single bar at bottom
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth(0.9f)
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(TrekTheme.colors.accent)
                        )
                    }
                    ToolbarLayout.BOTTOM -> {
                        // Stacked capsule + toolbar at bottom
                        Column(
                            modifier = Modifier.align(Alignment.BottomCenter),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.75f)
                                    .height(9.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(TrekTheme.colors.accent)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .height(9.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(TrekTheme.colors.textSecondary.copy(alpha = 0.4f))
                            )
                        }
                    }
                    ToolbarLayout.TOP -> {
                        // Capsule at top, toolbar at bottom
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth(0.8f)
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(TrekTheme.colors.accent)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth(0.9f)
                                .height(9.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(TrekTheme.colors.textSecondary.copy(alpha = 0.4f))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = layout.displayName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) TrekTheme.colors.accent else TrekTheme.colors.textPrimary
            )
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = TrekTheme.colors.accent,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 22.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TrekTheme.colors.accent,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = TrekTheme.colors.textPrimary,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TrekTheme.colors.textSecondary
                )
            }
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = TrekTheme.colors.textSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TrekTheme.colors.accent,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = TrekTheme.colors.textPrimary,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TrekTheme.colors.textSecondary
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = TrekTheme.colors.accent
            )
        )
    }
}
