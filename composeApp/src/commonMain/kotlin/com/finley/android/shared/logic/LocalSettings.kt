package com.finley.android.shared.logic

interface LocalSettings {
    fun putString(key: String, value: String)
    fun getStringOrNull(key: String): String?
    fun remove(key: String)
}

// Simple wrapper if the library actually works later
// class MultiplatformSettingsWrapper(val settings: com.russhwolf.multiplatform.settings.Settings) : LocalSettings { ... }
