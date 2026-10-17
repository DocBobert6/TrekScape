package com.example.ui.privacy

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.browser.BrowserViewModel
import com.example.ui.theme.TrekTheme

@Composable
fun PrivacyReportScreen(
    viewModel: BrowserViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val preferences by viewModel.preferences.collectAsState()
    val totalBlocked by viewModel.trackerRepository.totalBlockedCount.collectAsState(initial = 0)
    val topTrackers by viewModel.trackerRepository.topTrackers.collectAsState(initial = emptyList())
    val topSites by viewModel.trackerRepository.topSites.collectAsState(initial = emptyList())

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TrekTheme.colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
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
                    text = stringResource(R.string.privacy_report),
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

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Three Metric Summary Cards
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricCard(
                            icon = Icons.Outlined.Shield,
                            count = totalBlocked,
                            title = "Trackers & Ads Blocked",
                            description = "Blocked network requests to known ad, tracking, and analytics domains.",
                            iconColor = TrekTheme.colors.accent
                        )

                        MetricCard(
                            icon = Icons.Outlined.LinkOff,
                            count = preferences.paramsStrippedCount,
                            title = "Tracking Parameters Removed",
                            description = "Stripped cross-site tracking tags (e.g. fbclid, gclid, utm_*) while keeping login intact.",
                            iconColor = Color(0xFF10B981)
                        )

                        MetricCard(
                            icon = Icons.Outlined.Fingerprint,
                            count = preferences.fingerprintNeutralizedCount,
                            title = "Fingerprinting Defenses Active",
                            description = "Neutralized canvas, audio, and device hardware profiling signals.",
                            iconColor = Color(0xFF8B5CF6)
                        )
                    }
                }

                // Top Blocked Trackers
                item {
                    Text(
                        text = "Most-Contacted Trackers",
                        style = MaterialTheme.typography.titleMedium,
                        color = TrekTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (topTrackers.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = TrekTheme.colors.cardBg)
                        ) {
                            Box(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "No trackers intercepted yet. As you browse, blocked trackers will appear here.",
                                    color = TrekTheme.colors.textSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(TrekTheme.colors.cardBg)
                        ) {
                            topTrackers.forEach { tracker ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Block,
                                        contentDescription = null,
                                        tint = TrekTheme.colors.accent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = tracker.trackerDomain,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TrekTheme.colors.textPrimary,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${tracker.count}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TrekTheme.colors.textSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Per-site Breakdown
                item {
                    Text(
                        text = "Websites with Most Trackers",
                        style = MaterialTheme.typography.titleMedium,
                        color = TrekTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (topSites.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = TrekTheme.colors.cardBg)
                        ) {
                            Box(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "No sites visited with blocked trackers yet.",
                                    color = TrekTheme.colors.textSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(TrekTheme.colors.cardBg)
                        ) {
                            topSites.forEach { site ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = site.siteDomain.ifBlank { "Direct Requests" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TrekTheme.colors.textPrimary,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${site.count} blocked",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TrekTheme.colors.accent,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    icon: ImageVector,
    count: Int,
    title: String,
    description: String,
    iconColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
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
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TrekTheme.colors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.titleLarge,
                        color = iconColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TrekTheme.colors.textSecondary
                )
            }
        }
    }
}
