package com.example.ui.start

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.HistoryEntity
import com.example.data.local.entity.ReadingListEntity
import com.example.ui.browser.BrowserViewModel
import com.example.ui.theme.TrekTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StartPage(
    viewModel: BrowserViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val preferences by viewModel.preferences.collectAsState()
    val bookmarks by viewModel.bookmarkRepository.bookmarks.collectAsState(initial = emptyList())
    val frequentlyVisited by viewModel.historyRepository.frequentlyVisited.collectAsState(initial = emptyList())
    val readingList by viewModel.readingListRepository.unreadItems.collectAsState(initial = emptyList())
    val blockedCount by viewModel.trackerRepository.totalBlockedCount.collectAsState(initial = 0)

    var showAddFavoriteDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TrekTheme.colors.background),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Hero Compass Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(TrekTheme.colors.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Explore,
                        contentDescription = stringResource(R.string.app_name),
                        tint = TrekTheme.colors.accent,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = stringResource(R.string.start_page_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = TrekTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = stringResource(R.string.start_page_tagline),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TrekTheme.colors.textSecondary
                )
            }
        }

        // Privacy Report Card
        if (preferences.showPrivacyCard) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { viewModel.setPrivacyReportOpen(true) },
                    colors = CardDefaults.cardColors(containerColor = TrekTheme.colors.cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(TrekTheme.colors.accent.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Security,
                                contentDescription = null,
                                tint = TrekTheme.colors.accent,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.privacy_report),
                                style = MaterialTheme.typography.titleMedium,
                                color = TrekTheme.colors.textPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "$blockedCount trackers & ads blocked",
                                style = MaterialTheme.typography.bodySmall,
                                color = TrekTheme.colors.textSecondary
                            )
                        }

                        Icon(
                            imageVector = Icons.Outlined.ChevronRight,
                            contentDescription = null,
                            tint = TrekTheme.colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Favorites Grid
        if (preferences.showFavorites) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.favorites),
                        style = MaterialTheme.typography.titleMedium,
                        color = TrekTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = { showAddFavoriteDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Add Favorite",
                            tint = TrekTheme.colors.accent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    maxItemsInEachRow = 4
                ) {
                    bookmarks.take(8).forEach { bookmark ->
                        FavoriteTile(
                            bookmark = bookmark,
                            onClick = { onNavigate(bookmark.url) }
                        )
                    }
                }
            }
        }

        // Frequently Visited
        if (preferences.showFrequentlyVisited && frequentlyVisited.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.frequently_visited),
                    style = MaterialTheme.typography.titleMedium,
                    color = TrekTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(TrekTheme.colors.cardBg)
                ) {
                    frequentlyVisited.take(5).forEachIndexed { index, history ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigate(history.url) }
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(TrekTheme.colors.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.History,
                                    contentDescription = null,
                                    tint = TrekTheme.colors.accent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = history.title.ifBlank { history.url },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TrekTheme.colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = history.url,
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

        // Reading List Preview
        if (preferences.showReadingList && readingList.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.reading_list),
                        style = MaterialTheme.typography.titleMedium,
                        color = TrekTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    TextButton(onClick = { viewModel.openLibrary(1) }) {
                        Text(
                            text = "See All (${readingList.size})",
                            color = TrekTheme.colors.accent,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(TrekTheme.colors.cardBg)
                ) {
                    readingList.take(3).forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigate(item.url) }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(TrekTheme.colors.accent.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.MenuBook,
                                    contentDescription = null,
                                    tint = TrekTheme.colors.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TrekTheme.colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${item.readingTimeMinutes} min read • ${item.url}",
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

        // Bottom spacing for capsule
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    if (showAddFavoriteDialog) {
        AddFavoriteDialog(
            onDismiss = { showAddFavoriteDialog = false },
            onAdd = { title, url ->
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.bookmarkRepository.addBookmark(title, url, "Favorites")
                }
                showAddFavoriteDialog = false
            }
        )
    }
}

@Composable
fun FavoriteTile(
    bookmark: BookmarkEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val initial = bookmark.title.trim().take(1).uppercase().ifBlank { "?" }

    Column(
        modifier = modifier
            .width(72.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(TrekTheme.colors.cardBg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                style = MaterialTheme.typography.titleLarge,
                color = TrekTheme.colors.accent,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = bookmark.title,
            style = MaterialTheme.typography.bodySmall,
            color = TrekTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AddFavoriteDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, url: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
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
                    text = "Add Favorite",
                    style = MaterialTheme.typography.titleLarge,
                    color = TrekTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL (e.g. https://example.com)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TrekTheme.colors.textSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            if (url.isNotBlank()) {
                                val fixedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
                                onAdd(title.ifBlank { fixedUrl }, fixedUrl)
                            }
                        }
                    ) {
                        Text("Add", color = TrekTheme.colors.accent, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
