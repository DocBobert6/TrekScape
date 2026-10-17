package com.example.ui.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.HistoryEntity
import com.example.data.local.entity.ReadingListEntity
import com.example.ui.browser.BrowserViewModel
import com.example.ui.theme.TrekTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: BrowserViewModel,
    initialTab: Int = 0,
    onNavigate: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TrekTheme.colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when (selectedTab) {
                        0 -> stringResource(R.string.bookmarks)
                        1 -> stringResource(R.string.reading_list)
                        else -> stringResource(R.string.history)
                    },
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

            // Three-Segment Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = TrekTheme.colors.surfaceVariant,
                contentColor = TrekTheme.colors.accent,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.BookmarkBorder, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Bookmarks")
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reading List")
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.History, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("History")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (selectedTab) {
                0 -> BookmarksTabContent(viewModel, onNavigate)
                1 -> ReadingListTabContent(viewModel, onNavigate)
                2 -> HistoryTabContent(viewModel, onNavigate)
            }
        }
    }
}

@Composable
fun BookmarksTabContent(
    viewModel: BrowserViewModel,
    onNavigate: (String) -> Unit
) {
    val bookmarks by viewModel.bookmarkRepository.bookmarks.collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    if (bookmarks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.no_bookmarks),
                color = TrekTheme.colors.textSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(bookmarks, key = { it.id }) { bookmark ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onNavigate(bookmark.url) },
                    colors = CardDefaults.cardColors(containerColor = TrekTheme.colors.cardBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
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
                                imageVector = Icons.Outlined.Bookmark,
                                contentDescription = null,
                                tint = TrekTheme.colors.accent,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = bookmark.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TrekTheme.colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = bookmark.url,
                                style = MaterialTheme.typography.bodySmall,
                                color = TrekTheme.colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.bookmarkRepository.removeBookmarkById(bookmark.id)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete",
                                tint = TrekTheme.colors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReadingListTabContent(
    viewModel: BrowserViewModel,
    onNavigate: (String) -> Unit
) {
    val readingList by viewModel.readingListRepository.readingList.collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    if (readingList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.no_reading_list),
                color = TrekTheme.colors.textSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(readingList, key = { it.id }) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onNavigate(item.url) },
                    colors = CardDefaults.cardColors(containerColor = TrekTheme.colors.cardBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.readingListRepository.toggleReadStatus(item.id, item.isRead)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (item.isRead) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                contentDescription = if (item.isRead) "Mark unread" else "Mark read",
                                tint = if (item.isRead) TrekTheme.colors.accent else TrekTheme.colors.textSecondary
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TrekTheme.colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = if (!item.isRead) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = "${item.readingTimeMinutes} min read • ${item.url}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TrekTheme.colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.readingListRepository.removeItem(item.id)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete",
                                tint = TrekTheme.colors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryTabContent(
    viewModel: BrowserViewModel,
    onNavigate: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val historyList by if (searchQuery.isBlank()) {
        viewModel.historyRepository.allHistory.collectAsState(initial = emptyList())
    } else {
        viewModel.historyRepository.search(searchQuery).collectAsState(initial = emptyList())
    }

    var showClearMenu by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Clear Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search History") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = TrekTheme.colors.textSecondary) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Outlined.Clear, contentDescription = "Clear search", tint = TrekTheme.colors.textSecondary)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Box {
                TextButton(onClick = { showClearMenu = true }) {
                    Text("Clear", color = TrekTheme.colors.accent, fontWeight = FontWeight.SemiBold)
                }

                DropdownMenu(
                    expanded = showClearMenu,
                    onDismissRequest = { showClearMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.clear_history_last_hour)) },
                        onClick = {
                            coroutineScope.launch { viewModel.historyRepository.clearHistoryLastHour() }
                            showClearMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.clear_history_today)) },
                        onClick = {
                            coroutineScope.launch { viewModel.historyRepository.clearHistoryToday() }
                            showClearMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.clear_history_all_time)) },
                        onClick = {
                            coroutineScope.launch { viewModel.historyRepository.clearAllHistory() }
                            showClearMenu = false
                        }
                    )
                }
            }
        }

        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_history),
                    color = TrekTheme.colors.textSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            val dateFormat = remember { SimpleDateFormat("EEEE, MMM d", Locale.getDefault()) }
            val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(historyList, key = { it.id }) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onNavigate(item.url) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title.ifBlank { item.url },
                                style = MaterialTheme.typography.bodyMedium,
                                color = TrekTheme.colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${timeFormat.format(Date(item.timestamp))} • ${item.url}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TrekTheme.colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = {
                                coroutineScope.launch { viewModel.historyRepository.deleteHistory(item.id) }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete",
                                tint = TrekTheme.colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
