package com.finley.android.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.finley.android.shared.ui.auth.ui.AuthScreen
import com.finley.android.shared.ui.game.ui.GameScreen
import com.finley.android.shared.ui.game.ui.AdScreen
import com.finley.android.shared.ui.leaderboard.ui.LeaderboardScreen
import com.finley.android.shared.ui.profile.ui.ProfileScreen
import com.finley.android.shared.theme.getTypography
import com.finley.android.shared.*
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.compose.koinInject
import com.finley.android.shared.logic.UserManager
import org.jetbrains.compose.resources.stringResource

private val GameBackgroundStart = Color(0xFF1A1A2E)
private val GameBackgroundEnd = Color(0xFF162447)

@OptIn(KoinExperimentalAPI::class)
@Composable
fun App() {
    KoinContext {
        val userManager: UserManager = koinInject()
        
        MaterialTheme(
            colorScheme = darkColorScheme(),
            typography = getTypography()
        ) {
            var showSessionDialog by remember { mutableStateOf(false) }
            
            // 在最外层应用渐变背景，确保覆盖全屏
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(GameBackgroundStart, GameBackgroundEnd)))
            ) {
                val navController = rememberNavController()
                
                // 全局监听 SSO 强制登出事件
                LaunchedEffect(userManager) {
                    userManager.authEvents.collect {
                        showSessionDialog = true
                    }
                }
                
                if (showSessionDialog) {
                    AlertDialog(
                        onDismissRequest = { /* 不允许点击外部消失 */ },
                        title = { Text(stringResource(Res.string.auth_session_expired_title), fontWeight = FontWeight.Bold) },
                        text = { Text(stringResource(Res.string.auth_session_expired_desc)) },
                        confirmButton = {
                            Button(onClick = {
                                showSessionDialog = false
                                navController.navigate("auth") {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }) {
                                Text(stringResource(Res.string.common_confirm))
                            }
                        }
                    )
                }
                
                NavHost(
                    navController = navController,
                    startDestination = "auth",
                    modifier = Modifier.fillMaxSize()
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
                        val gameViewModel: com.finley.android.shared.ui.game.logic.GameViewModel = koinViewModel(viewModelStoreOwner = gameEntry)
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
                        val gameViewModel: com.finley.android.shared.ui.game.logic.GameViewModel = koinViewModel(viewModelStoreOwner = gameEntry)
                        
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
