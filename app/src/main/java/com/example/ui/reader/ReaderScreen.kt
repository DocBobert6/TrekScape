package com.example.ui.reader

import android.text.Html
import android.widget.TextView
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.ui.browser.BrowserViewModel
import com.example.ui.theme.TrekTheme

@Composable
fun ReaderScreen(
    viewModel: BrowserViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val article by viewModel.activeReaderArticle.collectAsState()
    val readerTheme by viewModel.readerTheme.collectAsState()
    val fontSize by viewModel.readerFontSize.collectAsState()
    val fontFamilyName by viewModel.readerFontFamily.collectAsState()

    var showControls by remember { mutableStateOf(false) }

    // Reader background and text colors
    val (bgColor, textColor, surfaceColor) = when (readerTheme) {
        "white" -> Triple(Color(0xFFFFFFFF), Color(0xFF1E293B), Color(0xFFF1F5F9))
        "sepia" -> Triple(Color(0xFFF8F1E3), Color(0xFF3F352B), Color(0xFFEBE0CE))
        "slate" -> Triple(Color(0xFF263238), Color(0xFFECEFF1), Color(0xFF37474F))
        else -> Triple(Color(0xFF000000), Color(0xFFE2E8F0), Color(0xFF18181B)) // Black
    }

    val selectedFontFamily = when (fontFamilyName) {
        "sans" -> FontFamily.SansSerif
        "mono" -> FontFamily.Monospace
        else -> FontFamily.Serif
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Article Header
            if (article != null) {
                Text(
                    text = article!!.title,
                    fontFamily = selectedFontFamily,
                    fontSize = (fontSize + 10).sp,
                    lineHeight = (fontSize + 16).sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                Spacer(modifier = Modifier.height(12.dp))

                val metaInfo = buildString {
                    if (article!!.byline.isNotBlank()) append(article!!.byline).append(" • ")
                    append("${article!!.readingTimeMinutes} min read")
                    if (article!!.siteName.isNotBlank()) append(" • ").append(article!!.siteName)
                }

                Text(
                    text = metaInfo,
                    fontFamily = selectedFontFamily,
                    fontSize = 14.sp,
                    color = textColor.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Clean Article HTML Body rendered into formatted TextView
                AndroidView(
                    factory = { context ->
                        TextView(context).apply {
                            setTextColor(textColor.toArgb())
                            textSize = fontSize.toFloat()
                            setLineSpacing(12f, 1.25f)
                            text = Html.fromHtml(article!!.contentHtml, Html.FROM_HTML_MODE_COMPACT)
                        }
                    },
                    update = { textView ->
                        textView.setTextColor(textColor.toArgb())
                        textView.textSize = fontSize.toFloat()
                        textView.text = Html.fromHtml(article!!.contentHtml, Html.FROM_HTML_MODE_COMPACT)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(100.dp))
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Reader View is extracting article content...",
                        color = textColor,
                        fontFamily = selectedFontFamily
                    )
                }
            }
        }

        // Top Navigation Bar (Close + Typography Controls)
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(bgColor.copy(alpha = 0.95f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Close Reader",
                    tint = textColor
                )
            }

            IconButton(onClick = { showControls = !showControls }) {
                Icon(
                    imageVector = Icons.Outlined.FormatSize,
                    contentDescription = "Typography Controls",
                    tint = textColor
                )
            }
        }

        // Reader Controls Drawer / Card
        if (showControls) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 60.dp, end = 16.dp)
                    .width(280.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Theme color swatches
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(
                            "white" to Color(0xFFFFFFFF),
                            "sepia" to Color(0xFFF8F1E3),
                            "slate" to Color(0xFF263238),
                            "black" to Color(0xFF000000)
                        ).forEach { (name, color) ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { viewModel.setReaderTheme(name) }
                            )
                        }
                    }

                    // Font Family choices
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("serif" to "Serif", "sans" to "Sans", "mono" to "Mono").forEach { (id, label) ->
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (fontFamilyName == id) TrekTheme.colors.accent else textColor,
                                fontWeight = if (fontFamilyName == id) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setReaderFontFamily(id) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Font Size Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("A", fontSize = 12.sp, color = textColor)
                        Slider(
                            value = fontSize.toFloat(),
                            onValueChange = { viewModel.setReaderFontSize(it.toInt()) },
                            valueRange = 14f..28f,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )
                        Text("A", fontSize = 22.sp, color = textColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
