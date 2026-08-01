package com.finley.android.shared.logic

import platform.Foundation.NSUserDefaults

class IosLocalSettings : LocalSettings {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun putString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }

    override fun getStringOrNull(key: String): String? {
        return defaults.stringForKey(key)
    }

    override fun remove(key: String) {
        defaults.removeObjectForKey(key)
    }
}
