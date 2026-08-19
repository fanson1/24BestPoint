package com.finley.android.shared.ui.auth.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.shared.Res
import com.finley.android.shared.auth_create_account
import com.finley.android.shared.auth_login_button
import com.finley.android.shared.auth_password_label
import com.finley.android.shared.auth_register_button
import com.finley.android.shared.auth_title
import com.finley.android.shared.auth_toggle_login
import com.finley.android.shared.auth_toggle_register
import com.finley.android.shared.auth_username_label
import com.finley.android.shared.auth_welcome_back
import com.finley.android.shared.ic_app_logo
import com.finley.android.shared.theme.Gold
import com.finley.android.shared.theme.InkMuted
import com.finley.android.shared.theme.InkWhite
import com.finley.android.shared.theme.Rose
import com.finley.android.shared.theme.Violet
import com.finley.android.shared.ui.auth.logic.AuthViewModel
import com.finley.android.shared.ui.auth.mvi.AuthEffect
import com.finley.android.shared.ui.auth.mvi.AuthIntent
import com.finley.android.shared.ui.components.GlassCard
import com.finley.android.shared.ui.components.GradientButton
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AuthScreen(viewModel: AuthViewModel, onNavigateToGame: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Logo 呼吸动画
    val infiniteTransition = rememberInfiniteTransition(label = "logo_anim")
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AuthEffect.NavigateToGame -> onNavigateToGame()
                is AuthEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 优雅的 Logo 展示：外圈光晕 + 呼吸缩放
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .scale(logoScale),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .shadow(40.dp, CircleShape, spotColor = Violet.copy(alpha = 0.35f))
                            .background(Gold.copy(alpha = 0.08f), CircleShape)
                            .border(2.dp, Gold.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.ic_app_logo),
                            contentDescription = "Game Logo",
                            modifier = Modifier.size(112.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                Text(
                    text = stringResource(Res.string.auth_title),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Light,
                    color = InkWhite,
                    letterSpacing = 8.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 仅在非预检状态下显示表单，带有优雅的淡入效果
                AnimatedVisibility(
                    visible = !state.isCheckingSession,
                    enter = fadeIn(animationSpec = tween(900)),
                    exit = fadeOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 34.dp)
                    ) {
                        Text(
                            text = if (state.isLoginMode) stringResource(Res.string.auth_welcome_back) else stringResource(Res.string.auth_create_account),
                            fontSize = 16.sp,
                            color = InkMuted,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )

                        GlassCard(
                            shape = RoundedCornerShape(28.dp),
                            fillAlpha = 0.08f,
                            borderAlpha = 0.14f,
                            modifier = Modifier.fillMaxWidth(0.86f)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                OutlinedTextField(
                                    value = state.username,
                                    onValueChange = { viewModel.handleIntent(AuthIntent.UsernameChanged(it)) },
                                    label = { Text(stringResource(Res.string.auth_username_label), color = InkMuted) },
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = InkWhite,
                                        unfocusedTextColor = InkWhite,
                                        focusedBorderColor = Rose,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                        cursorColor = Rose
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = state.password,
                                    onValueChange = { viewModel.handleIntent(AuthIntent.PasswordChanged(it)) },
                                    label = { Text(stringResource(Res.string.auth_password_label), color = InkMuted) },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(onDone = {
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                    }),
                                    singleLine = true,
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                                contentDescription = null,
                                                tint = InkMuted
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = InkWhite,
                                        unfocusedTextColor = InkWhite,
                                        focusedBorderColor = Rose,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                        cursorColor = Rose
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )

                                Spacer(modifier = Modifier.height(28.dp))

                                GradientButton(
                                    text = if (state.isLoginMode) stringResource(Res.string.auth_login_button) else stringResource(Res.string.auth_register_button),
                                    onClick = { viewModel.handleIntent(AuthIntent.Submit) },
                                    enabled = !state.isLoading,
                                    loading = state.isLoading
                                )
                            }
                        }

                        TextButton(
                            onClick = { viewModel.handleIntent(AuthIntent.ToggleMode) },
                            modifier = Modifier.padding(top = 18.dp)
                        ) {
                            Text(
                                text = if (state.isLoginMode) stringResource(Res.string.auth_toggle_register) else stringResource(Res.string.auth_toggle_login),
                                color = InkWhite,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
