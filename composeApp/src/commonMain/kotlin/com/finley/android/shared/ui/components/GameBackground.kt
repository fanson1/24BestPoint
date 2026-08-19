package com.finley.android.shared.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.finley.android.shared.theme.GameBackgroundGradient
import com.finley.android.shared.theme.Rose
import com.finley.android.shared.theme.Sky
import com.finley.android.shared.theme.Violet
import kotlin.math.cos
import kotlin.math.sin

/**
 * 全屏动态极光背景：深空渐变 + 缓慢漂移的彩色光晕 + 微弱星点。
 * 适合作为所有页面的底层背景。
 */
@Composable
fun GameBackground(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "aurora")
    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )
    val glow by infinite.animateFloat(
        initialValue = 0.28f,
        targetValue = 0.52f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    val twinkle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(GameBackgroundGradient))
            .drawBehind {
                val rad = size.maxDimension * 0.62f
                val baseRadian = phase * 0.0174533f

                val x1 = size.width * (0.5f + 0.34f * cos(baseRadian.toDouble()).toFloat())
                val y1 = size.height * (0.32f + 0.22f * sin((baseRadian * 0.72f).toDouble()).toFloat())
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Violet.copy(alpha = glow), Color.Transparent),
                        center = Offset(x1, y1),
                        radius = rad
                    ),
                    radius = rad,
                    center = Offset(x1, y1)
                )

                val radian2 = baseRadian * 1.6f
                val x2 = size.width * (0.72f + 0.2f * cos(radian2.toDouble()).toFloat())
                val y2 = size.height * (0.68f + 0.18f * sin((radian2 * 0.6f).toDouble()).toFloat())
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Rose.copy(alpha = glow * 0.7f), Color.Transparent),
                        center = Offset(x2, y2),
                        radius = rad * 0.8f
                    ),
                    radius = rad * 0.8f,
                    center = Offset(x2, y2)
                )

                val radian3 = baseRadian * 2.3f
                val x3 = size.width * (0.2f + 0.2f * cos(radian3.toDouble()).toFloat())
                val y3 = size.height * (0.75f + 0.15f * sin(radian3.toDouble()).toFloat())
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Sky.copy(alpha = glow * 0.5f), Color.Transparent),
                        center = Offset(x3, y3),
                        radius = rad * 0.7f
                    ),
                    radius = rad * 0.7f,
                    center = Offset(x3, y3)
                )

                // 微弱星点
                val dots = listOf(
                    0.12f to 0.22f, 0.85f to 0.15f, 0.72f to 0.42f, 0.25f to 0.62f,
                    0.90f to 0.76f, 0.08f to 0.84f, 0.52f to 0.10f, 0.46f to 0.88f,
                    0.64f to 0.28f, 0.34f to 0.46f
                )
                dots.forEachIndexed { index, (fx, fy) ->
                    val wave = (0.5f + 0.5f * sin((twinkle * 6.283f + index).toDouble()).toFloat())
                    drawCircle(
                        color = Color.White.copy(alpha = 0.03f + 0.05f * wave),
                        radius = size.minDimension * (0.004f + (index % 3) * 0.002f),
                        center = Offset(size.width * fx, size.height * fy)
                    )
                }
            }
    )
}
