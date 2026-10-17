package com.example.ui.browser

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowOutward
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.NorthWest
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.ViewQuilt
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.data.preferences.ToolbarLayout
import com.example.model.BrowserTab
import com.example.model.JsDialogType
import com.example.ui.library.LibraryScreen
import com.example.ui.menu.PageOptionsMenu
import com.example.ui.privacy.PrivacyReportScreen
import com.example.ui.reader.ReaderScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.start.StartPage
import com.example.ui.tabs.TabOverviewScreen
import com.example.ui.theme.SecureGreen
import com.example.ui.theme.TrekTheme
import com.example.ui.theme.WarningAmber

@Composable
fun BrowserScreen(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tabs by viewModel.tabs.collectAsState()
    val activeTab = viewModel.getActiveTab() ?: tabs.first()
    val preferences by viewModel.preferences.collectAsState()

    val isCapsuleFocused by viewModel.isCapsuleFocused.collectAsState()
    val capsuleInput by viewModel.capsuleInputText.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val isBarsCollapsed by viewModel.isBarsCollapsed.collectAsState()

    val isTabOverviewOpen by viewModel.isTabOverviewOpen.collectAsState()
    val isPageOptionsOpen by viewModel.isPageOptionsOpen.collectAsState()
    val isLibraryOpen by viewModel.isLibraryOpen.collectAsState()
    val libraryTab by viewModel.libraryTab.collectAsState()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsState()
    val isPrivacyReportOpen by viewModel.isPrivacyReportOpen.collectAsState()
    val isWebsiteSettingsOpen by viewModel.isWebsiteSettingsOpen.collectAsState()
    val isReaderOpen by viewModel.isReaderOpen.collectAsState()
    val isFindOnPageOpen by viewModel.isFindOnPageOpen.collectAsState()

    val contextMenu by viewModel.contextMenu.collectAsState()
    val jsDialog by viewModel.jsDialog.collectAsState()
    val permissionRequest by viewModel.permissionRequest.collectAsState()
    val certError by viewModel.certError.collectAsState()
    val customView by viewModel.customView.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    val toolbarLayout = preferences.toolbarLayout

    // Predictive Back Handling
    BackHandler(enabled = true) {
        when {
            isCapsuleFocused -> {
                viewModel.setCapsuleFocused(false)
                keyboardController?.hide()
                focusManager.clearFocus()
            }
            isFindOnPageOpen -> viewModel.closeFindOnPage()
            isPageOptionsOpen -> viewModel.setPageOptionsOpen(false)
            isTabOverviewOpen -> viewModel.setTabOverviewOpen(false)
            isLibraryOpen -> viewModel.closeLibrary()
            isSettingsOpen -> viewModel.setSettingsOpen(false)
            isPrivacyReportOpen -> viewModel.setPrivacyReportOpen(false)
            isWebsiteSettingsOpen -> viewModel.setWebsiteSettingsOpen(false)
            isReaderOpen -> viewModel.closeReaderMode()
            activeTab.canGoBack -> activeTab.webView?.goBack()
            tabs.size > 1 -> viewModel.closeTab(activeTab.id)
            activeTab.url.isNotBlank() -> viewModel.loadUrl("")
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TrekTheme.colors.background)
    ) {
        // Main Web Content or Start Page (extends edge-to-edge behind floating bars)
        Column(modifier = Modifier.fillMaxSize()) {
            // Dynamic top padding if Top layout
            val topInsetPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            val extraTopPad = if (toolbarLayout == ToolbarLayout.TOP && !isBarsCollapsed) 64.dp else 0.dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(topInsetPadding + extraTopPad)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            activeTab.webView?.scrollTo(0, 0)
                        }
                    }
            )

            // Web body container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (activeTab.url.isBlank()) {
                    StartPage(
                        viewModel = viewModel,
                        onNavigate = { viewModel.loadUrl(it) }
                    )
                } else {
                    BrowserWebView(
                        tab = activeTab,
                        viewModel = viewModel,
                        onScrollChanged = { scrollY, deltaY ->
                            if (deltaY > 15 && scrollY > 100) {
                                viewModel.setBarsCollapsed(true)
                            } else if (deltaY < -15) {
                                viewModel.setBarsCollapsed(false)
                            }
                        }
                    )
                }
            }

            // Dynamic bottom padding so page content scrolls completely clear of floating bars
            val navBarPad = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            val bottomClearance = when {
                isBarsCollapsed -> 48.dp + navBarPad
                toolbarLayout == ToolbarLayout.COMPACT -> 72.dp + navBarPad
                else -> 120.dp + navBarPad
            }
            Spacer(modifier = Modifier.height(bottomClearance))
        }

        // Suggestions Dropdown List (positioned appropriately for Top vs Bottom layout)
        AnimatedVisibility(
            visible = isCapsuleFocused,
            enter = fadeIn() + slideInVertically(initialOffsetY = { if (toolbarLayout == ToolbarLayout.TOP) -40 else 40 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { if (toolbarLayout == ToolbarLayout.TOP) -40 else 40 }),
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = if (toolbarLayout == ToolbarLayout.TOP) WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 64.dp else WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                    bottom = if (toolbarLayout != ToolbarLayout.TOP) WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 68.dp else 0.dp
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(TrekTheme.colors.background)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(suggestions) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    viewModel.submitInput(item.urlOrQuery)
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (item.type) {
                                    SuggestionType.TOP_HIT -> Icons.Outlined.ArrowOutward
                                    SuggestionType.BOOKMARK -> Icons.Outlined.BookmarkBorder
                                    SuggestionType.HISTORY -> Icons.Outlined.History
                                    SuggestionType.SEARCH -> Icons.Outlined.Search
                                },
                                contentDescription = null,
                                tint = if (item.type == SuggestionType.TOP_HIT) TrekTheme.colors.accent else TrekTheme.colors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TrekTheme.colors.textPrimary,
                                    fontWeight = if (item.type == SuggestionType.TOP_HIT) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (!item.subtitle.isNullOrBlank()) {
                                    Text(
                                        text = item.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TrekTheme.colors.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            IconButton(onClick = { viewModel.updateCapsuleInput(item.title) }) {
                                Icon(
                                    imageVector = Icons.Outlined.NorthWest,
                                    contentDescription = "Fill suggestion",
                                    tint = TrekTheme.colors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // TOP LAYOUT: Floating Address Capsule at top
        if (toolbarLayout == ToolbarLayout.TOP) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 6.dp)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                FloatingCapsule(
                    tab = activeTab,
                    isFocused = isCapsuleFocused,
                    inputText = capsuleInput,
                    isCollapsed = isBarsCollapsed,
                    onFocusChange = { focused ->
                        viewModel.setCapsuleFocused(focused)
                        if (focused) focusRequester.requestFocus()
                    },
                    onInputChange = { viewModel.updateCapsuleInput(it) },
                    onSubmit = {
                        viewModel.submitInput(it)
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    },
                    onReloadStop = {
                        if (activeTab.isLoading) activeTab.webView?.stopLoading()
                        else if (activeTab.url.isNotBlank()) activeTab.webView?.reload()
                    },
                    onPageOptions = { viewModel.setPageOptionsOpen(true) },
                    onSwipeTab = { offset -> viewModel.switchTabByOffset(offset) },
                    onSwipeUp = { viewModel.setTabOverviewOpen(true) },
                    onRestoreBars = { viewModel.setBarsCollapsed(false) },
                    focusRequester = focusRequester,
                    modifier = Modifier.widthIn(max = 600.dp)
                )
            }
        }

        // BOTTOM FLOATING BAR STACK
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 10.dp)
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Find on page bar
            AnimatedVisibility(visible = isFindOnPageOpen) {
                FindOnPageBar(
                    viewModel = viewModel,
                    modifier = Modifier.widthIn(max = 600.dp)
                )
            }

            when (toolbarLayout) {
                ToolbarLayout.COMPACT -> {
                    // FEATURE 1: COMPACT LAYOUT (One slim floating row)
                    CompactFloatingRow(
                        tab = activeTab,
                        tabsCount = tabs.size,
                        isFocused = isCapsuleFocused,
                        inputText = capsuleInput,
                        isCollapsed = isBarsCollapsed,
                        onFocusChange = { focused ->
                            viewModel.setCapsuleFocused(focused)
                            if (focused) focusRequester.requestFocus()
                        },
                        onInputChange = { viewModel.updateCapsuleInput(it) },
                        onSubmit = {
                            viewModel.submitInput(it)
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        },
                        onReloadStop = {
                            if (activeTab.isLoading) activeTab.webView?.stopLoading()
                            else if (activeTab.url.isNotBlank()) activeTab.webView?.reload()
                        },
                        onBack = { activeTab.webView?.goBack() },
                        onForward = { activeTab.webView?.goForward() },
                        onShare = {
                            if (activeTab.url.isNotBlank()) {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, activeTab.url)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share via TrekScape"))
                            }
                        },
                        onBookmarks = { viewModel.openLibrary(0) },
                        onNewTab = { viewModel.openTab() },
                        onTabs = { viewModel.setTabOverviewOpen(true) },
                        onPageOptions = { viewModel.setPageOptionsOpen(true) },
                        onSwipeTab = { offset -> viewModel.switchTabByOffset(offset) },
                        onSwipeUp = { viewModel.setTabOverviewOpen(true) },
                        onRestoreBars = { viewModel.setBarsCollapsed(false) },
                        focusRequester = focusRequester,
                        modifier = Modifier.widthIn(max = 600.dp)
                    )
                }

                ToolbarLayout.BOTTOM -> {
                    // Floating address capsule
                    FloatingCapsule(
                        tab = activeTab,
                        isFocused = isCapsuleFocused,
                        inputText = capsuleInput,
                        isCollapsed = isBarsCollapsed,
                        onFocusChange = { focused ->
                            viewModel.setCapsuleFocused(focused)
                            if (focused) focusRequester.requestFocus()
                        },
                        onInputChange = { viewModel.updateCapsuleInput(it) },
                        onSubmit = {
                            viewModel.submitInput(it)
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        },
                        onReloadStop = {
                            if (activeTab.isLoading) activeTab.webView?.stopLoading()
                            else if (activeTab.url.isNotBlank()) activeTab.webView?.reload()
                        },
                        onPageOptions = { viewModel.setPageOptionsOpen(true) },
                        onSwipeTab = { offset -> viewModel.switchTabByOffset(offset) },
                        onSwipeUp = { viewModel.setTabOverviewOpen(true) },
                        onRestoreBars = { viewModel.setBarsCollapsed(false) },
                        focusRequester = focusRequester,
                        modifier = Modifier.widthIn(max = 600.dp)
                    )

                    // Floating Navigation Toolbar
                    AnimatedVisibility(
                        visible = !isBarsCollapsed && !isCapsuleFocused,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { 40 }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { 40 })
                    ) {
                        FloatingToolbar(
                            activeTab = activeTab,
                            tabsCount = tabs.size,
                            onBack = { activeTab.webView?.goBack() },
                            onForward = { activeTab.webView?.goForward() },
                            onShare = {
                                if (activeTab.url.isNotBlank()) {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, activeTab.url)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share via TrekScape"))
                                }
                            },
                            onBookmarks = { viewModel.openLibrary(0) },
                            onTabs = { viewModel.setTabOverviewOpen(true) },
                            modifier = Modifier.widthIn(max = 600.dp)
                        )
                    }
                }

                ToolbarLayout.TOP -> {
                    // Top layout has capsule at top; toolbar floats at bottom
                    AnimatedVisibility(
                        visible = !isBarsCollapsed && !isCapsuleFocused,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { 40 }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { 40 })
                    ) {
                        FloatingToolbar(
                            activeTab = activeTab,
                            tabsCount = tabs.size,
                            onBack = { activeTab.webView?.goBack() },
                            onForward = { activeTab.webView?.goForward() },
                            onShare = {
                                if (activeTab.url.isNotBlank()) {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, activeTab.url)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share via TrekScape"))
                                }
                            },
                            onBookmarks = { viewModel.openLibrary(0) },
                            onTabs = { viewModel.setTabOverviewOpen(true) },
                            modifier = Modifier.widthIn(max = 600.dp)
                        )
                    }
                }
            }
        }

        // Fullscreen Custom View (video)
        customView?.let { cView ->
            AndroidView(
                factory = { cView },
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
        }

        // Sheets & Overlays
        if (isTabOverviewOpen) TabOverviewScreen(viewModel = viewModel)
        if (isPageOptionsOpen) PageOptionsMenu(viewModel = viewModel, onDismiss = { viewModel.setPageOptionsOpen(false) })
        if (isLibraryOpen) LibraryScreen(viewModel = viewModel, initialTab = libraryTab, onNavigate = { viewModel.loadUrl(it); viewModel.closeLibrary() }, onClose = { viewModel.closeLibrary() })
        if (isSettingsOpen) SettingsScreen(viewModel = viewModel, onClose = { viewModel.setSettingsOpen(false) })
        if (isPrivacyReportOpen) PrivacyReportScreen(viewModel = viewModel, onClose = { viewModel.setPrivacyReportOpen(false) })
        if (isWebsiteSettingsOpen) WebsiteSettingsSheet(viewModel = viewModel, onDismiss = { viewModel.setWebsiteSettingsOpen(false) })
        if (isReaderOpen) ReaderScreen(viewModel = viewModel, onClose = { viewModel.closeReaderMode() })

        // Context Menu Sheet
        contextMenu?.let { target ->
            ContextMenuSheet(target = target, viewModel = viewModel, onDismiss = { viewModel.dismissContextMenu() })
        }

        // JS Dialogs
        jsDialog?.let { dialog ->
            var promptText by remember { mutableStateOf(dialog.defaultValue) }
            AlertDialog(
                onDismissRequest = dialog.onCancel,
                title = { Text(Uri.parse(dialog.url).host.orEmpty()) },
                text = {
                    Column {
                        Text(dialog.message)
                        if (dialog.type == JsDialogType.PROMPT) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = promptText,
                                onValueChange = { promptText = it },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { dialog.onConfirm(promptText) }) {
                        Text("OK", color = TrekTheme.colors.accent, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    if (dialog.type != JsDialogType.ALERT) {
                        TextButton(onClick = dialog.onCancel) {
                            Text("Cancel", color = TrekTheme.colors.textSecondary)
                        }
                    }
                }
            )
        }

        // Permissions Request Dialog
        permissionRequest?.let { perm ->
            AlertDialog(
                onDismissRequest = perm.onDeny,
                icon = { Icon(Icons.Outlined.Warning, contentDescription = null, tint = WarningAmber) },
                title = { Text("Permission Request") },
                text = { Text("${perm.origin} requests access to: ${perm.resources.joinToString(", ")}.") },
                confirmButton = {
                    TextButton(onClick = perm.onGrant) {
                        Text("Allow", color = TrekTheme.colors.accent, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = perm.onDeny) {
                        Text("Deny", color = TrekTheme.colors.textSecondary)
                    }
                }
            )
        }

        // Certificate Error Dialog
        certError?.let { cert ->
            AlertDialog(
                onDismissRequest = cert.onCancel,
                icon = { Icon(Icons.Outlined.Warning, contentDescription = null, tint = Color(0xFFEF4444)) },
                title = { Text(stringResource(R.string.certificate_warning_title)) },
                text = {
                    Text("${stringResource(R.string.certificate_warning_message)}\n\nReason: ${cert.primaryError}\nTarget: ${cert.url}")
                },
                confirmButton = {
                    TextButton(onClick = cert.onCancel) {
                        Text(stringResource(R.string.go_back_safety), color = TrekTheme.colors.accent, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = cert.onProceed) {
                        Text(stringResource(R.string.proceed_anyway), color = Color(0xFFEF4444))
                    }
                }
            )
        }
    }
}

// FEATURE 1: COMPACT LAYOUT ROW (Back button when available + Capsule + More options menu)
@Composable
fun CompactFloatingRow(
    tab: BrowserTab,
    tabsCount: Int,
    isFocused: Boolean,
    inputText: String,
    isCollapsed: Boolean,
    onFocusChange: (Boolean) -> Unit,
    onInputChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    onReloadStop: () -> Unit,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onShare: () -> Unit,
    onBookmarks: () -> Unit,
    onNewTab: () -> Unit,
    onTabs: () -> Unit,
    onPageOptions: () -> Unit,
    onSwipeTab: (Int) -> Unit,
    onSwipeUp: () -> Unit,
    onRestoreBars: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    var showMoreMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .background(TrekTheme.colors.barBackground)
            .border(0.75.dp, TrekTheme.colors.separator.copy(alpha = 0.6f), RoundedCornerShape(28.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button (only shown when back history is available)
        if (tab.canGoBack && !isFocused) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = TrekTheme.colors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Address Capsule Pill (middle)
        FloatingCapsule(
            tab = tab,
            isFocused = isFocused,
            inputText = inputText,
            isCollapsed = isCollapsed,
            onFocusChange = onFocusChange,
            onInputChange = onInputChange,
            onSubmit = onSubmit,
            onReloadStop = onReloadStop,
            onPageOptions = onPageOptions,
            onSwipeTab = onSwipeTab,
            onSwipeUp = onSwipeUp,
            onRestoreBars = onRestoreBars,
            focusRequester = focusRequester,
            modifier = Modifier.weight(1f)
        )

        // "More" Button
        if (!isFocused) {
            Box {
                IconButton(
                    onClick = { showMoreMenu = true },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "More controls",
                        tint = TrekTheme.colors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMoreMenu,
                    onDismissRequest = { showMoreMenu = false }
                ) {
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null) },
                        text = { Text(stringResource(R.string.forward)) },
                        enabled = tab.canGoForward,
                        onClick = { onForward(); showMoreMenu = false }
                    )
                    DropdownMenuItem(
                        leadingIcon = { Icon(if (tab.isLoading) Icons.Outlined.Close else Icons.Outlined.Refresh, contentDescription = null) },
                        text = { Text(if (tab.isLoading) stringResource(R.string.stop) else stringResource(R.string.reload)) },
                        onClick = { onReloadStop(); showMoreMenu = false }
                    )
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Outlined.Share, contentDescription = null) },
                        text = { Text(stringResource(R.string.share)) },
                        onClick = { onShare(); showMoreMenu = false }
                    )
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Outlined.BookmarkBorder, contentDescription = null) },
                        text = { Text(stringResource(R.string.bookmarks)) },
                        onClick = { onBookmarks(); showMoreMenu = false }
                    )
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                        text = { Text(stringResource(R.string.new_tab)) },
                        onClick = { onNewTab(); showMoreMenu = false }
                    )
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Outlined.ViewQuilt, contentDescription = null) },
                        text = { Text("Tabs ($tabsCount)") },
                        onClick = { onTabs(); showMoreMenu = false }
                    )
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Outlined.Tune, contentDescription = null) },
                        text = { Text(stringResource(R.string.page_options)) },
                        onClick = { onPageOptions(); showMoreMenu = false }
                    )
                }
            }
        }
    }
}

