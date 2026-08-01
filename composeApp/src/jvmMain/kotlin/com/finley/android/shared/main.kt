package com.finley.android.shared

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.finley.android.shared.di.initKoin

fun main() {
    println("Desktop: Starting application...")
    try {
        // Initialize Koin for Desktop BEFORE application block
        initKoin()
        println("Desktop: Koin initialized.")

        application {
            Window(
                onCloseRequest = ::exitApplication,
                title = "极速24点",
            ) {
                App()
            }
        }
    } catch (e: Exception) {
        println("FATAL ERROR: ${e.message}")
        e.printStackTrace()
    }
}
