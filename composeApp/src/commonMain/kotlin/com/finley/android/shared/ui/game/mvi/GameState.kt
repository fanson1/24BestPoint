package com.finley.android.shared.ui.game.mvi

import com.finley.android.shared.domain.GameDifficulty
import com.finley.android.shared.logic.ExpressionToken

data class GameState(
    val username: String = "",
    val nickname: String? = null,
    val numbers: List<Int> = emptyList(),
    val expressionTokens: List<ExpressionToken> = emptyList(),
    val score: Int = 0,
    val difficulty: GameDifficulty = GameDifficulty.PRIMARY,
    val correctCountInLevel: Int = 0,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val hintContent: String? = null,
    val hintLevel: Int = 0,
    val showAdDialog: Boolean = false,
    val hintCredits: Int = 0,
    val isLevelLockActive: Boolean = false,
    val lockAdsWatched: Int = 0,
    val answerFeedback: AnswerFeedback? = null,
    val isFeverMode: Boolean = false,
    val timeLeft: Int = 0,
    val comboCount: Int = 0,
    val currentPuzzleStartTime: Long = 0,
    val showLevelUp: String? = null
) {
    /** Human-readable rendering of the current expression, e.g. "12+3". */
    val currentExpression: String
        get() = expressionTokens.joinToString("") { token ->
            when (token) {
                is ExpressionToken.Number -> token.value.toString()
                is ExpressionToken.Operator -> token.symbol
            }
        }

    /** Indices of puzzle cards that have been used so far. */
    val selectedIndices: Set<Int>
        get() = buildSet {
            for (token in expressionTokens) {
                if (token is ExpressionToken.Number) add(token.index)
            }
        }
}

data class AnswerFeedback(
    val type: AnswerFeedbackType,
    val scoreChange: Int = 0,
    val combo: Int = 0
)

enum class AnswerFeedbackType {
    CORRECT, INCORRECT
}

sealed interface GameIntent {
    object LoadNewPuzzle : GameIntent
    data class SelectNumber(val index: Int, val value: Int) : GameIntent
    data class SelectOperator(val operator: String) : GameIntent
    object Undo : GameIntent
    object Reset : GameIntent
    object Submit : GameIntent
    object RequestHint : GameIntent
    object UseHint : GameIntent
    object WatchAd : GameIntent
    object WatchLockAd : GameIntent
    object DismissAdDialog : GameIntent
    object Logout : GameIntent
    object ToggleFeverMode : GameIntent
}

sealed interface GameEffect {
    object ShowSuccessAnim : GameEffect
    data class ShowError(val message: String) : GameEffect
    object NavigateToLeaderboard : GameEffect
    object NavigateToAd : GameEffect
    object NavigateToAuth : GameEffect
    data class LevelUp(val newDifficulty: String) : GameEffect
    object FeverGameOver : GameEffect
}