package com.finley.android.shared.data.repository

import com.finley.android.shared.data.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlin.time.Clock

class GameRepository(private val client: HttpClient) {
    // baseUrl 已经包含 /api
    private val currentBaseUrl = baseUrl

    // 用于 SSO 的 Token
    private var authToken: String? = null
    
    // 用于通知外部 Token 已失效
    var onUnauthorized: (() -> Unit)? = null

    fun setAuthToken(token: String?) {
        this.authToken = token
    }

    private fun HttpRequestBuilder.withAuth() {
        authToken?.let { header("X-Auth-Token", it) }
    }

    private fun checkResponse(status: HttpStatusCode) {
        if (status == HttpStatusCode.Unauthorized) {
            onUnauthorized?.invoke()
        }
    }

    // 生成当前时间戳，用于 Cache Busting
    private fun getTimestamp(): Long = Clock.System.now().toEpochMilliseconds()

    suspend fun register(username: String, passwordHash: String): AuthResponse {
        return try {
            val response = client.post("$currentBaseUrl/register") {
                setBody(AuthRequest(username, passwordHash))
                contentType(ContentType.Application.Json)
            }
            checkResponse(response.status)
            val authResponse: AuthResponse = response.body()
            if (authResponse.status == "success") setAuthToken(authResponse.user?.token)
            authResponse
        } catch (e: Exception) {
            AuthResponse("error", e.message ?: "Unknown error")
        }
    }

    suspend fun login(username: String, passwordHash: String): AuthResponse {
        return try {
            val response = client.post("$currentBaseUrl/login") {
                setBody(AuthRequest(username, passwordHash))
                contentType(ContentType.Application.Json)
            }
            checkResponse(response.status)
            val authResponse: AuthResponse = response.body()
            if (authResponse.status == "success") setAuthToken(authResponse.user?.token)
            authResponse
        } catch (e: Exception) {
            AuthResponse("error", e.message ?: "Unknown error")
        }
    }

    suspend fun getUser(username: String): User? {
        return try {
            val response = client.get("$currentBaseUrl/user/$username") {
                parameter("t", getTimestamp())
                withAuth()
            }
            checkResponse(response.status)
            val authResponse: AuthResponse = response.body()
            if (authResponse.status == "success") authResponse.user else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUser(user: User) {
        try {
            val response = client.post("$currentBaseUrl/user/update") {
                setBody(user)
                contentType(ContentType.Application.Json)
                withAuth()
            }
            checkResponse(response.status)
            println("GameRepository: Update user ${user.username} result: ${response.status}")
        } catch (e: Exception) {
            println("GameRepository: Update user error: ${e.message}")
        }
    }

    suspend fun syncScore(username: String, nickname: String?, score: Int, level: String) {
        try {
            val response = client.post("$currentBaseUrl/sync") {
                setBody(ScoreSyncRequest(username, nickname, score, level))
                contentType(ContentType.Application.Json)
                withAuth()
            }
            checkResponse(response.status)
            println("GameRepository: Sync score for $username result: ${response.status}")
        } catch (e: Exception) {
            println("GameRepository: Sync score error: ${e.message}")
        }
    }

    suspend fun getGlobalLeaderboard(): List<ScoreSyncRequest> {
        return try {
            val response = client.get("$currentBaseUrl/leaderboard") {
                parameter("t", getTimestamp())
                withAuth() // 添加 Auth 以便即时捕获 Token 失效
            }
            checkResponse(response.status)
            val leaderResponse: LeaderboardResponse = response.body()
            leaderResponse.scores
        } catch (e: Exception) {
            emptyList()
        }
    }
}
