package com.finley.android.shared.data.repository

import kotlinx.browser.window

actual val baseUrl: String by lazy {
    val hostname = window.location.hostname
    "http://$hostname:8081/api"
}
