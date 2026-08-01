package com.finley.android.shared.di

import com.finley.android.shared.data.repository.GameRepository
import com.finley.android.shared.logic.UserManager
import com.finley.android.shared.ui.auth.logic.AuthViewModel
import com.finley.android.shared.ui.game.logic.GameViewModel
import com.finley.android.shared.ui.leaderboard.logic.LeaderboardViewModel
import com.finley.android.shared.ui.profile.logic.ProfileViewModel
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(commonModule(), platformModule())
    }

fun commonModule() = module {
    println("Koin: Registering commonModule")
    
    single<HttpClient> {
        HttpClient {
            install(ContentNegotiation) {
                json(Json { 
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
        }
    }
    
    single<GameRepository> { GameRepository(get()) }
    
    single<UserManager> { UserManager(get(), get()) }
    
    factory { AuthViewModel(get(), get()) }
    factory { GameViewModel(get(), get()) }
    factory { LeaderboardViewModel(get(), get()) }
    factory { ProfileViewModel(get(), get()) }
}

expect fun platformModule(): Module
