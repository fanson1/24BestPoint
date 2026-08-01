package com.finley.android.shared.data.model

data class GameScore(
    val playerName: String,
    val nickname: String? = null,
    val score: Int,
    val levelLabel: String = "PRIMARY",
    val timeTakenMillis: Long,
    val timestamp: Long
)
