package com.example.ui.browser

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
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
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                cacheMode = WebSettings.LOAD_DEFAULT
            }

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
        }

        onDispose {
            webView.stopLoading()
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
