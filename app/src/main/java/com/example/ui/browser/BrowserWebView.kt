package com.example.ui.browser

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Environment
import android.os.Message
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.SslErrorHandler
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.example.data.preferences.FingerprintingProtection
import com.example.data.repository.TrackingParamStripper
import com.example.model.BrowserTab
import com.example.model.CertErrorRequest
import com.example.model.ContextMenuTarget
import com.example.model.ContextMenuType
import com.example.model.JsDialogRequest
import com.example.model.JsDialogType
import com.example.model.PermissionRequestItem
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserWebView(
    tab: BrowserTab,
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier,
    onScrollChanged: (scrollY: Int, deltaY: Int) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val preferences = viewModel.preferences

    // File chooser launcher
    val fileChooserCallback = remember { mutableListOf<ValueCallback<Array<Uri>>?>() }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        val cb = if (fileChooserCallback.isNotEmpty()) fileChooserCallback.removeAt(0) else null
        cb?.onReceiveValue(uris.toTypedArray())
    }

    val webView = remember(tab.id) {
        ObservableWebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                useWideViewPort = true
                loadWithOverviewMode = true
                allowContentAccess = true
                allowFileAccess = false
                mediaPlaybackRequiresUserGesture = false
                textZoom = tab.zoomPercent

                if (tab.isPrivate) {
                    cacheMode = WebSettings.LOAD_NO_CACHE
                } else {
                    cacheMode = WebSettings.LOAD_DEFAULT
                }
            }

            // Block third-party cookies by default
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(this, !preferences.value.blockThirdPartyCookies)

            // Algorithmic darkening on Android 13+
            if (preferences.value.darkPagesEnabled) {
                if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                    WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, true)
                }
            }

            // Desktop mode UA
            if (tab.desktopMode) {
                settings.userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
            }

            // Document-start fingerprinting protection injection
            val fpScript = try {
                context.assets.open("fingerprint_protection.js").bufferedReader().use { it.readText() }
            } catch (_: Exception) {
                ""
            }

            if (fpScript.isNotBlank() && WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
                try {
                    WebViewCompat.addDocumentStartJavaScript(this, fpScript, setOf("*"))
                } catch (_: Exception) {}
            }

            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView,
                    request: WebResourceRequest
                ): WebResourceResponse? {
                    val reqUrl = request.url ?: return super.shouldInterceptRequest(view, request)
                    val reqHost = reqUrl.host.orEmpty()
                    val pageHost = Uri.parse(tab.url).host.orEmpty()

                    // Check if protections are disabled for this site
                    val protectionsDisabled = preferences.value.disabledProtectionDomains.contains(pageHost)

                    if (!protectionsDisabled && preferences.value.contentBlockerEnabled && viewModel.contentBlocker.shouldBlock(reqHost, pageHost)) {
                        coroutineScope.launch {
                            viewModel.contentBlocker.recordBlocked(pageHost, reqHost)
                        }
                        return WebResourceResponse(
                            "text/plain",
                            "UTF-8",
                            ByteArrayInputStream(ByteArray(0))
                        )
                    }

                    return super.shouldInterceptRequest(view, request)
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    request: WebResourceRequest
                ): Boolean {
                    val url = request.url?.toString() ?: return false
                    if (url.startsWith("http://") || url.startsWith("https://")) {
                        val host = Uri.parse(url).host.orEmpty()
                        val protectionsDisabled = preferences.value.disabledProtectionDomains.contains(host)

                        var processedUrl = url

                        // HTTPS-Only upgrade
                        if (!protectionsDisabled && preferences.value.httpsOnlyEnabled && processedUrl.startsWith("http://")) {
                            processedUrl = processedUrl.replaceFirst("http://", "https://")
                        }

                        // Tracking query parameter stripping
                        if (!protectionsDisabled && preferences.value.stripTrackingParams) {
                            val stripResult = TrackingParamStripper.stripTrackingParameters(processedUrl)
                            if (stripResult.removedCount > 0) {
                                coroutineScope.launch {
                                    viewModel.preferencesRepository.incrementParamsStripped(stripResult.removedCount)
                                }
                                processedUrl = stripResult.strippedUrl
                            }
                        }

                        if (processedUrl != url) {
                            view.loadUrl(processedUrl)
                            return true
                        }
                        return false
                    }

                    // External schemes (mailto, tel, intent, etc.)
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                        return true
                    } catch (_: Exception) {
                        return true
                    }
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    val isHttps = url?.startsWith("https://") == true
                    viewModel.updateActiveTabState(
                        url = url,
                        isLoading = true,
                        progress = 0.15f,
                        isSecure = isHttps
                    )

                    // Execute fingerprinting protection on page start
                    val currentHost = Uri.parse(url.orEmpty()).host.orEmpty()
                    val protectionsDisabled = preferences.value.disabledProtectionDomains.contains(currentHost)
                    val fpMode = preferences.value.fingerprintingProtection
                    val shouldApplyFp = !protectionsDisabled && when (fpMode) {
                        FingerprintingProtection.ALL -> true
                        FingerprintingProtection.PRIVATE_ONLY -> tab.isPrivate
                        FingerprintingProtection.OFF -> false
                    }

                    if (shouldApplyFp && fpScript.isNotBlank()) {
                        view?.evaluateJavascript(fpScript, null)
                        coroutineScope.launch {
                            viewModel.preferencesRepository.incrementFingerprintNeutralized(1)
                        }
                    }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    val isHttps = url?.startsWith("https://") == true
                    viewModel.updateActiveTabState(
                        title = view?.title,
                        url = url,
                        isLoading = false,
                        progress = 1.0f,
                        canGoBack = view?.canGoBack() ?: false,
                        canGoForward = view?.canGoForward() ?: false,
                        isSecure = isHttps
                    )
                }

                override fun onReceivedSslError(
                    view: WebView?,
                    handler: SslErrorHandler?,
                    error: SslError?
                ) {
                    val primaryError = when (error?.primaryError) {
                        SslError.SSL_UNTRUSTED -> "Untrusted Certificate Authority"
                        SslError.SSL_EXPIRED -> "Expired Certificate"
                        SslError.SSL_IDMISMATCH -> "Hostname Mismatch"
                        SslError.SSL_NOTYETVALID -> "Certificate Not Yet Valid"
                        else -> "SSL Certificate Validation Failed"
                    }

                    viewModel.updateActiveTabState(hasCertError = true, isSecure = false)
                    viewModel.showCertError(
                        CertErrorRequest(
                            url = tab.url,
                            primaryError = primaryError,
                            onProceed = {
                                handler?.proceed()
                                viewModel.dismissCertError()
                            },
                            onCancel = {
                                handler?.cancel()
                                viewModel.dismissCertError()
                            }
                        )
                    )
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    viewModel.updateActiveTabState(
                        progress = newProgress / 100f,
                        isLoading = newProgress < 100
                    )
                }

                override fun onReceivedTitle(view: WebView?, title: String?) {
                    super.onReceivedTitle(view, title)
                    if (!title.isNullOrBlank()) {
                        viewModel.updateActiveTabState(title = title)
                    }
                }

                override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                    super.onReceivedIcon(view, icon)
                    if (icon != null) {
                        viewModel.updateActiveTabState(favicon = icon)
                    }
                }

                override fun onJsAlert(
                    view: WebView?,
                    url: String?,
                    message: String?,
                    result: android.webkit.JsResult?
                ): Boolean {
                    viewModel.showJsDialog(
                        JsDialogRequest(
                            type = JsDialogType.ALERT,
                            url = url.orEmpty(),
                            message = message.orEmpty(),
                            onConfirm = { result?.confirm(); viewModel.dismissJsDialog() },
                            onCancel = { result?.cancel(); viewModel.dismissJsDialog() }
                        )
                    )
                    return true
                }

                override fun onJsConfirm(
                    view: WebView?,
                    url: String?,
                    message: String?,
                    result: android.webkit.JsResult?
                ): Boolean {
                    viewModel.showJsDialog(
                        JsDialogRequest(
                            type = JsDialogType.CONFIRM,
                            url = url.orEmpty(),
                            message = message.orEmpty(),
                            onConfirm = { result?.confirm(); viewModel.dismissJsDialog() },
                            onCancel = { result?.cancel(); viewModel.dismissJsDialog() }
                        )
                    )
                    return true
                }

                override fun onJsPrompt(
                    view: WebView?,
                    url: String?,
                    message: String?,
                    defaultValue: String?,
                    result: android.webkit.JsPromptResult?
                ): Boolean {
                    viewModel.showJsDialog(
                        JsDialogRequest(
                            type = JsDialogType.PROMPT,
                            url = url.orEmpty(),
                            message = message.orEmpty(),
                            defaultValue = defaultValue.orEmpty(),
                            onConfirm = { input -> result?.confirm(input); viewModel.dismissJsDialog() },
                            onCancel = { result?.cancel(); viewModel.dismissJsDialog() }
                        )
                    )
                    return true
                }

                override fun onPermissionRequest(request: PermissionRequest?) {
                    if (request == null) return
                    val origin = request.origin?.toString().orEmpty()
                    val resList = request.resources.toList()
                    viewModel.showPermissionRequest(
                        PermissionRequestItem(
                            origin = origin,
                            resources = resList,
                            onGrant = {
                                request.grant(request.resources)
                                viewModel.dismissPermissionRequest()
                            },
                            onDeny = {
                                request.deny()
                                viewModel.dismissPermissionRequest()
                            }
                        )
                    )
                }

                override fun onGeolocationPermissionsShowPrompt(
                    origin: String?,
                    callback: GeolocationPermissions.Callback?
                ) {
                    viewModel.showPermissionRequest(
                        PermissionRequestItem(
                            origin = origin.orEmpty(),
                            resources = listOf("Geolocation"),
                            onGrant = {
                                callback?.invoke(origin, true, true)
                                viewModel.dismissPermissionRequest()
                            },
                            onDeny = {
                                callback?.invoke(origin, false, false)
                                viewModel.dismissPermissionRequest()
                            }
                        )
                    )
                }

                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                    super.onShowCustomView(view, callback)
                    viewModel.setCustomView(view)
                }

                override fun onHideCustomView() {
                    super.onHideCustomView()
                    viewModel.setCustomView(null)
                }

                override fun onCreateWindow(
                    view: WebView?,
                    isDialog: Boolean,
                    isUserGesture: Boolean,
                    resultMsg: Message?
                ): Boolean {
                    val newTab = viewModel.openTab(url = "", isPrivate = tab.isPrivate, group = tab.groupName)
                    resultMsg?.let { msg ->
                        val transport = msg.obj as? WebView.WebViewTransport
                        transport?.webView = newTab.webView ?: view
                        msg.sendToTarget()
                    }
                    return true
                }

                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    fileChooserCallback.clear()
                    fileChooserCallback.add(filePathCallback)
                    try {
                        filePickerLauncher.launch(arrayOf("*/*"))
                        return true
                    } catch (_: Exception) {
                        return false
                    }
                }
            }

            // Downloads handling
            setDownloadListener { downloadUrl, userAgent, contentDisposition, mimetype, _ ->
                try {
                    val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                        setMimeType(mimetype)
                        addRequestHeader("User-Agent", userAgent)
                        setDescription("Downloading file from TrekScape")
                        setTitle(URLUtil.guessFileName(downloadUrl, contentDisposition, mimetype))
                        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_DOWNLOADS,
                            URLUtil.guessFileName(downloadUrl, contentDisposition, mimetype)
                        )
                    }
                    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    dm.enqueue(request)
                    Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            // Long-press context menu
            setOnLongClickListener {
                val result = hitTestResult
                when (result.type) {
                    WebView.HitTestResult.SRC_ANCHOR_TYPE -> {
                        viewModel.showContextMenu(
                            ContextMenuTarget(
                                type = ContextMenuType.LINK,
                                linkUrl = result.extra
                            )
                        )
                        true
                    }
                    WebView.HitTestResult.IMAGE_TYPE -> {
                        viewModel.showContextMenu(
                            ContextMenuTarget(
                                type = ContextMenuType.IMAGE,
                                imageUrl = result.extra
                            )
                        )
                        true
                    }
                    WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
                        viewModel.showContextMenu(
                            ContextMenuTarget(
                                type = ContextMenuType.IMAGE_LINK,
                                imageUrl = result.extra
                            )
                        )
                        true
                    }
                    else -> false
                }
            }

            setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                onScrollChanged(scrollY, scrollY - oldScrollY)
            }
        }
    }

    LaunchedEffect(webView) {
        tab.webView = webView
    }

    val navTrigger = viewModel.navigationTrigger
    LaunchedEffect(navTrigger.value) {
        val targetUrl = navTrigger.value
        if (!targetUrl.isNullOrBlank() && viewModel.getActiveTab()?.id == tab.id) {
            val domain = Uri.parse(targetUrl).host.orEmpty()
            val protectionsDisabled = preferences.value.disabledProtectionDomains.contains(domain)

            var cleanUrl = targetUrl
            if (!protectionsDisabled && preferences.value.stripTrackingParams) {
                val stripped = TrackingParamStripper.stripTrackingParameters(cleanUrl)
                if (stripped.removedCount > 0) {
                    coroutineScope.launch {
                        viewModel.preferencesRepository.incrementParamsStripped(stripped.removedCount)
                    }
                    cleanUrl = stripped.strippedUrl
                }
            }

            webView.loadUrl(cleanUrl)
            viewModel.clearNavigationTrigger()
        }
    }

    LaunchedEffect(tab.url) {
        if (tab.url.isNotBlank() && webView.url.isNullOrBlank()) {
            val domain = Uri.parse(tab.url).host.orEmpty()
            val protectionsDisabled = preferences.value.disabledProtectionDomains.contains(domain)

            var cleanUrl = tab.url
            if (!protectionsDisabled && preferences.value.stripTrackingParams) {
                val stripped = TrackingParamStripper.stripTrackingParameters(cleanUrl)
                if (stripped.removedCount > 0) {
                    coroutineScope.launch {
                        viewModel.preferencesRepository.incrementParamsStripped(stripped.removedCount)
                    }
                    cleanUrl = stripped.strippedUrl
                }
            }
            webView.loadUrl(cleanUrl)
        }
    }

    DisposableEffect(tab.id) {
        onDispose {
            try {
                if (webView.width > 0 && webView.height > 0) {
                    val bitmap = Bitmap.createBitmap(
                        webView.width.coerceAtMost(480),
                        webView.height.coerceAtMost(800),
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = android.graphics.Canvas(bitmap)
                    webView.draw(canvas)
                    tab.thumbnailBitmap = bitmap
                }
            } catch (_: Exception) {}
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier.fillMaxSize()
    )
}

class ObservableWebView(context: Context) : WebView(context) {
    private var scrollChangeListener: ((Int, Int, Int, Int, Int) -> Unit)? = null

    fun setOnScrollChangeListener(listener: (Int, Int, Int, Int, Int) -> Unit) {
        this.scrollChangeListener = listener
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        scrollChangeListener?.invoke(l, t, oldl, oldt, t - oldt)
    }
}
