package com.finley.android.shared.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * 毛玻璃卡片：半透明白 + 细描边，营造轻盈的层次感。
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    fillAlpha: Float = 0.06f,
    borderAlpha: Float = 0.14f,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = Color.White.copy(alpha = fillAlpha),
        border = BorderStroke(1.dp, Color.White.copy(alpha = borderAlpha))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            content = content
        )
    }
}
