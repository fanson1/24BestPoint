package com.finley.android.shared.logic

/**
 * A single element of the player's in-progress expression.
 *
 * Numbers reference the puzzle card they came from (via [Number.index]) so that
 * multi-digit values (e.g. 12, 24, 999) can be represented unambiguously and
 * undo/reset stay trivially correct.
 */
sealed interface ExpressionToken {
    data class Number(val index: Int, val value: Int) : ExpressionToken
    data class Operator(val symbol: String) : ExpressionToken
}