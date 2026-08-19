package com.finley.android.shared.ui.leaderboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.shared.Res
import com.finley.android.shared.common_back
import com.finley.android.shared.common_score_unit
import com.finley.android.shared.data.model.GameScore
import com.finley.android.shared.game_level_label
import com.finley.android.shared.leaderboard_empty_desc
import com.finley.android.shared.leaderboard_empty_title
import com.finley.android.shared.leaderboard_title
import com.finley.android.shared.domain.getLocalizedLevelLabel
import com.finley.android.shared.theme.Gold
import com.finley.android.shared.theme.InkMuted
import com.finley.android.shared.theme.InkWhite
import com.finley.android.shared.theme.RankBronze
import com.finley.android.shared.theme.RankGold
import com.finley.android.shared.theme.RankSilver
import com.finley.android.shared.theme.Rose
import com.finley.android.shared.theme.Violet
import com.finley.android.shared.ui.components.GlassCard
import com.finley.android.shared.ui.components.GlassTopBar
import com.finley.android.shared.ui.leaderboard.logic.LeaderboardViewModel
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(viewModel: LeaderboardViewModel, onNavigateBack: () -> Unit) {
    val scores by viewModel.topScores.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    // 每次进入排行榜都刷新最新数据
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    // 自定义下拉刷新逻辑
    val density = LocalDensity.current
    val refreshThreshold = with(density) { 80.dp.toPx() }
    var pullDistance by remember { mutableStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput && available.y < 0 && pullDistance > 0) {
                    val consumed = if (pullDistance + available.y > 0) available.y else -pullDistance
                    pullDistance += consumed
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput && available.y > 0) {
                    pullDistance += available.y * 0.5f // 阻尼效果
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (pullDistance > refreshThreshold) {
                    viewModel.refresh()
                }
                pullDistance = 0f
                return super.onPostFling(consumed, available)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            GlassTopBar(
                title = stringResource(Res.string.leaderboard_title),
                navigationContent = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.common_back),
                            tint = InkWhite
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = InkWhite)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .windowInsetsPadding(WindowInsets.systemBars)
                .nestedScroll(nestedScrollConnection)
        ) {
            // 列表内容
            if (scores.isEmpty() && !isRefreshing) {
                LeaderboardEmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset { IntOffset(0, pullDistance.roundToInt()) },
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    if (scores.isNotEmpty()) {
                        item(key = "podium") {
                            PodiumSection(scores.take(3))
                        }
                    }
                    itemsIndexed(scores) { index, score ->
                        if (index >= 3) {
                            ScoreItem(rank = index + 1, score = score)
                        }
                    }
                }
            }

            // 刷新指示器
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset { IntOffset(0, (pullDistance.roundToInt() - 100).coerceAtLeast(16)) }
            ) {
                if (isRefreshing || pullDistance > 0) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = Rose,
                        shadowElevation = 4.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PodiumSection(top: List<GameScore>) {
    val rank1 = top.getOrNull(0) ?: return
    val rank2 = top.getOrNull(1)
    val rank3 = top.getOrNull(2)

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            if (rank2 != null) {
                PodiumItem(rank = 2, score = rank2, modifier = Modifier.weight(1f))
            }
            PodiumItem(rank = 1, score = rank1, modifier = Modifier.weight(1.15f))
            if (rank3 != null) {
                PodiumItem(rank = 3, score = rank3, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun PodiumItem(rank: Int, score: GameScore, modifier: Modifier) {
    val rankColor = when (rank) {
        1 -> RankGold
        2 -> RankSilver
        3 -> RankBronze
        else -> InkMuted
    }
    val podiumHeight = when (rank) {
        1 -> 150
        2 -> 112
        else -> 86
    }
    val displayName = if (score.nickname?.isNotEmpty() == true) {
        "${score.playerName} (${score.nickname})"
    } else {
        score.playerName
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .border(2.dp, rankColor.copy(alpha = 0.7f), CircleShape)
                .background(Color.White.copy(alpha = 0.07f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = rankColor,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = displayName,
            color = InkWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        )
        Text(
            text = "${score.score}",
            color = rankColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(podiumHeight.dp)
                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                .background(rankColor.copy(alpha = 0.16f))
                .border(1.dp, rankColor.copy(alpha = 0.3f), RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = rank.toString(),
                color = rankColor,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun LeaderboardEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .shadow(
                    elevation = 18.dp,
                    shape = CircleShape,
                    ambientColor = Violet.copy(alpha = 0.35f),
                    spotColor = Violet.copy(alpha = 0.5f)
                )
                .background(Color.White.copy(alpha = 0.05f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Leaderboard,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(Res.string.leaderboard_empty_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = InkWhite
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(Res.string.leaderboard_empty_desc),
            fontSize = 16.sp,
            color = InkMuted,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ScoreItem(rank: Int, score: GameScore) {
    val rankColor = when (rank) {
        1 -> RankGold
        2 -> RankSilver
        3 -> RankBronze
        else -> InkMuted
    }

    GlassCard(
        shape = RoundedCornerShape(18.dp),
        fillAlpha = 0.06f,
        borderAlpha = 0.10f
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(rankColor.copy(alpha = 0.14f), CircleShape)
                    .border(1.dp, rankColor.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (rank <= 3) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = rankColor,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(text = "$rank", color = InkWhite, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                val displayName = if (score.nickname?.isNotEmpty() == true) {
                    "${score.playerName} (${score.nickname})"
                } else {
                    score.playerName
                }
                Text(
                    text = displayName,
                    color = InkWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(Res.string.game_level_label, getLocalizedLevelLabel(score.levelLabel)),
                    color = InkMuted,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "${score.score}",
                color = Rose,
                fontSize = 19.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = stringResource(Res.string.common_score_unit),
                color = InkMuted,
                fontSize = 14.sp
            )
        }
    }
}
