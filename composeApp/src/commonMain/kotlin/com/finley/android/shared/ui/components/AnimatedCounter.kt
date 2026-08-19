package com.finley.android.shared.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

/**
 * 数字滚动组件：数值变化时平滑地滚动到新值，适合分数等统计展示。
 */
@Composable
fun AnimatedCounter(
    value: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default
) {
    var display by remember { mutableStateOf(value) }

    LaunchedEffect(value) {
        val from = display
        val to = value
        if (from == to) return@LaunchedEffect
        val steps = ((to - from).absoluteValue).coerceIn(1, 40)
        for (i in 1..steps) {
            display = from + (to - from) * i / steps
            delay(14)
        }
        display = to
    }

    Box(modifier = modifier) {
        Text(
            text = display.toString(),
            style = style,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}
