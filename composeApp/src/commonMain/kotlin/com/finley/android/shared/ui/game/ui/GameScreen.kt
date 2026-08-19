package com.finley.android.shared.ui.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.shared.Res
import com.finley.android.shared.ad_timer_label
import com.finley.android.shared.app_name
import com.finley.android.shared.common_score_unit
import com.finley.android.shared.domain.getLocalizedLevelLabel
import com.finley.android.shared.game_achievement_unlocked
import com.finley.android.shared.game_action_hint_count
import com.finley.android.shared.game_action_reset
import com.finley.android.shared.game_action_submit
import com.finley.android.shared.game_action_undo
import com.finley.android.shared.game_expression_placeholder
import com.finley.android.shared.game_fever_combo
import com.finley.android.shared.game_fever_excellent
import com.finley.android.shared.game_fever_game_over
import com.finley.android.shared.game_fever_try_again
import com.finley.android.shared.game_hint_ad_get
import com.finley.android.shared.game_hint_cancel
import com.finley.android.shared.game_hint_dialog_desc
import com.finley.android.shared.game_hint_dialog_title
import com.finley.android.shared.game_hint_use_now
import com.finley.android.shared.game_level_label
import com.finley.android.shared.game_level_up_message
import com.finley.android.shared.game_lock_desc
import com.finley.android.shared.game_lock_title
import com.finley.android.shared.game_lock_unlock_button
import com.finley.android.shared.game_nav_profile
import com.finley.android.shared.game_nav_skip
import com.finley.android.shared.game_progress_label
import com.finley.android.shared.theme.BrandGradient
import com.finley.android.shared.theme.Emerald
import com.finley.android.shared.theme.FireGradient
import com.finley.android.shared.theme.Gold
import com.finley.android.shared.theme.GoldGradient
import com.finley.android.shared.theme.InkFaint
import com.finley.android.shared.theme.InkMuted
import com.finley.android.shared.theme.InkWhite
import com.finley.android.shared.theme.Midnight
import com.finley.android.shared.theme.Rose
import com.finley.android.shared.theme.Violet
import com.finley.android.shared.ui.components.AnimatedCounter
import com.finley.android.shared.ui.components.GlassCard
import com.finley.android.shared.ui.components.GlassTopBar
import com.finley.android.shared.ui.components.GradientButton
import com.finley.android.shared.ui.game.logic.GameViewModel
import com.finley.android.shared.ui.game.mvi.AnswerFeedback
import com.finley.android.shared.ui.game.mvi.AnswerFeedbackType
import com.finley.android.shared.ui.game.mvi.GameEffect
import com.finley.android.shared.ui.game.mvi.GameIntent
import com.finley.android.shared.ui.game.mvi.GameState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

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
                                contentDescription = null,
                                tint = Gold,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.handleIntent(GameIntent.ToggleFeverMode) }) {
                            Icon(
                                imageVector = if (state.isFeverMode) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Fever Mode",
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
        if (state.showLevelUp != null) {
            LevelUpOverlay(state.showLevelUp!!)
        }
    }
}

@Composable
fun StatsHeader(username: String, score: Int) {
    GlassCard(
        shape = RoundedCornerShape(18.dp),
        fillAlpha = 0.08f,
        borderAlpha = 0.12f
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(BrandGradient)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = username.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = username,
                    color = InkWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 140.dp)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                AnimatedCounter(
                    value = score,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = InkWhite,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Text(
                    text = stringResource(Res.string.common_score_unit),
                    color = InkMuted,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun FeverTimer(seconds: Int, combo: Int) {
    val infinite = rememberInfiniteTransition(label = "fever")
    val pulse by infinite.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.horizontalGradient(FireGradient.map { it.copy(alpha = 0.18f) }))
                .border(1.5.dp, Rose.copy(alpha = pulse), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = Rose,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = stringResource(Res.string.ad_timer_label, seconds),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    LinearProgressIndicator(
                        progress = { seconds / 30f },
                        modifier = Modifier.width(110.dp).height(4.dp).clip(CircleShape),
                        color = Rose,
                        trackColor = Color.White.copy(alpha = 0.12f)
                    )
                }
            }
            if (combo > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Gold.copy(alpha = 0.18f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(Res.string.game_fever_combo, combo),
                        color = Gold,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DifficultyBoard(difficultyLabel: String, progress: Int) {
    GlassCard(
        shape = RoundedCornerShape(16.dp),
        fillAlpha = 0.05f,
        borderAlpha = 0.10f
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(BrandGradient)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MilitaryTech,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(Res.string.game_level_label, difficultyLabel),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = InkWhite
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(Res.string.game_progress_label, progress),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkMuted
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress / 10f },
                modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),
                color = Rose,
                trackColor = Color.White.copy(alpha = 0.08f)
            )
        }
    }
}

