package com.example.ui.browser

import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.webkit.URLUtil
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.ContextMenuTarget
import com.example.model.ContextMenuType
import com.example.ui.menu.MenuActionRow
import com.example.ui.theme.TrekTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContextMenuSheet(
    target: ContextMenuTarget,
    viewModel: BrowserViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val targetUrl = target.linkUrl ?: target.imageUrl.orEmpty()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = TrekTheme.colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Target URL Header
            Text(
                text = when (target.type) {
                    ContextMenuType.LINK -> "Link Options"
                    ContextMenuType.IMAGE -> "Image Options"
                    ContextMenuType.IMAGE_LINK -> "Image Link Options"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TrekTheme.colors.textPrimary
            )

            Text(
                text = targetUrl,
                style = MaterialTheme.typography.bodySmall,
                color = TrekTheme.colors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            // Link Actions
            if (target.type == ContextMenuType.LINK || target.type == ContextMenuType.IMAGE_LINK) {
                target.linkUrl?.let { link ->
                    MenuActionRow(
                        icon = Icons.Outlined.OpenInBrowser,
                        title = "Open",
                        onClick = {
                            viewModel.loadUrl(link)
                            onDismiss()
                        }
                    )

                    MenuActionRow(
                        icon = Icons.Outlined.OpenInNew,
                        title = "Open in New Tab",
                        onClick = {
                            viewModel.openTab(url = link)
                            onDismiss()
                        }
                    )

                    MenuActionRow(
                        icon = Icons.Outlined.OpenInNew,
                        title = "Open in Background",
                        onClick = {
                            val activeId = viewModel.activeTabId.value
                            viewModel.openTab(url = link)
                            viewModel.selectTab(activeId)
                            Toast.makeText(context, "Opened in background", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    )

                    MenuActionRow(
                        icon = Icons.Outlined.MenuBook,
                        title = "Add to Reading List",
                        onClick = {
                            coroutineScope.launch {
                                viewModel.readingListRepository.addToReadingList(
                                    title = link,
                                    url = link
                                )
                                Toast.makeText(context, "Added to Reading List", Toast.LENGTH_SHORT).show()
                            }
                            onDismiss()
                        }
                    )

                    MenuActionRow(
                        icon = Icons.Outlined.ContentCopy,
                        title = "Copy Link",
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("URL", link))
                            Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    )

                    MenuActionRow(
                        icon = Icons.Outlined.Share,
                        title = "Share Link",
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, link)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Link via TrekScape"))
                            onDismiss()
                        }
                    )
                }
            }

            // Image Actions
            if (target.type == ContextMenuType.IMAGE || target.type == ContextMenuType.IMAGE_LINK) {
                target.imageUrl?.let { imgUrl ->
                    MenuActionRow(
                        icon = Icons.Outlined.Download,
                        title = "Save Image",
                        onClick = {
                            try {
                                val request = DownloadManager.Request(Uri.parse(imgUrl)).apply {
                                    setTitle("Image download")
                                    setDescription("Downloading image via TrekScape")
                                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                    setDestinationInExternalPublicDir(
                                        Environment.DIRECTORY_DOWNLOADS,
                                        URLUtil.guessFileName(imgUrl, null, "image/*")
                                    )
                                }
                                val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                dm.enqueue(request)
                                Toast.makeText(context, "Image download started", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
                            }
                            onDismiss()
                        }
                    )

                    MenuActionRow(
                        icon = Icons.Outlined.ContentCopy,
                        title = "Copy Image URL",
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Image URL", imgUrl))
                            Toast.makeText(context, "Image URL copied", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    )

                    MenuActionRow(
                        icon = Icons.Outlined.Share,
                        title = "Share Image",
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, imgUrl)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Image via TrekScape"))
                            onDismiss()
                        }
                    )

                    MenuActionRow(
                        icon = Icons.Outlined.OpenInNew,
                        title = "Open Image in New Tab",
                        onClick = {
                            viewModel.openTab(url = imgUrl)
                            onDismiss()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
