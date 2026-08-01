package com.finley.android.shared.logic

import kotlinx.browser.localStorage

class WebLocalSettings : LocalSettings {
    override fun putString(key: String, value: String) {
        localStorage.setItem(key, value)
    }

    override fun getStringOrNull(key: String): String? {
        return localStorage.getItem(key)
    }

    override fun remove(key: String) {
        localStorage.removeItem(key)
    }
}
