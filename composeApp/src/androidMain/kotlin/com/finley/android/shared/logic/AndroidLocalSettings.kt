package com.finley.android.shared.logic

import android.content.Context
import android.content.SharedPreferences

class AndroidLocalSettings(context: Context) : LocalSettings {
    private val prefs: SharedPreferences = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)

    override fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    override fun getStringOrNull(key: String): String? {
        return prefs.getString(key, null)
    }

    override fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }
}
