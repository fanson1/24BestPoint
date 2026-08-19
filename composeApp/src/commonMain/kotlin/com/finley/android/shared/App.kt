package com.finley.android.shared

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.finley.android.shared.logic.UserManager
import com.finley.android.shared.theme.AppTheme
import com.finley.android.shared.theme.Midnight
import com.finley.android.shared.ui.auth.ui.AuthScreen
import com.finley.android.shared.ui.components.GameBackground
import com.finley.android.shared.ui.game.logic.GameViewModel
import com.finley.android.shared.ui.game.ui.AdScreen
import com.finley.android.shared.ui.game.ui.GameScreen
import com.finley.android.shared.ui.leaderboard.ui.LeaderboardScreen
import com.finley.android.shared.ui.profile.ui.ProfileScreen
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun App() {
    KoinContext {
        AppTheme {
            val userManager: UserManager = koinInject()
            var showSessionDialog by remember { mutableStateOf(false) }
            val navController = rememberNavController()

            // 全局监听 SSO 强制登出事件
            LaunchedEffect(userManager) {
                userManager.authEvents.collect {
                    showSessionDialog = true
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                GameBackground()

                if (showSessionDialog) {
                    AlertDialog(
                        onDismissRequest = { },
                        containerColor = Midnight,
                        shape = RoundedCornerShape(28.dp),
                        titleContentColor = Color.White,
                        textContentColor = Color.White.copy(alpha = 0.8f),
                        title = {
                            Text(
                                stringResource(Res.string.auth_session_expired_title),
                                fontWeight = FontWeight.Black
                            )
                        },
                        text = {
                            Text(stringResource(Res.string.auth_session_expired_desc))
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showSessionDialog = false
                                    navController.navigate("auth") {
                                        popUpTo(0) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(stringResource(Res.string.common_confirm))
                            }
                        }
                    )
                }

                NavHost(
                    navController = navController,
                    startDestination = "auth",
                    modifier = Modifier.fillMaxSize(),
                    enterTransition = {
                        fadeIn(tween(320)) + slideInVertically(initialOffsetY = { it / 16 })
                    },
                    exitTransition = {
                        fadeOut(tween(260)) + slideOutVertically(targetOffsetY = { -it / 16 })
                    },
                    popEnterTransition = { fadeIn(tween(320)) },
                    popExitTransition = { fadeOut(tween(260)) }
                ) {
                    composable("auth") {
                        AuthScreen(
                            viewModel = koinViewModel(),
                            onNavigateToGame = {
                                navController.navigate("game") {
                                    popUpTo("auth") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("game") {
                        val gameEntry = remember(it) { it }
                        val gameViewModel: GameViewModel = koinViewModel(viewModelStoreOwner = gameEntry)
                        GameScreen(
                            viewModel = gameViewModel,
                            onNavigateToLeaderboard = { navController.navigate("leaderboard") },
                            onNavigateToAd = { navController.navigate("ad") },
                            onNavigateToAuth = {
                                navController.navigate("auth") {
                                    popUpTo("game") { inclusive = true }
                                }
                            },
                            onNavigateToProfile = { navController.navigate("profile") }
                        )
                    }
                    composable("ad") {
                        val gameEntry = remember(it) { navController.getBackStackEntry("game") }
                        val gameViewModel: GameViewModel = koinViewModel(viewModelStoreOwner = gameEntry)

                        AdScreen(
                            onAdFinished = {
                                gameViewModel.onAdCompleted()
                                navController.popBackStack()
                            },
                            onAdCancelled = { navController.popBackStack() }
                        )
                    }
                    composable("leaderboard") {
                        LeaderboardScreen(
                            viewModel = koinViewModel(),
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("profile") {
                        ProfileScreen(
                            viewModel = koinViewModel(),
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToAuth = {
                                navController.navigate("auth") {
                                    popUpTo("game") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
