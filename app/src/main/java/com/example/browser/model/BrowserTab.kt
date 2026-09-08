package com.example.browser.model

import java.util.UUID

data class BrowserTab(
    val id: String = UUID.randomUUID().toString(),
    val url: String = "about:home",
    val title: String = "New Tab",
    val progress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isLoading: Boolean = false,
    val isDesktopMode: Boolean = false,
    val isIncognito: Boolean = false,
    val findQuery: String = "",
    val isFindActive: Boolean = false
) {
    val isHome: Boolean
        get() = url == "about:home" || url.isBlank()
}
