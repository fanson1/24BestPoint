package com.finley.android.shared.logic

import org.kotlincrypto.hash.sha2.SHA256

object SecurityUtils {
    private const val SCORE_KEY = 0x5A5A5A5A

    /**
     * 使用纯 Kotlin 实现的 SHA256，确保 Android, JVM, iOS, Web(Wasm) 端逻辑完全一致
     */
    fun sha256(input: String): String {
        val digest = SHA256().digest(input.encodeToByteArray())
        return digest.joinToString("") { it.toUByte().toString(16).padStart(2, '0') }
    }

    fun obfuscateScore(score: Int): Int {
        if (score == 0) return 0
        return score xor SCORE_KEY
    }

    fun deobfuscateScore(obfuscatedScore: Int): Int {
        if (obfuscatedScore == 0) return 0
        return obfuscatedScore xor SCORE_KEY
    }
}