@Composable
fun HintView(hint: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Gold.copy(alpha = 0.10f))
            .border(1.dp, Gold.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Lightbulb,
            contentDescription = null,
            tint = Gold,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = hint,
            color = Gold,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ExpressionDisplay(expression: String) {
    val hasContent = expression.isNotEmpty()
    GlassCard(
        shape = RoundedCornerShape(16.dp),
        fillAlpha = if (hasContent) 0.08f else 0.05f,
        borderAlpha = if (hasContent) 0.35f else 0.10f
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = expression.ifEmpty { stringResource(Res.string.game_expression_placeholder) },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (expression.isEmpty()) InkFaint else InkWhite,
                letterSpacing = 1.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Gold.copy(alpha = 0.14f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "= 24",
                    color = Gold,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }
        }
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
                    NumberCard(
                        index = actualIndex,
                        value = num,
                        isSelected = isSelected,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNumberClick(actualIndex, num)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NumberCard(index: Int, value: Int, isSelected: Boolean, onClick: () -> Unit) {
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(index * 70L)
        appear.animateTo(1f, tween(340, easing = FastOutSlowInEasing))
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        label = "number_press"
    )

    val displayScale = (0.6f + 0.4f * appear.value) * pressScale

    Box(
        modifier = Modifier
            .size(92.dp)
            .scale(displayScale)
            .alpha(appear.value)
            .shadow(
                elevation = if (isSelected) 0.dp else 10.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Violet.copy(alpha = 0.35f),
                spotColor = Violet.copy(alpha = 0.5f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    if (isSelected) {
                        listOf(Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.05f))
                    } else {
                        listOf(Color(0xFF2B2F61), Color(0xFF1A2148))
                    }
                )
            )
            .border(
                BorderStroke(
                    2.dp,
                    if (isSelected) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.16f)
                ),
                RoundedCornerShape(18.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !isSelected
            ) { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = value.toString(),
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                color = if (isSelected) Color.White.copy(alpha = 0.18f) else Color.White
            )
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.30f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(34.dp)
                )
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
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.86f else 1f,
                label = "operator_press"
            )

            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onOperatorClick(op)
                },
                modifier = Modifier.size(52.dp).scale(scale),
                shape = CircleShape,
                color = if (isPressed) Violet.copy(alpha = 0.75f) else Color.White.copy(alpha = 0.07f),
                border = BorderStroke(
                    1.dp,
                    if (isPressed) Violet.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.16f)
                ),
                shadowElevation = if (isPressed) 0.dp else 6.dp,
                interactionSource = interactionSource
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = op,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPressed) Color.White else InkWhite
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
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GameActionChip(
                text = stringResource(Res.string.game_action_undo),
                icon = Icons.Default.Undo,
                modifier = Modifier.weight(1f),
                onClick = onUndo
            )
            GameActionChip(
                text = stringResource(Res.string.game_action_reset),
                icon = Icons.Default.Refresh,
                modifier = Modifier.weight(1f),
                onClick = onReset
            )
            GameActionChip(
                text = stringResource(Res.string.game_action_hint_count, hintCredits),
                icon = Icons.Default.Lightbulb,
                accent = true,
                modifier = Modifier.weight(1.2f),
                onClick = onHint
            )
        }
        GradientButton(
            text = stringResource(Res.string.game_action_submit),
            onClick = onSubmit,
            icon = Icons.Default.Bolt
        )
    }
}

