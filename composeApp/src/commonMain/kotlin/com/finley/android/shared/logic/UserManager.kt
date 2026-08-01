package com.finley.android.shared.logic

import com.finley.android.shared.data.model.User
import com.finley.android.shared.data.repository.GameRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class UserManager(
    private val settings: LocalSettings,
    private val gameRepository: GameRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var sessionJob: Job? = null

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    private val _authEvents = MutableSharedFlow<AuthEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val authEvents: SharedFlow<AuthEvent> = _authEvents.asSharedFlow()

    private val json = Json { ignoreUnknownKeys = true }

    init {
        // 启动时尝试从本地设置恢复完整的用户信息
        val savedUserJson = settings.getStringOrNull(KEY_USER_DATA)
        if (savedUserJson != null) {
            try {
                val user = json.decodeFromString<User>(savedUserJson)
                _currentUser.value = user
                gameRepository.setAuthToken(user.token)
                startSessionWatcher()
            } catch (e: Exception) {
                println("UserManager: Failed to restore user: ${e.message}")
            }
        }
        
        // 绑定 Repository 的回调
        gameRepository.onUnauthorized = {
            println("UserManager: Received unauthorized signal, forcing logout")
            forceLogout("Session expired or logged in on another device")
        }
    }

    fun login(user: User) {
        println("UserManager: Logging in ${user.username}")
        _currentUser.value = user
        saveUserToLocal(user)
        gameRepository.setAuthToken(user.token)
        startSessionWatcher()
    }

    fun logout() {
        println("UserManager: Logout triggered")
        sessionJob?.cancel()
        _currentUser.value = null
        settings.remove(KEY_USER_DATA)
        gameRepository.setAuthToken(null)
    }
    
    fun forceLogout(reason: String) {
        println("UserManager: Force logout triggered. Reason: $reason")
        logout()
        _authEvents.tryEmit(AuthEvent.LoggedOut(reason))
    }

    fun updateCurrentUser(user: User) {
        _currentUser.update { current ->
            if (current?.username == user.username) {
                saveUserToLocal(user)
                if (user.token != current.token) {
                    gameRepository.setAuthToken(user.token)
                }
                user
            } else current
        }
    }

    private fun startSessionWatcher() {
        sessionJob?.cancel()
        sessionJob = scope.launch {
            while (isActive && _currentUser.value != null) {
                // 每 5 秒轮询一次云端 Token 状态
                // 即使不主动操作，如果 Token 被抢占，repository 会通过 onUnauthorized 踢出
                val user = _currentUser.value ?: break
                println("UserManager: Checking session status for ${user.username}")
                gameRepository.getUser(user.username)
                delay(5000)
            }
        }
    }

    private fun saveUserToLocal(user: User) {
        try {
            val userJson = json.encodeToString(user)
            settings.putString(KEY_USER_DATA, userJson)
        } catch (e: Exception) {
            println("UserManager: Failed to save user: ${e.message}")
        }
    }

    fun getSavedUsername(): String? {
        return _currentUser.value?.username
    }

    fun initUser(user: User) {
        val current = _currentUser.value
        if (current == null || user.score >= current.score) {
            _currentUser.value = user
            saveUserToLocal(user)
            gameRepository.setAuthToken(user.token)
            startSessionWatcher()
        }
    }

    companion object {
        private const val KEY_USER_DATA = "user_data_v2"
    }
}

sealed interface AuthEvent {
    data class LoggedOut(val reason: String) : AuthEvent
}
