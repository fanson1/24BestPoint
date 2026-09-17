package com.finley.android.shared.ui.game.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.shared.Res
import com.finley.android.shared.app_name
import com.finley.android.shared.game_fever_game_over
import com.finley.android.shared.game_fever_mode_button
import com.finley.android.shared.game_nav_leaderboard
import com.finley.android.shared.game_nav_profile
import com.finley.android.shared.game_nav_skip
import com.finley.android.shared.theme.Gold
import com.finley.android.shared.theme.InkMuted
import com.finley.android.shared.theme.InkWhite
import com.finley.android.shared.ui.components.GlassTopBar
import com.finley.android.shared.ui.game.logic.GameViewModel
import com.finley.android.shared.ui.game.mvi.GameEffect
import com.finley.android.shared.ui.game.mvi.GameIntent
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToAd: () -> Unit,
    onNavigateToAuth: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is GameEffect.ShowError -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }
                }
                GameEffect.ShowSuccessAnim -> haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                is GameEffect.LevelUp -> haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                GameEffect.FeverGameOver -> scope.launch {
                    snackbarHostState.showSnackbar(getString(Res.string.game_fever_game_over))
                }
                GameEffect.NavigateToLeaderboard -> onNavigateToLeaderboard()
                GameEffect.NavigateToAd -> {
                    viewModel.handleIntent(GameIntent.DismissAdDialog)
                    onNavigateToAd()
                }
                GameEffect.NavigateToAuth -> onNavigateToAuth()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                GlassTopBar(
                    title = stringResource(Res.string.app_name),
                    navigationContent = {
                        IconButton(onClick = onNavigateToLeaderboard) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = stringResource(Res.string.game_nav_leaderboard),
                                tint = Gold,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.handleIntent(GameIntent.ToggleFeverMode) }) {
                            Icon(
                                imageVector = if (state.isFeverMode) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = stringResource(Res.string.game_fever_mode_button),
                                tint = if (state.isFeverMode) Color(0xFFFFD740) else InkMuted,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        TextButton(onClick = { viewModel.handleIntent(GameIntent.LoadNewPuzzle) }) {
                            Text(
                                text = stringResource(Res.string.game_nav_skip),
                                color = InkWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        TextButton(onClick = onNavigateToProfile) {
                            Text(
                                text = stringResource(Res.string.game_nav_profile),
                                color = InkWhite,
                                fontSize = 14.sp
                            )
                        }
                    }
                )
            }
        ) { padding ->
            if (state.showAdDialog) {
                AdRequestDialog(
                    hintCredits = state.hintCredits,
                    onWatchAd = { viewModel.handleIntent(GameIntent.WatchAd) },
                    onUseHint = { viewModel.handleIntent(GameIntent.UseHint) },
                    onDismiss = { viewModel.handleIntent(GameIntent.DismissAdDialog) }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(modifier = Modifier.height(2.dp))
                StatsHeader(
                    username = if (state.nickname?.isNotEmpty() == true) "${state.username} (${state.nickname})" else state.username,
                    score = state.score
                )

                if (state.isFeverMode) {
                    FeverTimer(state.timeLeft, state.comboCount)
                }

                DifficultyBoard(
                    difficultyLabel = stringResource(state.difficulty.labelRes),
                    progress = state.correctCountInLevel
                )

                if (state.isLevelLockActive) {
                    LevelLockScreen(
                        watched = state.lockAdsWatched,
                        onWatchAd = { viewModel.handleIntent(GameIntent.WatchLockAd) }
                    )
                } else {
                    state.hintContent?.let { HintView(it) }

                    ExpressionDisplay(state.currentExpression)

                    key(state.numbers) {
                        NumberGrid(
                            numbers = state.numbers,
                            selectedIndices = state.selectedIndices,
                            onNumberClick = { index, value ->
                                viewModel.handleIntent(GameIntent.SelectNumber(index, value))
                            }
                        )
                    }

                    OperatorRow(onOperatorClick = { op ->
                        viewModel.handleIntent(GameIntent.SelectOperator(op))
                    })

                    ActionButtons(
                        hintCredits = state.hintCredits,
                        onHint = { viewModel.handleIntent(GameIntent.RequestHint) },
                        onUndo = { viewModel.handleIntent(GameIntent.Undo) },
                        onReset = { viewModel.handleIntent(GameIntent.Reset) },
                        onSubmit = { viewModel.handleIntent(GameIntent.Submit) }
                    )
                }
            }
        }

        // 答案反馈层
        AnswerFeedbackOverlay(state.answerFeedback)

        // 升级庆典层
        state.showLevelUp?.let { LevelUpOverlay(it) }
    }
}