package com.finley.android.shared.ui.profile.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.shared.Res
import com.finley.android.shared.common_back
import com.finley.android.shared.common_cancel
import com.finley.android.shared.common_confirm
import com.finley.android.shared.common_score_unit
import com.finley.android.shared.domain.getLocalizedLevelLabel
import com.finley.android.shared.profile_action_change_password
import com.finley.android.shared.profile_action_logout
import com.finley.android.shared.profile_level_display
import com.finley.android.shared.profile_new_password
import com.finley.android.shared.profile_nickname_display
import com.finley.android.shared.profile_nickname_label
import com.finley.android.shared.profile_not_set
import com.finley.android.shared.profile_old_password
import com.finley.android.shared.profile_score_display
import com.finley.android.shared.profile_title
import com.finley.android.shared.profile_username_display
import com.finley.android.shared.theme.BrandGradient
import com.finley.android.shared.theme.FireGradient
import com.finley.android.shared.theme.Gold
import com.finley.android.shared.theme.InkMuted
import com.finley.android.shared.theme.InkWhite
import com.finley.android.shared.theme.Midnight
import com.finley.android.shared.theme.Rose
import com.finley.android.shared.ui.components.GlassCard
import com.finley.android.shared.ui.components.GlassTopBar
import com.finley.android.shared.ui.components.GradientButton
import com.finley.android.shared.ui.profile.logic.ProfileViewModel
import com.finley.android.shared.ui.profile.mvi.ProfileEffect
import com.finley.android.shared.ui.profile.mvi.ProfileIntent
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel, onNavigateBack: () -> Unit, onNavigateToAuth: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ProfileEffect.NavigateToAuth -> onNavigateToAuth()
                is ProfileEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                GlassTopBar(
                    title = stringResource(Res.string.profile_title),
                    navigationContent = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.common_back),
                                tint = InkWhite
                            )
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
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileHero(username = state.username, nickname = state.nickname)

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        label = stringResource(Res.string.profile_level_display),
                        value = getLocalizedLevelLabel(state.currentLevel),
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        gradient = BrandGradient,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = stringResource(Res.string.profile_score_display),
                        value = "${state.score}${stringResource(Res.string.common_score_unit)}",
                        icon = Icons.Default.Star,
                        gradient = listOf(Color(0xFFFFC24D), Gold),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (state.isEditing) {
                    GlassCard(
                        shape = RoundedCornerShape(18.dp),
                        fillAlpha = 0.06f,
                        borderAlpha = 0.14f
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(Res.string.profile_nickname_label),
                                fontSize = 13.sp,
                                color = InkMuted
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = state.newNickname,
                                onValueChange = { viewModel.handleIntent(ProfileIntent.NicknameChanged(it)) },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                }),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = InkWhite,
                                    unfocusedTextColor = InkWhite,
                                    focusedBorderColor = Rose,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                    cursorColor = Rose
                                ),
                                shape = RoundedCornerShape(14.dp),
                                trailingIcon = {
                                    Row {
                                        IconButton(onClick = { viewModel.handleIntent(ProfileIntent.SaveNickname) }) {
                                            Icon(Icons.Default.Check, contentDescription = stringResource(Res.string.common_confirm), tint = Color(0xFF34D399))
                                        }
                                        IconButton(onClick = { viewModel.handleIntent(ProfileIntent.CancelEditing) }) {
                                            Icon(Icons.Default.Close, contentDescription = stringResource(Res.string.common_cancel), tint = Color(0xFFFF5252))
                                        }
                                    }
                                }
                            )
                        }
                    }
                } else {
                    InfoCard(
                        label = stringResource(Res.string.profile_nickname_display),
                        value = if (state.nickname == "未设置") stringResource(Res.string.profile_not_set) else state.nickname,
                        icon = Icons.Default.Edit,
                        trailing = {
                            IconButton(onClick = { viewModel.handleIntent(ProfileIntent.StartEditing) }) {
                                Icon(Icons.Default.Edit, contentDescription = "修改", tint = InkMuted)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                InfoCard(
                    label = stringResource(Res.string.profile_username_display),
                    value = state.username,
                    icon = Icons.Default.Star,
                    trailing = null
                )

                Spacer(modifier = Modifier.height(24.dp))

                GlassButton(
                    text = stringResource(Res.string.profile_action_change_password),
                    icon = Icons.Default.Lock,
                    onClick = { viewModel.handleIntent(ProfileIntent.OpenPasswordDialog) }
                )

                Spacer(modifier = Modifier.height(14.dp))

                GradientButton(
                    text = stringResource(Res.string.profile_action_logout),
                    onClick = { viewModel.handleIntent(ProfileIntent.Logout) },
                    gradient = FireGradient
                )

                Spacer(modifier = Modifier.height(16.dp))
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
fun ProfileHero(username: String, nickname: String?) {
    GlassCard(
        shape = RoundedCornerShape(26.dp),
        fillAlpha = 0.08f,
        borderAlpha = 0.14f
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .border(2.dp, Gold.copy(alpha = 0.4f), CircleShape)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(BrandGradient)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = username.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = username,
                color = InkWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (nickname == "未设置") stringResource(Res.string.profile_not_set) else nickname ?: stringResource(Res.string.profile_not_set),
                color = InkMuted,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        fillAlpha = 0.06f,
        borderAlpha = 0.10f
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.linearGradient(gradient)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = InkMuted
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                color = InkWhite,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
        }
    }
}

@Composable
fun InfoCard(
    label: String,
    value: String,
    icon: ImageVector,
    trailing: (@Composable () -> Unit)?
) {
    GlassCard(
        shape = RoundedCornerShape(18.dp),
        fillAlpha = 0.06f,
        borderAlpha = 0.10f
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Gold, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, fontSize = 12.sp, color = InkMuted)
                Text(
                    text = value,
                    fontSize = 17.sp,
                    color = InkWhite,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
            trailing?.invoke()
        }
    }
}

@Composable
fun GlassButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = InkMuted, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, color = InkWhite, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ChangePasswordDialog(
    oldPassword: String,
    newPassword: String,
    onOldChanged: (String) -> Unit,
    onNewChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val newPasswordFocusRequester = remember { FocusRequester() }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Midnight,
        shape = RoundedCornerShape(26.dp),
        titleContentColor = Color.White,
        title = {
            Text(
                text = stringResource(Res.string.profile_action_change_password),
                fontWeight = FontWeight.Black
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = onOldChanged,
                    label = { Text(stringResource(Res.string.profile_old_password), color = InkMuted) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { newPasswordFocusRequester.requestFocus() }),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = InkWhite,
                        unfocusedTextColor = InkWhite,
                        focusedBorderColor = Rose,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = onNewChanged,
                    label = { Text(stringResource(Res.string.profile_new_password), color = InkMuted) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(newPasswordFocusRequester),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = InkWhite,
                        unfocusedTextColor = InkWhite,
                        focusedBorderColor = Rose,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Rose,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(stringResource(Res.string.common_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(Res.string.common_cancel),
                    color = InkMuted
                )
            }
        }
    )
}
