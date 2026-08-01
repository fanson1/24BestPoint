package com.finley.android.shared.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import com.finley.android.shared.Res
import com.finley.android.shared.notosans_sc
import org.jetbrains.compose.resources.Font

@Composable
actual fun getTypography(): Typography {
    val font = Font(Res.font.notosans_sc)
    val chineseFontFamily = FontFamily(font)
    return provideTypography(chineseFontFamily)
}
