package com.finley.android.shared.ui.game.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import com.finley.android.shared.Res
import com.finley.android.shared.*

private val GameBackgroundStart = Color(0xFF1A1A2E)
private val GameBackgroundEnd = Color(0xFF162447)
private val CardAccent = Color(0xFFE94560)
private val Gold = Color(0xFFFFD700)

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
            .background(Brush.verticalGradient(listOf(GameBackgroundStart, GameBackgroundEnd)))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(modifier = Modifier.size(120.dp), shape = CircleShape, color = Gold.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(stringResource(Res.string.ad_label), fontSize = 48.sp, fontWeight = FontWeight.Black, color = Gold)
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
            Text(text = stringResource(Res.string.ad_content_title), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(Res.string.ad_content_subtitle), color = Color.White.copy(alpha = 0.6f), fontSize = 18.sp, textAlign = TextAlign.Center)
        }

        Surface(modifier = Modifier.align(Alignment.TopEnd).padding(24.dp), color = Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(24.dp)) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(progress = { animatedProgress }, modifier = Modifier.size(20.dp), color = CardAccent, strokeWidth = 3.dp, trackColor = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = stringResource(Res.string.ad_timer_label, timeLeft), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        IconButton(onClick = { showExitDialog = true }, modifier = Modifier.align(Alignment.TopStart).padding(16.dp).background(Color.White.copy(alpha = 0.1f), CircleShape)) {
            Icon(Icons.Default.Close, contentDescription = "Close Ad", tint = Color.White)
        }

        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                containerColor = Color(0xFF1F4068),
                titleContentColor = Color.White,
                textContentColor = Color.White.copy(alpha = 0.8f),
                title = { Text(stringResource(Res.string.ad_exit_dialog_title), fontWeight = FontWeight.Bold) },
                text = { Text(stringResource(Res.string.ad_exit_dialog_desc_alt)) },
                confirmButton = {
                    TextButton(onClick = onAdCancelled) {
                        Text(
                            stringResource(Res.string.ad_exit_dialog_confirm_alt),
                            color = Color.White
                        )
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showExitDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CardAccent,
                            contentColor = Color.White
                        )
                    ) { Text(stringResource(Res.string.ad_exit_dialog_cancel)) }
                }
            )
        }
    }
}