// FEATURE 2: FLOATING CAPSULE WITH SPRING MORPH TRANSITION
@Composable
fun FloatingCapsule(
    tab: BrowserTab,
    isFocused: Boolean,
    inputText: String,
    isCollapsed: Boolean,
    onFocusChange: (Boolean) -> Unit,
    onInputChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    onReloadStop: () -> Unit,
    onPageOptions: () -> Unit,
    onSwipeTab: (Int) -> Unit,
    onSwipeUp: () -> Unit,
    onRestoreBars: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    var totalDragX by remember { mutableFloatStateOf(0f) }
    var totalDragY by remember { mutableFloatStateOf(0f) }

    val capsuleHeight by animateDpAsState(
        targetValue = if (isCollapsed) 38.dp else 50.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
        label = "capsuleHeight"
    )

    val domainText = remember(tab.url, tab.title) {
        if (tab.url.isBlank()) "Start Page"
        else Uri.parse(tab.url).host ?: tab.url
    }

    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .background(TrekTheme.colors.capsuleBg)
            .border(0.75.dp, TrekTheme.colors.separator.copy(alpha = 0.5f), RoundedCornerShape(28.dp))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (totalDragY < -70) {
                            onSwipeUp()
                        } else if (totalDragX > 60) {
                            onSwipeTab(-1)
                        } else if (totalDragX < -60) {
                            onSwipeTab(1)
                        }
                        totalDragX = 0f
                        totalDragY = 0f
                    },
                    onDrag = { _, dragAmount ->
                        totalDragX += dragAmount.x
                        totalDragY += dragAmount.y
                    }
                )
            }
            .clickable {
                if (isCollapsed) onRestoreBars()
                else if (!isFocused) onFocusChange(true)
            }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(capsuleHeight)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isFocused && !isCollapsed) {
                    IconButton(
                        onClick = onPageOptions,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = stringResource(R.string.page_options),
                            tint = TrekTheme.colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                if (!isFocused && tab.url.isNotBlank()) {
                    Icon(
                        imageVector = if (tab.hasCertError) Icons.Outlined.Warning else Icons.Outlined.Lock,
                        contentDescription = if (tab.isSecure) stringResource(R.string.security_secure) else stringResource(R.string.security_not_secure),
                        tint = if (tab.hasCertError) WarningAmber else if (tab.isSecure) SecureGreen else TrekTheme.colors.textSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = if (isFocused) Alignment.CenterStart else Alignment.Center
                ) {
                    if (isFocused) {
                        BasicTextField(
                            value = inputText,
                            onValueChange = onInputChange,
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = TrekTheme.colors.textPrimary),
                            cursorBrush = SolidColor(TrekTheme.colors.accent),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(onGo = { onSubmit(inputText) }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                        )
                    } else {
                        Text(
                            text = domainText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TrekTheme.colors.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (isFocused) {
                    if (inputText.isNotBlank()) {
                        IconButton(onClick = { onInputChange("") }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Outlined.Clear, contentDescription = stringResource(R.string.clear_input), tint = TrekTheme.colors.textSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                    TextButton(onClick = { onFocusChange(false) }) {
                        Text(stringResource(R.string.cancel), color = TrekTheme.colors.accent, style = MaterialTheme.typography.bodyMedium)
                    }
                } else if (!isCollapsed) {
                    IconButton(onClick = onReloadStop, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (tab.isLoading) Icons.Outlined.Close else Icons.Outlined.Refresh,
                            contentDescription = if (tab.isLoading) stringResource(R.string.stop) else stringResource(R.string.reload),
                            tint = TrekTheme.colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (tab.isLoading) {
                LinearProgressIndicator(
                    progress = { tab.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp),
                    color = TrekTheme.colors.accent,
                    trackColor = Color.Transparent
                )
            }
        }
    }
}

// FEATURE 2: FLOATING TOOLBAR WITH ROUNDED CAPSULE SHAPE
@Composable
fun FloatingToolbar(
    activeTab: BrowserTab,
    tabsCount: Int,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onShare: () -> Unit,
    onBookmarks: () -> Unit,
    onTabs: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(26.dp))
            .clip(RoundedCornerShape(26.dp))
            .background(TrekTheme.colors.barBackground)
            .border(0.75.dp, TrekTheme.colors.separator.copy(alpha = 0.5f), RoundedCornerShape(26.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, enabled = activeTab.canGoBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = if (activeTab.canGoBack) TrekTheme.colors.textPrimary else TrekTheme.colors.textSecondary.copy(alpha = 0.4f)
            )
        }

        IconButton(onClick = onForward, enabled = activeTab.canGoForward) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = stringResource(R.string.forward),
                tint = if (activeTab.canGoForward) TrekTheme.colors.textPrimary else TrekTheme.colors.textSecondary.copy(alpha = 0.4f)
            )
        }

        IconButton(onClick = onShare, enabled = activeTab.url.isNotBlank()) {
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = stringResource(R.string.share),
                tint = if (activeTab.url.isNotBlank()) TrekTheme.colors.textPrimary else TrekTheme.colors.textSecondary.copy(alpha = 0.4f)
            )
        }

        IconButton(onClick = onBookmarks) {
            Icon(
                imageVector = Icons.Outlined.BookmarkBorder,
                contentDescription = stringResource(R.string.bookmarks),
                tint = TrekTheme.colors.textPrimary
            )
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(9.dp))
                .border(1.5.dp, TrekTheme.colors.textPrimary, RoundedCornerShape(9.dp))
                .clickable(onClick = onTabs),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$tabsCount",
                style = MaterialTheme.typography.labelMedium,
                color = TrekTheme.colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
