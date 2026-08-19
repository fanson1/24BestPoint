package com.finley.android.shared.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.shared.theme.BrandGradient
import com.finley.android.shared.theme.InkWhite

/**
 * 渐变主按钮：渐变底色 + 按下缩放反馈 + 阴影光晕。
 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradient: List<Color> = BrandGradient,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    height: Dp = 56.dp,
    fontSize: TextUnit = 18.sp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        label = "gradient_button_scale"
    )

    Box(modifier = modifier.scale(scale)) {
        Surface(
            onClick = onClick,
            enabled = enabled && !loading,
            interactionSource = interactionSource,
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
            shape = RoundedCornerShape(18.dp),
            color = Color.Transparent,
            shadowElevation = if (enabled && !loading) 14.dp else 0.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.horizontalGradient(gradient)),
                contentAlignment = Alignment.Center
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        color = InkWhite,
                        modifier = Modifier.size(26.dp),
                        strokeWidth = 3.dp
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (icon != null) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = text,
                            color = Color.White,
                            fontSize = fontSize,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}
