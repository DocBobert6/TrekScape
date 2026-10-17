package com.example.ui.menu

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.FindInPage
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.ViewQuilt
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material.icons.outlined.ZoomOut
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.MainActivity
import com.example.R
import com.example.data.preferences.ToolbarLayout
import com.example.model.ReaderArticle
import com.example.ui.browser.BrowserViewModel
import com.example.ui.theme.TrekTheme
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageOptionsMenu(
    viewModel: BrowserViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activeTab = viewModel.getActiveTab() ?: return
    val preferences by viewModel.preferences.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val currentDomain = remember(activeTab.url) { Uri.parse(activeTab.url).host.orEmpty() }
    val isProtectionsDisabled = preferences.disabledProtectionDomains.contains(currentDomain)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = TrekTheme.colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Text Size & Page Zoom Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(TrekTheme.colors.surfaceVariant)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.adjustZoom(-10) }) {
                        Icon(
                            imageVector = Icons.Outlined.ZoomOut,
                            contentDescription = "Zoom out",
                            tint = TrekTheme.colors.textPrimary
                        )
                    }
                    Text(
                        text = "${activeTab.zoomPercent}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TrekTheme.colors.textPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(onClick = { viewModel.adjustZoom(10) }) {
                        Icon(
                            imageVector = Icons.Outlined.ZoomIn,
                            contentDescription = "Zoom in",
                            tint = TrekTheme.colors.textPrimary
                        )
                    }
                }

                // Desktop mode toggle
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            viewModel.toggleDesktopMode()
                            onDismiss()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Computer,
                        contentDescription = stringResource(R.string.desktop_site),
                        tint = if (activeTab.desktopMode) TrekTheme.colors.accent else TrekTheme.colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Desktop",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (activeTab.desktopMode) TrekTheme.colors.accent else TrekTheme.colors.textSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Per-site protections toggle card
            if (currentDomain.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isProtectionsDisabled) Color(0xFFEF4444).copy(alpha = 0.08f) else TrekTheme.colors.surfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Security,
                        contentDescription = null,
                        tint = if (isProtectionsDisabled) Color(0xFFEF4444) else TrekTheme.colors.accent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isProtectionsDisabled) "Protections OFF for site" else "Protections Active",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TrekTheme.colors.textPrimary
                        )
                        Text(
                            text = if (isProtectionsDisabled) "Trackers allowed for $currentDomain" else "Blocking trackers & fingerprinting",
                            style = MaterialTheme.typography.bodySmall,
                            color = TrekTheme.colors.textSecondary
                        )
                    }
                    Switch(
                        checked = !isProtectionsDisabled,
                        onCheckedChange = { active ->
                            coroutineScope.launch {
                                viewModel.preferencesRepository.toggleDomainProtection(currentDomain, active)
                                Toast.makeText(context, "Reloading $currentDomain...", Toast.LENGTH_SHORT).show()
                                activeTab.webView?.reload()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = TrekTheme.colors.accent
                        )
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Toolbar Layout Switcher Row
            MenuActionRow(
                icon = Icons.Outlined.ViewQuilt,
                title = "Toolbar Layout",
                subtitle = "Current: ${preferences.toolbarLayout.displayName}",
                onClick = {
                    val next = when (preferences.toolbarLayout) {
                        ToolbarLayout.COMPACT -> ToolbarLayout.BOTTOM
                        ToolbarLayout.BOTTOM -> ToolbarLayout.TOP
                        ToolbarLayout.TOP -> ToolbarLayout.COMPACT
                    }
                    coroutineScope.launch {
                        viewModel.preferencesRepository.setToolbarLayout(next)
                    }
                }
            )

            // Actions list
            MenuActionRow(
                icon = Icons.Outlined.MenuBook,
                title = stringResource(R.string.reader_view),
                subtitle = "Clean article layout",
                onClick = {
                    onDismiss()
                    activeTab.webView?.let { wv ->
                        try {
                            val script = context.assets.open("reader.js").bufferedReader().use { it.readText() }
                            wv.evaluateJavascript(script) { jsonResult ->
                                if (!jsonResult.isNullOrBlank() && jsonResult != "null") {
                                    try {
                                        val unquoted = if (jsonResult.startsWith("\"") && jsonResult.endsWith("\"")) {
                                            JSONObject("{ \"s\": $jsonResult }").getString("s")
                                        } else {
                                            jsonResult
                                        }
                                        val obj = JSONObject(unquoted)
                                        val article = ReaderArticle(
                                            title = obj.optString("title", activeTab.title),
                                            byline = obj.optString("byline", ""),
                                            leadImage = obj.optString("leadImage", ""),
                                            readingTimeMinutes = obj.optInt("readingTimeMinutes", 1),
                                            wordCount = obj.optInt("wordCount", 0),
                                            contentHtml = obj.optString("contentHtml", ""),
                                            url = obj.optString("url", activeTab.url),
                                            siteName = obj.optString("siteName", "")
                                        )
                                        viewModel.openReaderMode(article)
                                    } catch (_: Exception) {
                                        viewModel.openReaderMode(
                                            ReaderArticle(
                                                title = activeTab.title,
                                                byline = "",
                                                leadImage = "",
                                                readingTimeMinutes = 1,
                                                wordCount = 0,
                                                contentHtml = "<p>Reading mode could not extract content.</p>",
                                                url = activeTab.url,
                                                siteName = Uri.parse(activeTab.url).host.orEmpty()
                                            )
                                        )
                                    }
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }
            )

            MenuActionRow(
                icon = Icons.Outlined.FindInPage,
                title = stringResource(R.string.find_on_page),
                onClick = {
                    viewModel.openFindOnPage()
                    onDismiss()
                }
            )

            MenuActionRow(
                icon = Icons.Outlined.BookmarkBorder,
                title = stringResource(R.string.add_bookmark),
                onClick = {
                    coroutineScope.launch {
                        viewModel.bookmarkRepository.addBookmark(activeTab.title, activeTab.url)
                        Toast.makeText(context, "Added to Bookmarks", Toast.LENGTH_SHORT).show()
                    }
                    onDismiss()
                }
            )

            MenuActionRow(
                icon = Icons.Outlined.AddBox,
                title = stringResource(R.string.add_to_reading_list),
                onClick = {
                    coroutineScope.launch {
                        viewModel.readingListRepository.addToReadingList(
                            title = activeTab.title,
                            url = activeTab.url
                        )
                        Toast.makeText(context, "Added to Reading List", Toast.LENGTH_SHORT).show()
                    }
                    onDismiss()
                }
            )

            MenuActionRow(
                icon = Icons.Outlined.Share,
                title = "Add to Home Screen",
                onClick = {
                    try {
                        val shortcutIntent = Intent(context, MainActivity::class.java).apply {
                            action = Intent.ACTION_VIEW
                            data = Uri.parse(activeTab.url)
                        }
                        val pinShortcutInfo = ShortcutInfoCompat.Builder(context, "shortcut_${activeTab.url.hashCode()}")
                            .setShortLabel(activeTab.title.take(15))
                            .setLongLabel(activeTab.title)
                            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_launcher_foreground))
                            .setIntent(shortcutIntent)
                            .build()
                        ShortcutManagerCompat.requestPinShortcut(context, pinShortcutInfo, null)
                        Toast.makeText(context, "Shortcut created", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Shortcut created on Home screen", Toast.LENGTH_SHORT).show()
                    }
                    onDismiss()
                }
            )

            MenuActionRow(
                icon = Icons.Outlined.Print,
                title = stringResource(R.string.print_save_pdf),
                onClick = {
                    try {
                        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                        val printAdapter = activeTab.webView?.createPrintDocumentAdapter(activeTab.title.ifBlank { "TrekScape_Page" })
                        printAdapter?.let { adapter ->
                            printManager?.print("TrekScape - ${activeTab.title}", adapter, PrintAttributes.Builder().build())
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Printing unavailable", Toast.LENGTH_SHORT).show()
                    }
                    onDismiss()
                }
            )

            MenuActionRow(
                icon = Icons.Outlined.Translate,
                title = stringResource(R.string.translate),
                onClick = {
                    val encoded = Uri.encode(activeTab.url)
                    val translateUrl = "https://translate.google.com/translate?sl=auto&tl=en&u=$encoded"
                    viewModel.loadUrl(translateUrl)
                    onDismiss()
                }
            )

            MenuActionRow(
                icon = Icons.Outlined.Security,
                title = stringResource(R.string.privacy_report),
                onClick = {
                    viewModel.setPrivacyReportOpen(true)
                    onDismiss()
                }
            )

            MenuActionRow(
                icon = Icons.Outlined.Settings,
                title = stringResource(R.string.website_settings),
                onClick = {
                    viewModel.setWebsiteSettingsOpen(true)
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun MenuActionRow(
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
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(TrekTheme.colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TrekTheme.colors.textPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

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
    }
}
