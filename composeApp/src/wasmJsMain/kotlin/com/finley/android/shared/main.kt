package com.finley.android.shared

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.finley.android.shared.di.initKoin
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import org.jetbrains.compose.resources.configureWebResources

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // Explicitly mapping to ensure resources are found at the root distribution level
    configureWebResources {
        resourcePathMapping { path -> "./$path" }
    }
    initKoin()
    val body = document.body ?: return
    val container = document.getElementById("compose-target") ?: body
    
    ComposeViewport(container) {
        App()
    }
}
