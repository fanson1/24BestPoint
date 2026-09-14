package com.finley.android.a24bestpoint

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.finley.android.shared.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 强制全屏透明导航栏，消除灰色条
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        // Prevent the system from adding a translucent scrim to the navigation
        // bar so the app's edge-to-edge background shows through it.
        window.isNavigationBarContrastEnforced = false

        setContent {
            App()
        }
    }
}
