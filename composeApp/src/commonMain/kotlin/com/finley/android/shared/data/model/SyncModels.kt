package com.finley.android.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ScoreSyncRequest(
    val username: String,
    val nickname: String?,
    val score: Int,
    val levelLabel: String
)

@Serializable
data class LeaderboardResponse(
    val scores: List<ScoreSyncRequest>
)

@Serializable
data class AuthRequest(
    val username: String,
    val passwordHash: String
)

@Serializable
data class AuthResponse(
    val status: String,
    val message: String,
    val user: User? = null
)
