package com.finley.android.shared.domain

import androidx.compose.runtime.Composable
import com.finley.android.shared.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun getLocalizedLevelLabel(levelKey: String): String {
    val resource = getLevelResource(levelKey)
    return if (resource != null) stringResource(resource) else levelKey
}

fun getLevelResource(levelKey: String): StringResource? {
    return when (levelKey) {
        "PRIMARY" -> Res.string.level_primary
        "JUNIOR" -> Res.string.level_junior
        "SENIOR" -> Res.string.level_senior
        "UNIVERSITY" -> Res.string.level_university
        "MASTER" -> Res.string.level_master
        "DOCTOR" -> Res.string.level_doctor
        "POSTDOCTOR" -> Res.string.level_postdoctor
        "ASSOCIATE_PROFESSOR" -> Res.string.level_associate_professor
        "PROFESSOR" -> Res.string.level_professor
        "SCIENTIST" -> Res.string.level_scientist
        else -> null
    }
}
