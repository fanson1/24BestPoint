package com.finley.android.shared.domain

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource

@Composable
fun getLocalizedLevelLabel(levelKey: String): String {
    val difficulty = GameDifficulty.fromKey(levelKey) ?: GameDifficulty.fromLegacyLabel(levelKey)
    return if (difficulty != null) stringResource(difficulty.labelRes) else levelKey
}