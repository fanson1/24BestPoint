package com.finley.android.shared.ui.auth.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.shared.Res
import com.finley.android.shared.ic_app_logo
import com.finley.android.shared.ui.auth.logic.AuthViewModel
import com.finley.android.shared.ui.auth.mvi.AuthEffect
import com.finley.android.shared.ui.auth.mvi.AuthIntent
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.finley.android.shared.*

private val GameBackgroundStart = Color(0xFF1A1A2E)
private val GameBackgroundEnd = Color(0xFF162447)
private val CardAccent = Color(0xFFE94560)
private val Gold = Color(0xFFFFD700)

@Composable
fun AuthScreen(viewModel: AuthViewModel, onNavigateToGame: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var passwordVisible by remember { mutableStateOf(false) }

    // Logo 呼吸动画
    val infiniteTransition = rememberInfiniteTransition(label = "logo_anim")
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .windowInsetsPadding(WindowInsets.systemBars),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 优雅的 Logo 展示
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(logoScale)
                        .background(Gold.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.ic_app_logo),
                        contentDescription = "Game Logo",
                        modifier = Modifier.size(120.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(Res.string.auth_title),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White,
                    letterSpacing = 8.sp
                )

                // 仅在非预检状态下显示表单，带有优雅的淡入效果
                AnimatedVisibility(
                    visible = !state.isCheckingSession,
                    enter = fadeIn(animationSpec = tween(1000)),
                    exit = fadeOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 40.dp)
                    ) {
                        Text(
                            text = if (state.isLoginMode) stringResource(Res.string.auth_welcome_back) else stringResource(Res.string.auth_create_account),
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        Surface(
                            color = Color.White.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(28.dp),
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                OutlinedTextField(
                                    value = state.username,
                                    onValueChange = { viewModel.handleIntent(AuthIntent.UsernameChanged(it)) },
                                    label = { Text(stringResource(Res.string.auth_username_label), color = Color.White.copy(alpha = 0.4f)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = CardAccent,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                        cursorColor = CardAccent
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = state.password,
                                    onValueChange = { viewModel.handleIntent(AuthIntent.PasswordChanged(it)) },
                                    label = { Text(stringResource(Res.string.auth_password_label), color = Color.White.copy(alpha = 0.4f)) },
                                    visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                                contentDescription = null,
                                                tint = Color.White.copy(alpha = 0.4f)
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = CardAccent,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                        cursorColor = CardAccent
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )

                                Spacer(modifier = Modifier.height(32.dp))

                                Button(
                                    onClick = { viewModel.handleIntent(AuthIntent.Submit) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CardAccent,
                                        contentColor = Color.White
                                    ),
                                    enabled = !state.isLoading
                                ) {
                                    if (state.isLoading) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            if (state.isLoginMode) stringResource(Res.string.auth_login_button) else stringResource(Res.string.auth_register_button),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        TextButton(
                            onClick = { viewModel.handleIntent(AuthIntent.ToggleMode) },
                            modifier = Modifier.padding(top = 20.dp)
                        ) {
                            Text(
                                if (state.isLoginMode) stringResource(Res.string.auth_toggle_register) else stringResource(Res.string.auth_toggle_login),
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
