package com.finley.android.shared.logic

import java.util.prefs.Preferences

class JvmLocalSettings : LocalSettings {
    private val prefs = Preferences.userRoot().node("com.finley.android.24bestpoint.prefs")

    override fun putString(key: String, value: String) {
        prefs.put(key, value)
    }

    override fun getStringOrNull(key: String): String? {
        return prefs.get(key, null)
    }

    override fun remove(key: String) {
        prefs.remove(key)
    }
}
