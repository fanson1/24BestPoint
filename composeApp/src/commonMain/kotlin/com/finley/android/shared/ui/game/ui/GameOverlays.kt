package com.finley.android.shared.ui.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.shared.Res
import com.finley.android.shared.common_score_unit
import com.finley.android.shared.game_fever_excellent
import com.finley.android.shared.game_fever_try_again
import com.finley.android.shared.game_level_up_message
import com.finley.android.shared.domain.getLocalizedLevelLabel
import com.finley.android.shared.theme.Emerald
import com.finley.android.shared.theme.Gold
import com.finley.android.shared.theme.GoldGradient
import com.finley.android.shared.theme.Rose
import com.finley.android.shared.ui.game.mvi.AnswerFeedback
import com.finley.android.shared.ui.game.mvi.AnswerFeedbackType
import org.jetbrains.compose.resources.stringResource

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