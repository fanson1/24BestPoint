package com.finley.android.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val username: String,
    val passwordHash: String,
    val nickname: String? = null,
    val hintCredits: Int = 0,
    val currentLevel: String = "PRIMARY",
    val correctCountInLevel: Int = 0,
    val score: Int = 0,
    val isLevelLockActive: Boolean = false,
    val lockAdsWatched: Int = 0,
    val bestFeverCombo: Int = 0,
    val totalAdsWatched: Int = 0,
    val fastSolveCount: Int = 0,
    val token: String? = null
)
