package com.finley.android.shared.di

import com.finley.android.shared.logic.LocalSettings
import com.finley.android.shared.logic.WebLocalSettings
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<LocalSettings> { WebLocalSettings() }
}
