package com.finley.android.shared.ui.auth.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finley.android.shared.Res
import com.finley.android.shared.data.repository.GameRepository
import com.finley.android.shared.logic.SecurityUtils
import com.finley.android.shared.logic.UserManager
import com.finley.android.shared.data.model.User
import com.finley.android.shared.ui.auth.logic.AuthViewModel
import com.finley.android.shared.ui.auth.mvi.AuthEffect
import com.finley.android.shared.ui.auth.mvi.AuthIntent
import com.finley.android.shared.auth_error_empty_fields
import com.finley.android.shared.auth_error_network
import com.finley.android.shared.ui.auth.mvi.AuthState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class AuthViewModel(
    private val gameRepository: GameRepository,
    private val userManager: UserManager
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _effect = Channel<AuthEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        println("AuthViewModel: Constructor start")
        // Start init in a background scope to avoid constructor blocking
        viewModelScope.launch {
            println("AuthViewModel: Initializing via Init intent")
            handleIntent(AuthIntent.Init)
        }
    }

    fun handleIntent(intent: AuthIntent) {
        println("AuthViewModel: Handling intent: $intent")
        when (intent) {
            is AuthIntent.Init -> checkAutoLogin()
            is AuthIntent.UsernameChanged -> _state.update { it.copy(username = intent.value) }
            is AuthIntent.PasswordChanged -> _state.update { it.copy(password = intent.value) }
            is AuthIntent.ToggleMode -> _state.update { it.copy(isLoginMode = !it.isLoginMode) }
            is AuthIntent.Submit -> submit()
            is AuthIntent.OfflineMode -> _state.update { it.copy(offlineMode = !it.offlineMode) }
        }
    }

    private fun checkAutoLogin() {
        println("AuthViewModel: Checking auto-login")
        viewModelScope.launch {
            try {
                val savedUsername = userManager.getSavedUsername()
                if (savedUsername != null) {
                    println("AuthViewModel: Attempting auto-login for $savedUsername from Cloud")
                    val user = gameRepository.getUser(savedUsername)
                    if (user != null) {
                        println("AuthViewModel: Cloud user found, auto-logging in")
                        userManager.initUser(user)
                        _effect.send(AuthEffect.NavigateToGame)
                        return@launch
                    } else {
                        println("AuthViewModel: Cloud user not found for $savedUsername")
                        _state.update { it.copy(offlineMode = true) }
                    }
                } else {
                    println("AuthViewModel: No saved username found")
                }
            } catch (e: Exception) {
                println("AuthViewModel: Auto-login error: ${e.message}")
                _state.update { it.copy(offlineMode = true) }
            } finally {
                _state.update { it.copy(isCheckingSession = false) }
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun submit() {
        val currentState = _state.value
        if (currentState.username.isBlank() || currentState.password.isBlank()) {
            viewModelScope.launch { 
                _effect.send(AuthEffect.ShowError(getString(Res.string.auth_error_empty_fields))) 
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                if (currentState.offlineMode) {
                    val hashedPassword = SecurityUtils.sha256(currentState.password)
                    val user = User(
                        username = currentState.username,
                        passwordHash = hashedPassword,
                        nickname = currentState.username,
                        hintCredits = 0,
                        currentLevel = "1",
                        correctCountInLevel = 0,
                        score = 0,
                        isLevelLockActive = false,
                        lockAdsWatched = 0,
                        bestFeverCombo = 0,
                        totalAdsWatched = 0,
                        fastSolveCount = 0,
                        token = Uuid.random().toString()
                    )
                    userManager.login(user)
                    _effect.send(AuthEffect.NavigateToGame)
                } else {
                    val hashedPassword = SecurityUtils.sha256(currentState.password)
                    
                    println("AuthViewModel: Requesting server auth")
                    val response = if (currentState.isLoginMode) {
                        gameRepository.login(currentState.username, hashedPassword)
                    } else {
                        gameRepository.register(currentState.username, hashedPassword)
                    }

                    println("AuthViewModel: Server response: ${response.status}")
                    if (response.status == "success" && response.user != null) {
                        userManager.login(response.user)
                        _effect.send(AuthEffect.NavigateToGame)
                    } else {
                        _effect.send(AuthEffect.ShowError(response.message))
                    }
                }
            } catch (e: Exception) {
                println("AuthViewModel: Submit error: ${e.message}")
                if (!currentState.offlineMode) {
                    _effect.send(AuthEffect.ShowError(getString(Res.string.auth_error_network)))
                }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}
