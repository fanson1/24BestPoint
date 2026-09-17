package com.finley.android.shared.domain

import com.finley.android.shared.Res
import com.finley.android.shared.level_associate_professor
import com.finley.android.shared.level_doctor
import com.finley.android.shared.level_junior
import com.finley.android.shared.level_master
import com.finley.android.shared.level_postdoctor
import com.finley.android.shared.level_primary
import com.finley.android.shared.level_professor
import com.finley.android.shared.level_scientist
import com.finley.android.shared.level_senior
import com.finley.android.shared.level_university
import org.jetbrains.compose.resources.StringResource

enum class GameDifficulty(val labelRes: StringResource, val maxNumber: Int, val scorePerQuestion: Int) {
    PRIMARY(Res.string.level_primary, 9, 10),
    JUNIOR(Res.string.level_junior, 13, 20),
    SENIOR(Res.string.level_senior, 20, 30),
    UNIVERSITY(Res.string.level_university, 30, 50),
    MASTER(Res.string.level_master, 50, 80),
    DOCTOR(Res.string.level_doctor, 70, 120),
    POSTDOCTOR(Res.string.level_postdoctor, 100, 200),
    ASSOCIATE_PROFESSOR(Res.string.level_associate_professor, 150, 300),
    PROFESSOR(Res.string.level_professor, 200, 500),
    SCIENTIST(Res.string.level_scientist, 999, 1000);

    fun next(): GameDifficulty? {
        val nextIndex = ordinal + 1
        return if (nextIndex < entries.size) entries[nextIndex] else null
    }

    companion object {
        fun fromKey(key: String): GameDifficulty? = entries.firstOrNull { it.name == key }

        /** Legacy labels from earlier versions of the app, mapped to the stable enum keys. */
        fun fromLegacyLabel(label: String): GameDifficulty? = when (label) {
            "小学" -> PRIMARY
            "初中" -> JUNIOR
            "高中" -> SENIOR
            "大学" -> UNIVERSITY
            "硕士" -> MASTER
            "博士" -> DOCTOR
            "博士后" -> POSTDOCTOR
            "副教授" -> ASSOCIATE_PROFESSOR
            "教授" -> PROFESSOR
            "科学家" -> SCIENTIST
            else -> null
        }
    }
}