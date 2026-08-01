package com.finley.android.shared.di

import com.finley.android.shared.logic.AndroidLocalSettings
import com.finley.android.shared.logic.LocalSettings
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<LocalSettings> { AndroidLocalSettings(get()) }
}
