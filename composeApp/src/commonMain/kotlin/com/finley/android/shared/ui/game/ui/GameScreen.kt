package com.finley.android.shared.ui.game.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.shared.Res
import com.finley.android.shared.ad_timer_label
import com.finley.android.shared.app_name
import com.finley.android.shared.game_action_hint_count
import com.finley.android.shared.game_action_reset
import com.finley.android.shared.game_action_submit
import com.finley.android.shared.game_action_undo
import com.finley.android.shared.game_expression_placeholder
import com.finley.android.shared.game_fever_combo
import com.finley.android.shared.game_hint_ad_get
import com.finley.android.shared.game_hint_cancel
import com.finley.android.shared.game_hint_dialog_desc
import com.finley.android.shared.game_hint_dialog_title
import com.finley.android.shared.game_hint_use_now
import com.finley.android.shared.game_level_label
import com.finley.android.shared.game_lock_desc
import com.finley.android.shared.game_lock_title
import com.finley.android.shared.game_lock_unlock_button
import com.finley.android.shared.game_nav_profile
import com.finley.android.shared.game_nav_skip
import com.finley.android.shared.game_progress_label
import com.finley.android.shared.game_score_label
import com.finley.android.shared.game_level_up_message
import com.finley.android.shared.game_achievement_unlocked
import com.finley.android.shared.game_fever_game_over
import com.finley.android.shared.game_fever_excellent
import com.finley.android.shared.game_fever_try_again
import com.finley.android.shared.domain.getLocalizedLevelLabel
import com.finley.android.shared.domain.getLevelResource
import com.finley.android.shared.ui.game.logic.GameViewModel
import org.jetbrains.compose.resources.getString
import com.finley.android.shared.ui.game.mvi.AnswerFeedback
import com.finley.android.shared.ui.game.mvi.AnswerFeedbackType
import com.finley.android.shared.ui.game.mvi.GameEffect
import com.finley.android.shared.ui.game.mvi.GameIntent
import com.finley.android.shared.ui.game.mvi.GameState
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource

// 预定义颜色
private val GameBackgroundStart = Color(0xFF1A1A2E)
private val GameBackgroundEnd = Color(0xFF162447)
private val CardPrimary = Color(0xFF1F4068)
private val CardAccent = Color(0xFFE94560)
private val NumberText = Color(0xFFFFFFFF)
private val OperatorButtonColor = Color(0xFF533483)
private val Gold = Color(0xFFFFD700)
private val SuccessGreen = Color(0xFF4E9F3D)

