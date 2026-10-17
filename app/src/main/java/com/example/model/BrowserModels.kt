package com.example.model

import android.graphics.Bitmap
import android.webkit.WebView
import java.util.UUID

data class BrowserTab(
    val id: String = UUID.randomUUID().toString(),
    var title: String = "Start Page",
    var url: String = "",
    var canGoBack: Boolean = false,
    var canGoForward: Boolean = false,
    var isLoading: Boolean = false,
    var progress: Float = 0f,
    var favicon: Bitmap? = null,
    var isPrivate: Boolean = false,
    var groupName: String = "Default",
    var isPinned: Boolean = false,
    var isSecure: Boolean = true,
    var hasCertError: Boolean = false,
    var readerArticle: ReaderArticle? = null,
    var thumbnailBitmap: Bitmap? = null,
    var desktopMode: Boolean = false,
    var zoomPercent: Int = 100,
    var webView: WebView? = null
)

data class ReaderArticle(
    val title: String,
    val byline: String,
    val leadImage: String,
    val readingTimeMinutes: Int,
    val wordCount: Int,
    val contentHtml: String,
    val url: String,
    val siteName: String
)

enum class SecurityState {
    SECURE,
    NOT_SECURE,
    DANGEROUS
}

data class ContextMenuTarget(
    val type: ContextMenuType,
    val linkUrl: String? = null,
    val imageUrl: String? = null,
    val titleOrAlt: String? = null
)

enum class ContextMenuType {
    LINK,
    IMAGE,
    IMAGE_LINK
}

data class JsDialogRequest(
    val type: JsDialogType,
    val url: String,
    val message: String,
    val defaultValue: String = "",
    val onConfirm: (String) -> Unit,
    val onCancel: () -> Unit
)

enum class JsDialogType {
    ALERT,
    CONFIRM,
    PROMPT
}

data class PermissionRequestItem(
    val origin: String,
    val resources: List<String>,
    val onGrant: () -> Unit,
    val onDeny: () -> Unit
)

data class CertErrorRequest(
    val url: String,
    val primaryError: String,
    val onProceed: () -> Unit,
    val onCancel: () -> Unit
)
