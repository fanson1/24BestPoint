package com.finley.android.shared.di

import com.finley.android.shared.logic.JvmLocalSettings
import com.finley.android.shared.logic.LocalSettings
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    println("Koin: Registering platformModule (JVM)")
    single<LocalSettings> { 
        println("Koin: Creating JvmLocalSettings")
        JvmLocalSettings() 
    }
}
