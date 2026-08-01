package com.finley.android.server.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        // Default to H2 for easy local development
        val isPostgres = System.getenv("DATABASE_URL") != null
        val driverClassName = if (isPostgres) "org.postgresql.Driver" else "org.h2.Driver"
        val jdbcURL = System.getenv("DATABASE_URL") ?: "jdbc:h2:file:./build/db_new;AUTO_SERVER=TRUE"

        val database = Database.connect(createHikariDataSource(jdbcURL, driverClassName))
        
        transaction(database) {
            // 使用 createMissingTablesAndColumns 确保新添加的 token 列被创建
            SchemaUtils.createMissingTablesAndColumns(Users)
            migrateLevelLabels()
        }
    }

    private fun migrateLevelLabels() {
        val mapping = mapOf(
            "小学" to "PRIMARY",
            "初中" to "JUNIOR",
            "高中" to "SENIOR",
            "大学" to "UNIVERSITY",
            "硕士" to "MASTER",
            "博士" to "DOCTOR",
            "博士后" to "POSTDOCTOR",
            "副教授" to "ASSOCIATE_PROFESSOR",
            "教授" to "PROFESSOR",
            "科学家" to "SCIENTIST"
        )
        
        mapping.forEach { (old, new) ->
            Users.update({ Users.currentLevel eq old }) {
                it[currentLevel] = new
            }
        }
    }

    private fun createHikariDataSource(url: String, driver: String) = HikariDataSource(HikariConfig().apply {
        driverClassName = driver
        jdbcUrl = url
        maximumPoolSize = 3
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    })

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}

object Users : Table() {
    val username = varchar("username", 50)
    val passwordHash = varchar("password_hash", 128)
    val nickname = varchar("nickname", 50).nullable()
    val hintCredits = integer("hint_credits").default(0)
    val currentLevel = varchar("current_level", 20).default("PRIMARY")
    val correctCountInLevel = integer("correct_count_in_level").default(0)
    val score = integer("score").default(0)
    val isLevelLockActive = bool("is_level_lock_active").default(false)
    val lockAdsWatched = integer("lock_ads_watched").default(0)
    val bestFeverCombo = integer("best_fever_combo").default(0)
    val totalAdsWatched = integer("total_ads_watched").default(0)
    val fastSolveCount = integer("fast_solve_count").default(0)
    val token = varchar("token", 128).nullable()

    override val primaryKey = PrimaryKey(username)
}