@Composable
fun GameActionChip(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        label = "chip_press"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.height(50.dp).scale(scale),
        shape = RoundedCornerShape(14.dp),
        color = if (accent) Gold.copy(alpha = 0.13f) else Color.White.copy(alpha = 0.06f),
        border = BorderStroke(
            1.dp,
            if (accent) Gold.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.14f)
        ),
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (accent) Gold else InkMuted,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = if (accent) Gold else InkWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun AdRequestDialog(state: GameState, viewModel: GameViewModel) {
    AlertDialog(
        onDismissRequest = { viewModel.handleIntent(GameIntent.DismissAdDialog) },
        containerColor = Midnight,
        shape = RoundedCornerShape(26.dp),
        titleContentColor = Color.White,
        textContentColor = InkMuted,
        title = {
            Text(
                text = stringResource(Res.string.game_hint_dialog_title),
                fontWeight = FontWeight.Black
            )
        },
        text = {
            Text(stringResource(Res.string.game_hint_dialog_desc, state.hintCredits))
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.hintCredits < 5) {
                    Button(
                        onClick = { viewModel.handleIntent(GameIntent.WatchAd) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Rose,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(stringResource(Res.string.game_hint_ad_get))
                    }
                }
                if (state.hintCredits > 0) {
                    Button(
                        onClick = { viewModel.handleIntent(GameIntent.UseHint) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(stringResource(Res.string.game_hint_use_now))
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.handleIntent(GameIntent.DismissAdDialog) }) {
                Text(
                    text = stringResource(Res.string.game_hint_cancel),
                    color = InkMuted
                )
            }
        }
    )
}

@Composable
fun LevelLockScreen(watched: Int, onWatchAd: () -> Unit) {
    GlassCard(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        shape = RoundedCornerShape(26.dp),
        fillAlpha = 0.06f,
        borderAlpha = 0.4f
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .shadow(20.dp, CircleShape, spotColor = Gold.copy(alpha = 0.35f))
                    .background(Gold.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(42.dp),
                    tint = Gold
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = stringResource(Res.string.game_lock_title),
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = InkWhite
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.game_lock_desc),
                fontSize = 15.sp,
                color = InkMuted
            )
            Spacer(modifier = Modifier.height(26.dp))
            GradientButton(
                text = stringResource(Res.string.game_lock_unlock_button, watched),
                onClick = onWatchAd,
                icon = Icons.Default.Lightbulb
            )
        }
    }
}

@Composable
fun AnswerFeedbackOverlay(feedback: AnswerFeedback?) {
    AnimatedVisibility(
        visible = feedback != null,
        enter = fadeIn(tween(120)) + scaleIn(initialScale = 0.75f, animationSpec = tween(240)),
        exit = fadeOut(tween(180))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val isCorrect = feedback?.type == AnswerFeedbackType.CORRECT
                Text(
                    text = if (isCorrect) stringResource(Res.string.game_fever_excellent) else stringResource(Res.string.game_fever_try_again),
                    color = if (isCorrect) Emerald else Rose,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                if (isCorrect) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "+${feedback?.scoreChange ?: 0}${stringResource(Res.string.common_score_unit)}",
                        color = Gold,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
fun LevelUpOverlay(newLevelKey: String) {
    val appear = remember { Animatable(0.82f) }
    LaunchedEffect(Unit) {
        appear.animateTo(1f, tween(420, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.82f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.scale(appear.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .shadow(28.dp, CircleShape, spotColor = Gold.copy(alpha = 0.5f))
                    .background(Brush.linearGradient(GoldGradient), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(Res.string.game_level_up_message, getLocalizedLevelLabel(newLevelKey)),
                color = Gold,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}

