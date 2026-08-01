package com.finley.android.shared.domain

import com.finley.android.shared.Res
import com.finley.android.shared.*
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
}
