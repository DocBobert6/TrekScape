package com.example.ui.tabs

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.BrowserTab
import com.example.ui.browser.BrowserViewModel
import com.example.ui.theme.TrekTheme

@Composable
fun TabOverviewScreen(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val tabs by viewModel.tabs.collectAsState()
    val activeTabId by viewModel.activeTabId.collectAsState()
    val isPrivateMode by viewModel.isPrivateMode.collectAsState()
    val activeGroup by viewModel.activeGroup.collectAsState()
    val tabGroups by viewModel.tabGroups.collectAsState()
    val recentlyClosed by viewModel.recentlyClosedTabs.collectAsState()

    var showRecentlyClosedDialog by remember { mutableStateOf(false) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var tabToMoveGroup by remember { mutableStateOf<BrowserTab?>(null) }

    // Filter tabs by private mode and selected group
    val displayTabs = tabs.filter { tab ->
        if (isPrivateMode) {
            tab.isPrivate
        } else {
            !tab.isPrivate && (activeGroup == "All" || tab.groupName == activeGroup)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TrekTheme.colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Private mode toggle button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isPrivateMode) TrekTheme.colors.accent.copy(alpha = 0.2f)
                            else TrekTheme.colors.surfaceVariant
                        )
                        .clickable { viewModel.togglePrivateMode() }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isPrivateMode) Icons.Outlined.Shield else Icons.Outlined.Lock,
                        contentDescription = stringResource(R.string.private_mode),
                        tint = if (isPrivateMode) TrekTheme.colors.accent else TrekTheme.colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPrivateMode) "Private Mode" else "Standard",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isPrivateMode) TrekTheme.colors.accent else TrekTheme.colors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (recentlyClosed.isNotEmpty()) {
                        IconButton(onClick = { showRecentlyClosedDialog = true }) {
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = stringResource(R.string.recently_closed),
                                tint = TrekTheme.colors.textSecondary
                            )
                        }
                    }

                    TextButton(onClick = { viewModel.setTabOverviewOpen(false) }) {
                        Text(
                            text = stringResource(R.string.done),
                            style = MaterialTheme.typography.titleMedium,
                            color = TrekTheme.colors.accent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Tab Groups bar (visible in non-private mode)
            if (!isPrivateMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ScrollableTabRow(
                        selectedTabIndex = tabGroups.indexOf(activeGroup).coerceAtLeast(0),
                        edgePadding = 0.dp,
                        containerColor = Color.Transparent,
                        divider = {},
                        indicator = {},
                        modifier = Modifier.weight(1f)
                    ) {
                        tabGroups.forEach { group ->
                            val isSelected = group == activeGroup
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectGroup(group) },
                                label = { Text(group) },
                                modifier = Modifier.padding(end = 8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TrekTheme.colors.accent,
                                    selectedLabelColor = Color.White,
                                    containerColor = TrekTheme.colors.surfaceVariant,
                                    labelColor = TrekTheme.colors.textPrimary
                                )
                            )
                        }
                    }

                    IconButton(onClick = { showCreateGroupDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = stringResource(R.string.create_group),
                            tint = TrekTheme.colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 2-Column Grid of live Tab cards
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayTabs, key = { it.id }) { tab ->
                    TabCard(
                        tab = tab,
                        isActive = tab.id == activeTabId,
                        onSelect = { viewModel.selectTab(tab.id) },
                        onClose = { viewModel.closeTab(tab.id) },
                        onDuplicate = { viewModel.duplicateTab(tab) },
                        onTogglePin = { viewModel.togglePinTab(tab.id) },
                        onMoveGroup = { tabToMoveGroup = tab },
                        onCloseOthers = { viewModel.closeOtherTabs(tab.id) }
                    )
                }
            }
        }

        // Bottom Action Bar: Close All, + New Tab
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(TrekTheme.colors.barBackground)
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            TextButton(
                onClick = { viewModel.closeAllTabs(isPrivateMode) },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Text(
                    text = stringResource(R.string.close_all_tabs),
                    color = TrekTheme.colors.textSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(TrekTheme.colors.accent)
                    .clickable { viewModel.openTab(isPrivate = isPrivateMode) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.new_tab),
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }

    // Recently Closed Tabs Dialog
    if (showRecentlyClosedDialog) {
        Dialog(onDismissRequest = { showRecentlyClosedDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TrekTheme.colors.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = stringResource(R.string.recently_closed),
                        style = MaterialTheme.typography.titleLarge,
                        color = TrekTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    recentlyClosed.take(8).forEach { tab ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.restoreRecentlyClosedTab(tab)
                                    showRecentlyClosedDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Restore,
                                contentDescription = null,
                                tint = TrekTheme.colors.accent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tab.title.ifBlank { tab.url },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TrekTheme.colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = tab.url,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TrekTheme.colors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { showRecentlyClosedDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(stringResource(R.string.done), color = TrekTheme.colors.accent)
                    }
                }
            }
        }
    }

    // Move to Group Dialog
    tabToMoveGroup?.let { targetTab ->
        Dialog(onDismissRequest = { tabToMoveGroup = null }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TrekTheme.colors.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Move Tab to Group",
                        style = MaterialTheme.typography.titleMedium,
                        color = TrekTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    tabGroups.filter { it != "All" }.forEach { group ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.moveTabToGroup(targetTab.id, group)
                                    tabToMoveGroup = null
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = group,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TrekTheme.colors.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            if (targetTab.groupName == group) {
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

    // Create Group Dialog
    if (showCreateGroupDialog) {
        var newGroupName by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showCreateGroupDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TrekTheme.colors.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "New Tab Group",
                        style = MaterialTheme.typography.titleMedium,
                        color = TrekTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = newGroupName,
                        onValueChange = { newGroupName = it },
                        label = { Text("Group Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showCreateGroupDialog = false }) {
                            Text("Cancel", color = TrekTheme.colors.textSecondary)
                        }
                        TextButton(
                            onClick = {
                                viewModel.addGroup(newGroupName)
                                showCreateGroupDialog = false
                            }
                        ) {
                            Text("Create", color = TrekTheme.colors.accent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabCard(
    tab: BrowserTab,
    isActive: Boolean,
    onSelect: () -> Unit,
    onClose: () -> Unit,
    onDuplicate: () -> Unit,
    onTogglePin: () -> Unit,
    onMoveGroup: () -> Unit,
    onCloseOthers: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = if (isActive) TrekTheme.colors.accent else TrekTheme.colors.separator,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(containerColor = TrekTheme.colors.cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 4.dp else 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Tab Card Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (tab.isPinned) {
                    Icon(
                        imageVector = Icons.Outlined.PushPin,
                        contentDescription = "Pinned",
                        tint = TrekTheme.colors.accent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = tab.title.ifBlank { "TrekScape" },
                    style = MaterialTheme.typography.labelMedium,
                    color = TrekTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                )

                // Menu Trigger
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "Tab options",
                            tint = TrekTheme.colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Duplicate") },
                            onClick = { onDuplicate(); showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text(if (tab.isPinned) "Unpin Tab" else "Pin Tab") },
                            onClick = { onTogglePin(); showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Move to Group") },
                            onClick = { onMoveGroup(); showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Close Others") },
                            onClick = { onCloseOthers(); showMenu = false }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Close Button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.close_tab),
                        tint = TrekTheme.colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Thumbnail or Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .background(TrekTheme.colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (tab.thumbnailBitmap != null) {
                    Image(
                        bitmap = tab.thumbnailBitmap!!.asImageBitmap(),
                        contentDescription = tab.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Explore,
                            contentDescription = null,
                            tint = TrekTheme.colors.accent.copy(alpha = 0.4f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (tab.url.isBlank()) "Start Page" else tab.url.removePrefix("https://").removePrefix("http://"),
                            style = MaterialTheme.typography.bodySmall,
                            color = TrekTheme.colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
