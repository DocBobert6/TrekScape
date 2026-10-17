package com.example.ui.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.TrekTheme

@Composable
fun FindOnPageBar(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val query by viewModel.findQuery.collectAsState()
    val matchCount by viewModel.findMatchCount.collectAsState()
    val activeTab = viewModel.getActiveTab()

    // Trigger findAllAsync when query changes
    LaunchedEffect(query) {
        val wv = activeTab?.webView
        if (wv != null) {
            if (query.isBlank()) {
                wv.clearMatches()
                viewModel.updateFindMatchCount(0, 0)
            } else {
                wv.setFindListener { activeMatchOrdinal, numberOfMatches, _ ->
                    viewModel.updateFindMatchCount(
                        if (numberOfMatches > 0) activeMatchOrdinal + 1 else 0,
                        numberOfMatches
                    )
                }
                wv.findAllAsync(query)
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
            .background(TrekTheme.colors.surface)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.updateFindQuery(it) },
            placeholder = { Text("Find on page...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = TrekTheme.colors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            },
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(24.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        if (query.isNotBlank()) {
            Text(
                text = "${matchCount.first}/${matchCount.second}",
                style = MaterialTheme.typography.bodySmall,
                color = TrekTheme.colors.textSecondary,
                fontWeight = FontWeight.SemiBold
            )

            IconButton(
                onClick = { activeTab?.webView?.findNext(false) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowUp,
                    contentDescription = "Previous match",
                    tint = TrekTheme.colors.textPrimary
                )
            }

            IconButton(
                onClick = { activeTab?.webView?.findNext(true) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = "Next match",
                    tint = TrekTheme.colors.textPrimary
                )
            }
        }

        TextButton(
            onClick = {
                activeTab?.webView?.clearMatches()
                viewModel.closeFindOnPage()
            }
        ) {
            Text(
                text = stringResource(R.string.done),
                color = TrekTheme.colors.accent,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