@OptIn(ExperimentalMaterial3Api::class)
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
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is GameEffect.ShowError -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    snackbarHostState.showSnackbar(effect.message)
                }
                is GameEffect.ShowSuccessAnim -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                is GameEffect.LevelUp -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val levelRes = getLevelResource(effect.newDifficulty)
                    val levelLabel = if (levelRes != null) getString(levelRes) else effect.newDifficulty
                    snackbarHostState.showSnackbar(getString(Res.string.game_level_up_message, levelLabel))
                }
                is GameEffect.AchievementUnlocked -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    snackbarHostState.showSnackbar(getString(Res.string.game_achievement_unlocked, effect.title))
                }
                GameEffect.FeverGameOver -> {
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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    windowInsets = TopAppBarDefaults.windowInsets,
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    ),
                    title = {
                        Text(
                            stringResource(Res.string.app_name),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            letterSpacing = 1.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateToLeaderboard) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Gold, modifier = Modifier.size(24.dp))
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.handleIntent(GameIntent.ToggleFeverMode) }) {
                            Icon(
                                imageVector = if (state.isFeverMode) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Fever Mode",
                                tint = if (state.isFeverMode) Color.Yellow else Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        TextButton(onClick = { viewModel.handleIntent(GameIntent.LoadNewPuzzle) }) {
                            Text(
                                stringResource(Res.string.game_nav_skip),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        TextButton(onClick = onNavigateToProfile) {
                            Text(
                                stringResource(Res.string.game_nav_profile),
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                )
            }
        ) { padding ->
            if (state.showAdDialog) {
                AdRequestDialog(state, viewModel)
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

                DifficultyBoard(stringResource(state.difficulty.labelRes), state.correctCountInLevel)

                if (state.isLevelLockActive) {
                    LevelLockScreen(
                        watched = state.lockAdsWatched,
                        onWatchAd = { viewModel.handleIntent(GameIntent.WatchLockAd) }
                    )
                } else {
                    state.hintContent?.let { HintView(it) }

                    ExpressionDisplay(state.currentExpression)

                    NumberGrid(
                        numbers = state.numbers,
                        selectedIndices = state.selectedIndices,
                        onNumberClick = { index, value ->
                            viewModel.handleIntent(GameIntent.SelectNumber(index, value))
                        }
                    )

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
        if (state.showLevelUp != null) {
            LevelUpOverlay(state.showLevelUp!!)
        }
    }
}

@Composable
fun StatsHeader(username: String, score: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(username, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                stringResource(Res.string.game_score_label, score),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun FeverTimer(seconds: Int, combo: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Red.copy(alpha = 0.2f))
            .border(2.dp, Color.Red.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Timer, contentDescription = null, tint = Color.Red)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                stringResource(Res.string.ad_timer_label, seconds),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
        }
        if (combo > 0) {
            Text(
                stringResource(Res.string.game_fever_combo, combo),
                color = Color.Yellow,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun DifficultyBoard(difficultyLabel: String, progress: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = stringResource(Res.string.game_level_label, difficultyLabel),
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = CardAccent
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator(
                progress = { progress / 10f },
                modifier = Modifier.weight(1f).height(8.dp).clip(CircleShape),
                color = CardAccent,
                trackColor = Color.White.copy(alpha = 0.1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(Res.string.game_progress_label, progress),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun HintView(hint: String) {
    Text(
        text = hint,
        color = Gold,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Gold.copy(alpha = 0.1f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun ExpressionDisplay(expression: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .border(
                BorderStroke(
                    1.dp,
                    if (expression.isNotEmpty()) CardAccent.copy(alpha = 0.5f) else Color.White.copy(
                        alpha = 0.2f
                    )
                ), RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = expression.ifEmpty { stringResource(Res.string.game_expression_placeholder) },
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (expression.isEmpty()) Color.White.copy(alpha = 0.3f) else Color.White,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun NumberGrid(numbers: List<Int>, selectedIndices: Set<Int>, onNumberClick: (Int, Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val rows = numbers.chunked(2)
        rows.forEachIndexed { rowIndex, rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                rowItems.forEachIndexed { colIndex, num ->
                    val actualIndex = rowIndex * 2 + colIndex
                    val isSelected = selectedIndices.contains(actualIndex)
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val scale by animateFloatAsState(targetValue = if (isPressed) 0.92f else 1.0f)

                    Surface(
                        modifier = Modifier
                            .size(90.dp)
                            .scale(scale)
                            .shadow(
                                elevation = if (isSelected) 0.dp else 8.dp,
                                shape = RoundedCornerShape(16.dp),
                                ambientColor = CardAccent.copy(alpha = 0.4f),
                                spotColor = CardAccent
                            )
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                enabled = !isSelected
                            ) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onNumberClick(actualIndex, num)
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) Color.White.copy(alpha = 0.05f) else CardPrimary,
                        border = if (isSelected) null else BorderStroke(
                            2.dp,
                            Color.White.copy(alpha = 0.15f)
                        )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = num.toString(),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) Color.White.copy(alpha = 0.2f) else NumberText
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OperatorRow(onOperatorClick: (String) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val operators = listOf("+", "-", "*", "/", "(", ")")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        operators.forEach { op ->
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(if (isPressed) 0.85f else 1.0f)

            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onOperatorClick(op)
                },
                modifier = Modifier.size(54.dp).scale(scale),
                shape = CircleShape,
                color = OperatorButtonColor,
                shadowElevation = 6.dp,
                interactionSource = interactionSource
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = op,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ActionButtons(
    hintCredits: Int,
    onHint: () -> Unit,
    onUndo: () -> Unit,
    onReset: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionButton(
                text = stringResource(Res.string.game_action_undo),
                color = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.weight(1f),
                onClick = onUndo
            )
            ActionButton(
                text = stringResource(Res.string.game_action_reset),
                color = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.weight(1f),
                onClick = onReset
            )
            ActionButton(
                text = stringResource(Res.string.game_action_hint_count, hintCredits),
                color = OperatorButtonColor,
                modifier = Modifier.weight(1.2f),
                onClick = onHint
            )
        }
        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth().height(56.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CardAccent,
                contentColor = Color.White
            )
        ) {
            Text(
                stringResource(Res.string.game_action_submit),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun ActionButton(text: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = color,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(text = text, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AdRequestDialog(state: GameState, viewModel: GameViewModel) {
    AlertDialog(
        onDismissRequest = { viewModel.handleIntent(GameIntent.DismissAdDialog) },
        containerColor = CardPrimary,
        titleContentColor = Color.White,
        textContentColor = Color.White.copy(alpha = 0.8f),
        title = {
            Text(
                stringResource(Res.string.game_hint_dialog_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    stringResource(
                        Res.string.game_hint_dialog_desc,
                        state.hintCredits
                    )
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.hintCredits < 5) Button(
                    onClick = { viewModel.handleIntent(GameIntent.WatchAd) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CardAccent,
                        contentColor = Color.White
                    )
                ) { Text(stringResource(Res.string.game_hint_ad_get)) }
                if (state.hintCredits > 0) Button(
                    onClick = { viewModel.handleIntent(GameIntent.UseHint) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SuccessGreen,
                        contentColor = Color.White
                    )
                ) { Text(stringResource(Res.string.game_hint_use_now)) }
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.handleIntent(GameIntent.DismissAdDialog) }) {
                Text(
                    stringResource(Res.string.game_hint_cancel),
                    color = Color.White
                )
            }
        }
    )
}

@Composable
fun LevelLockScreen(watched: Int, onWatchAd: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(2.dp, CardAccent.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = CardAccent
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                stringResource(Res.string.game_lock_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                stringResource(Res.string.game_lock_desc),
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onWatchAd,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CardAccent,
                    contentColor = Color.White
                )
            ) {
                Text(
                    stringResource(Res.string.game_lock_unlock_button, watched),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AnswerFeedbackOverlay(feedback: AnswerFeedback?) {
    if (feedback != null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (feedback.type == AnswerFeedbackType.CORRECT) stringResource(Res.string.game_fever_excellent) else stringResource(Res.string.game_fever_try_again),
                color = if (feedback.type == AnswerFeedbackType.CORRECT) SuccessGreen else CardAccent,
                fontSize = 44.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun LevelUpOverlay(newLevelKey: String) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Text(stringResource(Res.string.game_level_up_message, getLocalizedLevelLabel(newLevelKey)), color = Gold, fontSize = 32.sp, fontWeight = FontWeight.Bold)
    }
}
