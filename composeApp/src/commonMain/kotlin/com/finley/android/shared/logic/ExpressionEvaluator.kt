package com.finley.android.shared.logic

import kotlin.math.abs

object ExpressionEvaluator {
    private const val EPSILON = 1e-6

    fun evaluate(expression: String): Double? {
        return try {
            val tokens = tokenize(expression)
            val rpn = toRPN(tokens)
            evaluateRPN(rpn)
        } catch (e: Exception) {
            null
        }
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        while (i < expr.length) {
            val c = expr[i]
            when {
                c.isDigit() -> {
                    val sb = StringBuilder()
                    while (i < expr.length && expr[i].isDigit()) {
                        sb.append(expr[i])
                        i++
                    }
                    tokens.add(sb.toString())
                    continue
                }
                c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == ')' -> {
                    tokens.add(c.toString())
                }
                c.isWhitespace() -> {}
                else -> throw IllegalArgumentException("Invalid character: $c")
            }
            i++
        }
        return tokens
    }

    private fun toRPN(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val stack = ArrayDeque<String>()
        val precedence = mapOf("+" to 1, "-" to 1, "*" to 2, "/" to 2)

        for (token in tokens) {
            when {
                token.all { it.isDigit() } -> output.add(token)
                token == "(" -> stack.addLast(token)
                token == ")" -> {
                    while (stack.isNotEmpty() && stack.last() != "(") {
                        output.add(stack.removeLast())
                    }
                    if (stack.isNotEmpty()) stack.removeLast()
                }
                else -> {
                    while (stack.isNotEmpty() && stack.last() != "(" &&
                        precedence[stack.last()]!! >= precedence[token]!!
                    ) {
                        output.add(stack.removeLast())
                    }
                    stack.addLast(token)
                }
            }
        }
        while (stack.isNotEmpty()) {
            output.add(stack.removeLast())
        }
        return output
    }

    private fun evaluateRPN(rpn: List<String>): Double {
        val stack = ArrayDeque<Double>()
        for (token in rpn) {
            if (token.all { it.isDigit() }) {
                stack.addLast(token.toDouble())
            } else {
                val b = stack.removeLast()
                val a = stack.removeLast()
                when (token) {
                    "+" -> stack.addLast(a + b)
                    "-" -> stack.addLast(a - b)
                    "*" -> stack.addLast(a * b)
                    "/" -> {
                        if (abs(b) < EPSILON) throw ArithmeticException("Division by zero")
                        stack.addLast(a / b)
                    }
                }
            }
        }
        return stack.removeLast()
    }
}
