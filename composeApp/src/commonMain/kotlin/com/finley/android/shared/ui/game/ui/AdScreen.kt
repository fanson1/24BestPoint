package com.finley.android.shared.ui.game.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.shared.Res
import com.finley.android.shared.ad_content_subtitle
import com.finley.android.shared.ad_content_title
import com.finley.android.shared.ad_exit_dialog_cancel
import com.finley.android.shared.ad_exit_dialog_confirm_alt
import com.finley.android.shared.ad_exit_dialog_desc_alt
import com.finley.android.shared.ad_exit_dialog_title
import com.finley.android.shared.ad_label
import com.finley.android.shared.ad_timer_label
import com.finley.android.shared.theme.BrandGradient
import com.finley.android.shared.theme.Gold
import com.finley.android.shared.theme.InkMuted
import com.finley.android.shared.theme.InkWhite
import com.finley.android.shared.theme.Midnight
import com.finley.android.shared.theme.Rose
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@Composable
fun AdScreen(onAdFinished: () -> Unit, onAdCancelled: () -> Unit) {
    var timeLeft by remember { mutableIntStateOf(2) }
    var showExitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
        onAdFinished()
    }

    val animatedProgress by animateFloatAsState(
        targetValue = timeLeft / 2f,
        animationSpec = tween(1000),
        label = "progress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0A0E22), Color(0xFF201A44))))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(132.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(BrandGradient)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.ad_label),
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 4.sp
                )
            }
            Spacer(modifier = Modifier.height(44.dp))
            Text(
                text = stringResource(Res.string.ad_content_title),
                color = InkWhite,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(Res.string.ad_content_subtitle),
                color = InkMuted,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
        }

        // 倒计时进度环
        Surface(
            modifier = Modifier.align(Alignment.TopEnd).padding(24.dp),
            color = Color.Black.copy(alpha = 0.45f),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CountdownRing(
                    progress = animatedProgress,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(Res.string.ad_timer_label, timeLeft),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        IconButton(
            onClick = { showExitDialog = true },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(Color.White.copy(alpha = 0.10f), CircleShape)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close Ad", tint = Color.White)
        }

        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                containerColor = Midnight,
                shape = RoundedCornerShape(26.dp),
                titleContentColor = Color.White,
                textContentColor = InkMuted,
                title = { Text(stringResource(Res.string.ad_exit_dialog_title), fontWeight = FontWeight.Black) },
                text = { Text(stringResource(Res.string.ad_exit_dialog_desc_alt)) },
                confirmButton = {
                    TextButton(onClick = onAdCancelled) {
                        Text(
                            text = stringResource(Res.string.ad_exit_dialog_confirm_alt),
                            color = InkWhite
                        )
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showExitDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Rose,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text(stringResource(Res.string.ad_exit_dialog_cancel)) }
                }
            )
        }
    }
}

@Composable
fun CountdownRing(progress: Float, modifier: Modifier = Modifier) {
    val sweep by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f) * 360f,
        animationSpec = tween(1000),
        label = "ring_sweep"
    )
    Canvas(modifier = modifier) {
        val strokeWidth = 4.dp.toPx()
        val inset = strokeWidth / 2
        val arcSize = androidx.compose.ui.geometry.Size(
            width = size.width - strokeWidth,
            height = size.height - strokeWidth
        )
        drawArc(
            color = Color.White.copy(alpha = 0.12f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(width = strokeWidth)
        )
        drawArc(
            color = Gold,
            startAngle = -90f,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}
