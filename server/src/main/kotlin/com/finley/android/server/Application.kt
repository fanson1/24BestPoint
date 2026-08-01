package com.finley.android.server

import com.finley.android.server.db.DatabaseFactory
import com.finley.android.server.db.Users
import com.finley.android.shared.data.model.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID

fun main() {
    embeddedServer(Netty, port = 8081, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()

    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowHeader("X-Requested-With")
        allowHeader("X-Auth-Token")
        allowCredentials = true
        maxAgeInSeconds = 3600
    }

    install(ContentNegotiation) {
        json()
    }

    intercept(ApplicationCallPipeline.Plugins) {
        if (call.request.uri.startsWith("/api/")) {
            call.response.header(HttpHeaders.CacheControl, "no-cache, no-store, must-revalidate")
            call.response.header(HttpHeaders.Pragma, "no-cache")
            call.response.header(HttpHeaders.Expires, "0")
        }
    }

    routing {
        get("/") {
            call.respondText("24 Best Point Game Server is running with SSO support!")
        }
        
        route("/api") {
            post("/register") {
                try {
                    val request = call.receive<AuthRequest>()
                    println("Server: Registering user: ${request.username}")
                    val newToken = UUID.randomUUID().toString()
                    val result = DatabaseFactory.dbQuery {
                        val existing = Users.select(Users.username)
                            .where { Users.username eq request.username }
                            .singleOrNull()

                        if (existing == null) {
                            Users.insert {
                                it[username] = request.username
                                it[passwordHash] = request.passwordHash
                                it[token] = newToken
                            }
                            User(username = request.username, passwordHash = request.passwordHash, token = newToken)
                        } else null
                    }
                    
                    if (result != null) {
                        call.respond(AuthResponse("success", "Registration successful", result))
                    } else {
                        call.respond(HttpStatusCode.Conflict, AuthResponse("error", "User already exists"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, AuthResponse("error", e.message ?: "Server error"))
                }
            }

            post("/login") {
                try {
                    val request = call.receive<AuthRequest>()
                    println("Server: Login attempt for user: ${request.username}")
                    val newToken = UUID.randomUUID().toString()
                    val user = DatabaseFactory.dbQuery {
                        val row = Users.selectAll()
                            .where { (Users.username eq request.username) and (Users.passwordHash eq request.passwordHash) }
                            .singleOrNull()

                        if (row != null) {
                            Users.update({ Users.username eq request.username }) {
                                it[token] = newToken
                            }
                            User(
                                username = row[Users.username],
                                passwordHash = row[Users.passwordHash],
                                nickname = row[Users.nickname],
                                hintCredits = row[Users.hintCredits],
                                currentLevel = row[Users.currentLevel],
                                correctCountInLevel = row[Users.correctCountInLevel],
                                score = row[Users.score],
                                isLevelLockActive = row[Users.isLevelLockActive],
                                lockAdsWatched = row[Users.lockAdsWatched],
                                bestFeverCombo = row[Users.bestFeverCombo],
                                totalAdsWatched = row[Users.totalAdsWatched],
                                fastSolveCount = row[Users.fastSolveCount],
                                token = newToken
                            )
                        } else null
                    }
                    
                    if (user != null) {
                        call.respond(AuthResponse("success", "Login successful", user))
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, AuthResponse("error", "Invalid username or password"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, AuthResponse("error", e.message ?: "Server error"))
                }
            }

            get("/user/{username}") {
                try {
                    val username = call.parameters["username"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                    val requestToken = call.request.headers["X-Auth-Token"]
                    
                    val user = DatabaseFactory.dbQuery {
                        Users.selectAll().where { Users.username eq username }.singleOrNull()?.let {
                            val dbToken = it[Users.token]
                            if (dbToken != null && dbToken == requestToken) {
                                User(
                                    username = it[Users.username],
                                    passwordHash = it[Users.passwordHash],
                                    nickname = it[Users.nickname],
                                    hintCredits = it[Users.hintCredits],
                                    currentLevel = it[Users.currentLevel],
                                    correctCountInLevel = it[Users.correctCountInLevel],
                                    score = it[Users.score],
                                    isLevelLockActive = it[Users.isLevelLockActive],
                                    lockAdsWatched = it[Users.lockAdsWatched],
                                    bestFeverCombo = it[Users.bestFeverCombo],
                                    totalAdsWatched = it[Users.totalAdsWatched],
                                    fastSolveCount = it[Users.fastSolveCount],
                                    token = dbToken
                                )
                            } else null
                        }
                    }
                    
                    if (user != null) {
                        call.respond(AuthResponse("success", "User found", user))
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, AuthResponse("error", "Session expired"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, AuthResponse("error", "Server error"))
                }
            }

            post("/user/update") {
                try {
                    val request = call.receive<User>()
                    val requestToken = call.request.headers["X-Auth-Token"]
                    
                    val success = DatabaseFactory.dbQuery {
                        val row = Users.selectAll().where { Users.username eq request.username }.singleOrNull()
                        val dbToken = row?.get(Users.token)
                        if (dbToken != null && dbToken == requestToken) {
                            Users.update({ Users.username eq request.username }) {
                                it[nickname] = request.nickname
                                it[hintCredits] = request.hintCredits
                                it[currentLevel] = request.currentLevel
                                it[correctCountInLevel] = request.correctCountInLevel
                                it[score] = request.score
                                it[isLevelLockActive] = request.isLevelLockActive
                                it[lockAdsWatched] = request.lockAdsWatched
                                it[bestFeverCombo] = request.bestFeverCombo
                                it[totalAdsWatched] = request.totalAdsWatched
                                it[fastSolveCount] = request.fastSolveCount
                            }
                            true
                        } else false
                    }
                    
                    if (success) {
                        call.respond(mapOf("status" to "success"))
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("status" to "error", "message" to "Session expired"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("status" to "error", "message" to "Server error"))
                }
            }

            post("/sync") {
                try {
                    val request = call.receive<ScoreSyncRequest>()
                    val requestToken = call.request.headers["X-Auth-Token"]
                    
                    val success = DatabaseFactory.dbQuery {
                        val row = Users.selectAll().where { Users.username eq request.username }.singleOrNull()
                        val dbToken = row?.get(Users.token)
                        if (dbToken != null && dbToken == requestToken) {
                            Users.update({ Users.username eq request.username }) {
                                it[score] = request.score
                                it[currentLevel] = request.levelLabel
                                if (request.nickname != null) it[nickname] = request.nickname
                            }
                            true
                        } else false
                    }
                    
                    if (success) {
                        call.respond(mapOf("status" to "success"))
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("status" to "error", "message" to "Session expired"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("status" to "error", "message" to "Server error"))
                }
            }
            
            get("/leaderboard") {
                val sortedScores = DatabaseFactory.dbQuery {
                    Users.selectAll()
                        .orderBy(Users.score to SortOrder.DESC)
                        .limit(50)
                        .map {
                            ScoreSyncRequest(
                                username = it[Users.username],
                                nickname = it[Users.nickname],
                                score = it[Users.score],
                                levelLabel = it[Users.currentLevel]
                            )
                        }
                }
                call.respond(LeaderboardResponse(sortedScores))
            }
        }
    }
}
