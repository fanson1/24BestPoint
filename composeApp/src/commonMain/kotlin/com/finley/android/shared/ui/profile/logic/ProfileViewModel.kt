package com.finley.android.shared.ui.profile.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finley.android.shared.data.repository.GameRepository
import com.finley.android.shared.logic.SecurityUtils
import com.finley.android.shared.logic.UserManager
import com.finley.android.shared.ui.profile.mvi.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.finley.android.shared.*
import org.jetbrains.compose.resources.getString

class ProfileViewModel(
    private val gameRepository: GameRepository,
    private val userManager: UserManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _effect = Channel<ProfileEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        observeUser()
    }

    private fun observeUser() {
        // 使用 collect 持续观察本地 userManager 的变化
        // 当 GameViewModel 更新了本地 UserManager 时，ProfileViewModel 会自动更新 UI
        viewModelScope.launch {
            userManager.currentUser.collect { user ->
                if (user != null) {
                    val localizedNotSet = getString(Res.string.profile_not_set)
                    _state.update {
                        it.copy(
                            username = user.username,
                            nickname = user.nickname ?: localizedNotSet,
                            currentLevel = user.currentLevel,
                            score = user.score
                        )
                    }
                } else {
                    _state.update { ProfileState() }
                }
            }
        }
    }

    fun handleIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.LoadProfile -> { /* 已由 observeUser 处理 */ }
            ProfileIntent.StartEditing -> _state.update { it.copy(isEditing = true, newNickname = it.nickname) }
            is ProfileIntent.NicknameChanged -> _state.update { it.copy(newNickname = intent.value) }
            ProfileIntent.SaveNickname -> saveNickname()
            ProfileIntent.CancelEditing -> _state.update { it.copy(isEditing = false) }
            ProfileIntent.OpenPasswordDialog -> _state.update { it.copy(showPasswordDialog = true, oldPassword = "", newPassword = "") }
            ProfileIntent.DismissPasswordDialog -> _state.update { it.copy(showPasswordDialog = false) }
            is ProfileIntent.OldPasswordChanged -> _state.update { it.copy(oldPassword = intent.value) }
            is ProfileIntent.NewPasswordChanged -> _state.update { it.copy(newPassword = intent.value) }
            ProfileIntent.ChangePassword -> changePassword()
            ProfileIntent.Logout -> logout()
        }
    }

    private fun saveNickname() {
        val user = userManager.currentUser.value ?: return
        val newNick = _state.value.newNickname
        if (newNick.isBlank()) {
            viewModelScope.launch { 
                _effect.send(ProfileEffect.ShowMessage(getString(Res.string.profile_error_empty_nickname))) 
            }
            return
        }

        viewModelScope.launch {
            val updatedUser = user.copy(nickname = newNick)
            // 1. 更新本地内存 (这会让 UI 立即变化)
            userManager.updateCurrentUser(updatedUser)
            // 2. 更新云端 (确保多端同步)
            gameRepository.updateUser(updatedUser)
            _state.update { it.copy(nickname = newNick, isEditing = false) }
            _effect.send(ProfileEffect.ShowMessage(getString(Res.string.profile_success_nickname_updated)))
        }
    }

    private fun changePassword() {
        val user = userManager.currentUser.value ?: return
        val state = _state.value
        if (state.oldPassword.isBlank() || state.newPassword.isBlank()) {
            viewModelScope.launch { 
                _effect.send(ProfileEffect.ShowMessage(getString(Res.string.auth_error_empty_fields))) 
            }
            return
        }
        if (user.passwordHash != SecurityUtils.sha256(state.oldPassword)) {
            viewModelScope.launch { 
                _effect.send(ProfileEffect.ShowMessage(getString(Res.string.profile_error_wrong_old_password))) 
            }
            return
        }
        viewModelScope.launch {
            val updatedUser = user.copy(passwordHash = SecurityUtils.sha256(state.newPassword))
            userManager.updateCurrentUser(updatedUser)
            gameRepository.updateUser(updatedUser)
            _state.update { it.copy(showPasswordDialog = false) }
            _effect.send(ProfileEffect.ShowMessage(getString(Res.string.profile_success_password_changed)))
        }
    }

    private fun logout() {
        userManager.logout()
        viewModelScope.launch {
            _effect.send(ProfileEffect.NavigateToAuth)
        }
    }
}
