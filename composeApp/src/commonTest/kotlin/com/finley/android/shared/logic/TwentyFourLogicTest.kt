package com.finley.android.shared.logic

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ExpressionEvaluatorTest {

    private fun assertEvaluatesTo(expected: Double, tokens: List<ExpressionToken>) {
        val actual = ExpressionEvaluator.evaluate(tokens)
        assertNotNull(actual, "expression should evaluate")
        assertEquals(expected, actual, 1e-9)
    }

    @Test
    fun evaluatesSimpleArithmetic() {
        val tokens = listOf(
            ExpressionToken.Number(0, 1),
            ExpressionToken.Operator("+"),
            ExpressionToken.Number(1, 2)
        )
        assertEvaluatesTo(3.0, tokens)
    }

    @Test
    fun respectsOperatorPrecedence() {
        val tokens = listOf(
            ExpressionToken.Number(0, 1),
            ExpressionToken.Operator("+"),
            ExpressionToken.Number(1, 2),
            ExpressionToken.Operator("*"),
            ExpressionToken.Number(2, 3)
        )
        assertEvaluatesTo(7.0, tokens)
    }

    @Test
    fun respectsParentheses() {
        val tokens = listOf(
            ExpressionToken.Operator("("),
            ExpressionToken.Number(0, 1),
            ExpressionToken.Operator("+"),
            ExpressionToken.Number(1, 2),
            ExpressionToken.Operator(")"),
            ExpressionToken.Operator("*"),
            ExpressionToken.Number(2, 3)
        )
        assertEvaluatesTo(9.0, tokens)
    }

    @Test
    fun handlesDivision() {
        val tokens = listOf(
            ExpressionToken.Number(0, 8),
            ExpressionToken.Operator("/"),
            ExpressionToken.Number(1, 2)
        )
        assertEvaluatesTo(4.0, tokens)
    }

    @Test
    fun returnsNullForDivisionByZero() {
        val tokens = listOf(
            ExpressionToken.Number(0, 8),
            ExpressionToken.Operator("/"),
            ExpressionToken.Number(1, 0)
        )
        assertNull(ExpressionEvaluator.evaluate(tokens))
    }

    @Test
    fun returnsNullForIncompleteExpression() {
        val tokens = listOf(
            ExpressionToken.Number(0, 8),
            ExpressionToken.Operator("+")
        )
        assertNull(ExpressionEvaluator.evaluate(tokens))
    }

    @Test
    fun returnsNullForEmptyExpression() {
        assertNull(ExpressionEvaluator.evaluate(emptyList()))
    }
}

class TwentyFourSolverTest {

    private fun assertEvaluatesTo24(expression: String) {
        val tokens = tokenize(expression)
        val result = ExpressionEvaluator.evaluate(tokens)
        assertNotNull(result, "expression should evaluate: $expression")
        assertTrue(abs(result - 24.0) < 1e-6, "expression should equal 24, was $result: $expression")
    }

    @Test
    fun findsSolutionForClassicPuzzle() {
        val solution = TwentyFourSolver.solve(listOf(1, 2, 3, 4))
        assertNotNull(solution)
        assertEvaluatesTo24(solution)
    }

    @Test
    fun isNullForUnsolvablePuzzle() {
        val solution = TwentyFourSolver.solve(listOf(1, 1, 1, 1))
        assertNull(solution)
    }

    @Test
    fun solveIsNullForWrongNumberCount() {
        assertNull(TwentyFourSolver.solve(listOf(1, 2, 3)))
        assertNull(TwentyFourSolver.solve(listOf(1, 2, 3, 4, 5)))
    }

    @Test
    fun findAllSolutionsReturnsOnlyValidExpressions() {
        val solutions = TwentyFourSolver.findAllSolutions(listOf(1, 2, 3, 4))
        assertTrue(solutions.isNotEmpty())
        solutions.forEach(::assertEvaluatesTo24)
    }

    @Test
    fun findAllSolutionsIsEmptyForUnsolvable() {
        assertEquals(emptySet(), TwentyFourSolver.findAllSolutions(listOf(1, 1, 1, 1)))
    }

    @Test
    fun getSmartHintDataIsConsistentWithSolution() {
        val numbers = listOf(1, 2, 3, 4)
        val hint = TwentyFourSolver.getSmartHintData(numbers)
        assertNotNull(hint)
        val (first, second, operator) = hint
        assertTrue(first in numbers, "hint number must be in puzzle: $first")
        assertTrue(second in numbers, "hint number must be in puzzle: $second")
        assertTrue(operator in setOf("+", "-", "*", "/"))
    }

    @Test
    fun getSmartHintDataIsNullForUnsolvable() {
        assertNull(TwentyFourSolver.getSmartHintData(listOf(1, 1, 1, 1)))
    }

    @Test
    fun generateSolvablePuzzleIsAlwaysSolvableUnderLimit() {
        val random = kotlin.random.Random(42)
        repeat(50) {
            val puzzle = TwentyFourSolver.generateSolvablePuzzle(maxNumber = 13, random = random)
            assertNotNull(TwentyFourSolver.solve(puzzle), "puzzle should be solvable: $puzzle")
        }
    }

    @Test
    fun generateSolvablePuzzleHoldsForHighMaxNumber() {
        val random = kotlin.random.Random(7)
        repeat(30) {
            val puzzle = TwentyFourSolver.generateSolvablePuzzle(maxNumber = 999, random = random)
            assertNotNull(TwentyFourSolver.solve(puzzle), "puzzle should be solvable: $puzzle")
            puzzle.forEach { assertTrue(it in 1..999) }
        }
    }

    private fun tokenize(expression: String): List<ExpressionToken> =
        parseNumericExpression(expression)

    private fun parseNumericExpression(expression: String): List<ExpressionToken> {
        val tokens = mutableListOf<ExpressionToken>()
        var buffer = StringBuilder()
        expression.forEach { char ->
            when {
                char.isDigit() -> buffer.append(char)
                char == ' ' -> Unit
                else -> {
                    if (buffer.isNotEmpty()) {
                        tokens.add(ExpressionToken.Number(tokens.size, buffer.toString().toInt()))
                        buffer = StringBuilder()
                    }
                    tokens.add(ExpressionToken.Operator(char.toString()))
                }
            }
        }
        if (buffer.isNotEmpty()) {
            tokens.add(ExpressionToken.Number(tokens.size, buffer.toString().toInt()))
        }
        return tokens
    }
}