package com.finley.android.shared.ui.profile.mvi

data class ProfileState(
    val username: String = "",
    val nickname: String = "",
    val currentLevel: String = "",
    val score: Int = 0,
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val newNickname: String = "",
    val oldPassword: String = "",
    val newPassword: String = "",
    val showPasswordDialog: Boolean = false
)

sealed interface ProfileIntent {
    object LoadProfile : ProfileIntent
    object StartEditing : ProfileIntent
    data class NicknameChanged(val value: String) : ProfileIntent
    object SaveNickname : ProfileIntent
    object CancelEditing : ProfileIntent
    object OpenPasswordDialog : ProfileIntent
    object DismissPasswordDialog : ProfileIntent
    data class OldPasswordChanged(val value: String) : ProfileIntent
    data class NewPasswordChanged(val value: String) : ProfileIntent
    object ChangePassword : ProfileIntent
    object Logout : ProfileIntent
}

sealed interface ProfileEffect {
    object NavigateToAuth : ProfileEffect
    data class ShowMessage(val message: String) : ProfileEffect
}
