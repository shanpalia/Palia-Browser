package com.example.ui.browser

import android.Manifest
import android.os.Build
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.SslErrorHandler
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.ValueCallback
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.browser.model.BrowserTab
import com.example.ui.BrowserViewModel
import com.example.ui.WebCommand
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserWebView(
    tab: BrowserTab,
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var pendingWebPermissionRequest by remember { mutableStateOf<PermissionRequest?>(null) }
    var pendingGeoOrigin by remember { mutableStateOf<String?>(null) }
    var pendingGeoCallback by remember { mutableStateOf<GeolocationPermissions.Callback?>(null) }
    var pendingFileChooser by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }

    val fileChooserLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        pendingFileChooser?.onReceiveValue(uris.takeIf { it.isNotEmpty() }?.toTypedArray())
        pendingFileChooser = null
    }

    val webPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val request = pendingWebPermissionRequest
        pendingWebPermissionRequest = null
        if (request != null) {
            val requestedResources = request.resources.toSet()
            val requiredPermissions = buildList {
                if (PermissionRequest.RESOURCE_VIDEO_CAPTURE in requestedResources) add(Manifest.permission.CAMERA)
                if (PermissionRequest.RESOURCE_AUDIO_CAPTURE in requestedResources) add(Manifest.permission.RECORD_AUDIO)
            }
            if (requiredPermissions.isNotEmpty() && requiredPermissions.all { permission ->
                    results[permission] == true ||
                        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
                }) {
                val grantedResources = request.resources.filter { resource ->
                    resource == PermissionRequest.RESOURCE_VIDEO_CAPTURE ||
                        resource == PermissionRequest.RESOURCE_AUDIO_CAPTURE
                }
                if (grantedResources.isNotEmpty()) request.grant(grantedResources.toTypedArray()) else request.deny()
            } else {
                request.deny()
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val origin = pendingGeoOrigin
        val callback = pendingGeoCallback
        pendingGeoOrigin = null
        pendingGeoCallback = null
        if (origin != null && callback != null) {
            val hasLocation = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                results[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            callback.invoke(origin, hasLocation, false)
        }
    }

    val webView = remember(tab.id) {
        WebView(context).apply {
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
                mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                allowFileAccess = false
                allowContentAccess = true
                javaScriptCanOpenWindowsAutomatically = false
                setSupportMultipleWindows(false)
                cacheMode = WebSettings.LOAD_DEFAULT
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) safeBrowsingEnabled = true
            }

            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

            setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                viewModel.onDownloadDetected(url, contentDisposition, mimetype, contentLength)
            }
        }
    }

    // Configure desktop mode and incognito settings
    LaunchedEffect(tab.isDesktopMode) {
        val desktopAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
        if (tab.isDesktopMode) {
            webView.settings.userAgentString = desktopAgent
            webView.settings.useWideViewPort = true
            webView.settings.loadWithOverviewMode = true
        } else {
            webView.settings.userAgentString = null
        }
    }

    LaunchedEffect(tab.isIncognito) {
        if (tab.isIncognito) {
            webView.settings.saveFormData = false
            webView.clearCache(true)
            webView.clearHistory()
            CookieManager.getInstance().setAcceptCookie(false)
        } else {
            CookieManager.getInstance().setAcceptCookie(true)
        }
    }

    // Connect WebViewClient and WebChromeClient
    DisposableEffect(tab.id) {
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                url?.let {
                    viewModel.updateActiveTabState(
                        url = it,
                        isLoading = true,
                        canGoBack = view?.canGoBack(),
                        canGoForward = view?.canGoForward()
                    )
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                viewModel.updateActiveTabState(
                    title = view?.title ?: "Page",
                    url = url ?: view?.url,
                    isLoading = false,
                    canGoBack = view?.canGoBack(),
                    canGoForward = view?.canGoForward()
                )
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                // Never let users bypass invalid certificates.
                handler?.cancel()
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val uri = request?.url ?: return false
                val scheme = uri.scheme ?: return false

                if (scheme == "http" || scheme == "https") {
                    return false
                }

                // Handle intent, mailto, tel, etc.
                try {
                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    return true
                } catch (_: Exception) {
                    return true
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                if (filePathCallback == null) return false
                pendingFileChooser?.onReceiveValue(null)
                pendingFileChooser = filePathCallback
                val acceptedTypes = fileChooserParams?.acceptTypes
                    ?.filter { it.isNotBlank() && it != "*/*" }
                    ?.toTypedArray()
                    ?.takeIf { it.isNotEmpty() } ?: arrayOf("*/*")
                return try {
                    fileChooserLauncher.launch(acceptedTypes)
                    true
                } catch (_: Exception) {
                    pendingFileChooser?.onReceiveValue(null)
                    pendingFileChooser = null
                    false
                }
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                viewModel.updateActiveTabState(
                    progress = newProgress,
                    isLoading = newProgress < 100
                )
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                title?.let {
                    viewModel.updateActiveTabState(title = it)
                }
            }

            override fun onPermissionRequest(request: PermissionRequest?) {
                if (request == null || request.origin?.scheme != "https") {
                    request?.deny()
                    return
                }
                val resources = request.resources.toSet()
                val requiredPermissions = buildList {
                    if (PermissionRequest.RESOURCE_VIDEO_CAPTURE in resources) add(Manifest.permission.CAMERA)
                    if (PermissionRequest.RESOURCE_AUDIO_CAPTURE in resources) add(Manifest.permission.RECORD_AUDIO)
                }
                val supportedResources = request.resources.filter {
                    it == PermissionRequest.RESOURCE_VIDEO_CAPTURE ||
                        it == PermissionRequest.RESOURCE_AUDIO_CAPTURE
                }
                if (requiredPermissions.isEmpty() || supportedResources.isEmpty()) {
                    request.deny()
                    return
                }
                val missingPermissions = requiredPermissions.filter { permission ->
                    ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
                }
                if (missingPermissions.isEmpty()) {
                    request.grant(supportedResources.toTypedArray())
                } else {
                    pendingWebPermissionRequest?.deny()
                    pendingWebPermissionRequest = request
                    webPermissionLauncher.launch(missingPermissions.toTypedArray())
                }
            }

            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: GeolocationPermissions.Callback?
            ) {
                if (origin.isNullOrBlank() || callback == null || !origin.startsWith("https://", ignoreCase = true)) {
                    callback?.invoke(origin ?: "", false, false)
                    return
                }
                val hasLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                if (hasLocation) {
                    callback.invoke(origin, true, false)
                } else {
                    pendingGeoOrigin = origin
                    pendingGeoCallback = callback
                    locationPermissionLauncher.launch(arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ))
                }
            }
        }

        onDispose {
            webView.stopLoading()
            pendingFileChooser?.onReceiveValue(null)
            pendingFileChooser = null
        }
    }

    // Collect web commands (Back, Forward, Reload, LoadUrl, Find)
    LaunchedEffect(tab.id) {
        viewModel.webCommands.collectLatest { command ->
            when (command) {
                is WebCommand.LoadUrl -> {
                    webView.loadUrl(command.url)
                }
                is WebCommand.GoBack -> {
                    if (webView.canGoBack()) {
                        webView.goBack()
                    } else {
                        viewModel.goHome()
                    }
                }
                is WebCommand.GoForward -> {
                    if (webView.canGoForward()) {
                        webView.goForward()
                    }
                }
                is WebCommand.Reload -> {
                    webView.reload()
                }
                is WebCommand.Stop -> {
                    webView.stopLoading()
                }
                is WebCommand.FindInPage -> {
                    if (command.query.isNotBlank()) {
                        webView.findAllAsync(command.query)
                        webView.findNext(command.forward)
                    }
                }
                is WebCommand.ClearMatches -> {
                    webView.clearMatches()
                }
            }
        }
    }

    // Load initial URL if not about:home
    LaunchedEffect(tab.url) {
        if (!tab.isHome && webView.url != tab.url) {
            webView.loadUrl(tab.url)
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier.fillMaxSize()
    )
}
