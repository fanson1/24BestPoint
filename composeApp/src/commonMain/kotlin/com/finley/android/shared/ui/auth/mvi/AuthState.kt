package com.finley.android.shared.ui.auth.mvi

data class AuthState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isCheckingSession: Boolean = true,
    val isLoginMode: Boolean = true,
    val errorMessage: String? = null
)

sealed interface AuthIntent {
    object Init : AuthIntent
    data class UsernameChanged(val value: String) : AuthIntent
    data class PasswordChanged(val value: String) : AuthIntent
    object ToggleMode : AuthIntent
    object Submit : AuthIntent
}

sealed interface AuthEffect {
    object NavigateToGame : AuthEffect
    data class ShowError(val message: String) : AuthEffect
}
