package com.finley.android.shared.logic

import kotlin.math.abs

object ExpressionEvaluator {
    private const val EPSILON = 1e-6

    /**
     * Evaluates a tokenized arithmetic expression (shunting-yard + RPN).
     * Returns `null` if the expression is malformed or contains a division by zero.
     */
    fun evaluate(tokens: List<ExpressionToken>): Double? {
        return try {
            evaluateRPN(toRPN(tokens))
        } catch (e: Exception) {
            null
        }
    }

    private sealed interface RpnItem {
        data class Value(val number: Double) : RpnItem
        data class Operator(val symbol: String) : RpnItem
    }

    private fun toRPN(tokens: List<ExpressionToken>): List<RpnItem> {
        val output = mutableListOf<RpnItem>()
        val stack = ArrayDeque<String>()

        for (token in tokens) {
            when (token) {
                is ExpressionToken.Number -> output.add(RpnItem.Value(token.value.toDouble()))
                is ExpressionToken.Operator -> when (token.symbol) {
                    "(" -> stack.addLast(token.symbol)
                    ")" -> {
                        while (stack.isNotEmpty() && stack.last() != "(") {
                            output.add(RpnItem.Operator(stack.removeLast()))
                        }
                        if (stack.isNotEmpty()) stack.removeLast()
                    }
                    else -> {
                        while (stack.isNotEmpty() && stack.last() != "(" &&
                            precedence(stack.last()) >= precedence(token.symbol)
                        ) {
                            output.add(RpnItem.Operator(stack.removeLast()))
                        }
                        stack.addLast(token.symbol)
                    }
                }
            }
        }
        while (stack.isNotEmpty()) {
            output.add(RpnItem.Operator(stack.removeLast()))
        }
        return output
    }

    private fun precedence(operator: String): Int = when (operator) {
        "+", "-" -> 1
        "*", "/" -> 2
        else -> 0
    }

    private fun evaluateRPN(rpn: List<RpnItem>): Double {
        val stack = ArrayDeque<Double>()
        for (item in rpn) {
            when (item) {
                is RpnItem.Value -> stack.addLast(item.number)
                is RpnItem.Operator -> {
                    val b = stack.removeLast()
                    val a = stack.removeLast()
                    val result = when (item.symbol) {
                        "+" -> a + b
                        "-" -> a - b
                        "*" -> a * b
                        "/" -> {
                            if (abs(b) < EPSILON) throw ArithmeticException("Division by zero")
                            a / b
                        }
                        else -> throw IllegalArgumentException("Unknown operator: ${item.symbol}")
                    }
                    stack.addLast(result)
                }
            }
        }
        return stack.removeLast()
    }
}