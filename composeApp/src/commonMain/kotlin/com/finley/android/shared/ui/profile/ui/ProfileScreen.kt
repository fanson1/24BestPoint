package com.finley.android.shared.ui.profile.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.shared.ui.profile.logic.ProfileViewModel
import com.finley.android.shared.ui.profile.mvi.ProfileEffect
import com.finley.android.shared.ui.profile.mvi.ProfileIntent
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import com.finley.android.shared.Res
import com.finley.android.shared.*
import com.finley.android.shared.domain.getLocalizedLevelLabel

private val GameBackgroundStart = Color(0xFF1A1A2E)
private val GameBackgroundEnd = Color(0xFF162447)
private val CardAccent = Color(0xFFE94560)
private val Gold = Color(0xFFFFD700)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel, onNavigateBack: () -> Unit, onNavigateToAuth: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ProfileEffect.NavigateToAuth -> onNavigateToAuth()
                is ProfileEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    ),
                    title = { Text(stringResource(Res.string.profile_title), fontWeight = FontWeight.Black) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.common_back), tint = Color.White)
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(modifier = Modifier.size(100.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.1f)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Gold, modifier = Modifier.size(64.dp))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                ProfileInfoCard(label = stringResource(Res.string.profile_username_display), value = state.username, icon = Icons.Default.AccountCircle)
                Spacer(modifier = Modifier.height(16.dp))
                if (state.isEditing) {
                    OutlinedTextField(
                        value = state.newNickname,
                        onValueChange = { viewModel.handleIntent(ProfileIntent.NicknameChanged(it)) },
                        label = { Text(stringResource(Res.string.profile_nickname_label), color = Color.White.copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = CardAccent, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                        trailingIcon = {
                            Row {
                                IconButton(onClick = { viewModel.handleIntent(ProfileIntent.SaveNickname) }) { Icon(Icons.Default.Check, contentDescription = stringResource(Res.string.common_confirm), tint = Color.Green) }
                                IconButton(onClick = { viewModel.handleIntent(ProfileIntent.CancelEditing) }) { Icon(Icons.Default.Close, contentDescription = stringResource(Res.string.common_cancel), tint = Color.Red) }
                            }
                        }
                    )
                } else {
                    ProfileInfoCard(label = stringResource(Res.string.profile_nickname_display), value = if (state.nickname == "未设置") stringResource(Res.string.profile_not_set) else state.nickname, icon = Icons.Default.Face, trailingIcon = {
                        IconButton(onClick = { viewModel.handleIntent(ProfileIntent.StartEditing) }) { Icon(Icons.Default.Edit, contentDescription = "修改", tint = Color.White.copy(alpha = 0.5f)) }
                    })
                }
                Spacer(modifier = Modifier.height(16.dp))
                ProfileInfoCard(label = stringResource(Res.string.profile_level_display), value = getLocalizedLevelLabel(state.currentLevel), icon = Icons.AutoMirrored.Filled.TrendingUp)
                Spacer(modifier = Modifier.height(16.dp))
                ProfileInfoCard(label = stringResource(Res.string.profile_score_display), value = "${state.score}${stringResource(Res.string.common_score_unit)}", icon = Icons.Default.Star)
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { viewModel.handleIntent(ProfileIntent.OpenPasswordDialog) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.1f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(Res.string.profile_action_change_password))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.handleIntent(ProfileIntent.Logout) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CardAccent,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(stringResource(Res.string.profile_action_logout), fontWeight = FontWeight.Bold)
                }
            }
        }

        if (state.showPasswordDialog) {
            ChangePasswordDialog(
                oldPassword = state.oldPassword,
                newPassword = state.newPassword,
                onOldChanged = { viewModel.handleIntent(ProfileIntent.OldPasswordChanged(it)) },
                onNewChanged = { viewModel.handleIntent(ProfileIntent.NewPasswordChanged(it)) },
                onDismiss = { viewModel.handleIntent(ProfileIntent.DismissPasswordDialog) },
                onConfirm = { viewModel.handleIntent(ProfileIntent.ChangePassword) }
            )
        }
    }
}

@Composable
fun ProfileInfoCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, trailingIcon: @Composable (() -> Unit)? = null) {
    Surface(color = Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Gold, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                Text(text = value, fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Medium)
            }
            trailingIcon?.invoke()
        }
    }
}

@Composable
fun ChangePasswordDialog(oldPassword: String, newPassword: String, onOldChanged: (String) -> Unit, onNewChanged: (String) -> Unit, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1F4068),
        titleContentColor = Color.White,
        title = { Text(stringResource(Res.string.profile_action_change_password), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(value = oldPassword, onValueChange = onOldChanged, label = { Text(stringResource(Res.string.profile_old_password), color = Color.White.copy(alpha = 0.5f)) }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = CardAccent))
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = newPassword, onValueChange = onNewChanged, label = { Text(stringResource(Res.string.profile_new_password), color = Color.White.copy(alpha = 0.5f)) }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = CardAccent))
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CardAccent,
                    contentColor = Color.White
                )
            ) { Text(stringResource(Res.string.common_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(Res.string.common_cancel),
                    color = Color.White
                )
            }
        }
    )
}
