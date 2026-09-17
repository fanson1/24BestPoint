package com.finley.android.shared.logic

import kotlin.math.abs
import kotlin.random.Random

object TwentyFourSolver {
    private const val TARGET = 24.0
    private const val EPSILON = 1e-6

    /** Numbers must be solvable to keep high-difficulty levels playable; cap the search. */
    private const val MAX_GENERATION_ATTEMPTS = 500
    private const val SMALL_POOL_MAX = 13

    private data class Candidate(
        val value: Double,
        val expression: String,
        val operator: String
    )

    fun solve(numbers: List<Int>): String? {
        if (numbers.size != 4) return null
        val solutions = mutableSetOf<String>()
        backtrackAll(
            nums = numbers.map { it.toDouble() },
            exprs = numbers.map { it.toString() },
            solutions = solutions,
            stopWhenFound = true
        )
        return solutions.firstOrNull()
    }

    fun findAllSolutions(numbers: List<Int>): Set<String> {
        if (numbers.size != 4) return emptySet()
        val solutions = mutableSetOf<String>()
        backtrackAll(
            nums = numbers.map { it.toDouble() },
            exprs = numbers.map { it.toString() },
            solutions = solutions,
            stopWhenFound = false
        )
        return solutions
    }

    private fun backtrackAll(
        nums: List<Double>,
        exprs: List<String>,
        solutions: MutableSet<String>,
        stopWhenFound: Boolean
    ) {
        if (nums.size == 1) {
            if (abs(nums[0] - TARGET) < EPSILON) {
                solutions.add(exprs[0])
            }
            return
        }

        val remainingNums = ArrayList<Double>(nums.size - 1)
        val remainingExprs = ArrayList<String>(nums.size - 1)

        for (i in nums.indices) {
            for (j in nums.indices) {
                if (i == j) continue

                for (k in nums.indices) {
                    if (k != i && k != j) {
                        remainingNums.add(nums[k])
                        remainingExprs.add(exprs[k])
                    }
                }

                for (candidate in getPossibleCandidates(nums[i], nums[j], exprs[i], exprs[j])) {
                    remainingNums.add(candidate.value)
                    remainingExprs.add(candidate.expression)
                    backtrackAll(remainingNums, remainingExprs, solutions, stopWhenFound)
                    if (stopWhenFound && solutions.isNotEmpty()) return
                    remainingNums.removeAt(remainingNums.lastIndex)
                    remainingExprs.removeAt(remainingExprs.lastIndex)
                }

                remainingNums.clear()
                remainingExprs.clear()
            }
        }
    }

    /**
     * Returns a hint describing the first operation that leads to a solution:
     * the two numbers to combine first and the operator to use.
     * The actual string formatting is left to the caller (ViewModel/UI) for localization.
     */
    fun getSmartHintData(numbers: List<Int>): Triple<Int, Int, String>? {
        if (numbers.size != 4) return null

        val nums = numbers.map { it.toDouble() }
        val exprs = numbers.map { it.toString() }

        for (i in nums.indices) {
            for (j in nums.indices) {
                if (i == j) continue

                val remainingNums = mutableListOf<Double>()
                val remainingExprs = mutableListOf<String>()
                for (k in nums.indices) {
                    if (k != i && k != j) {
                        remainingNums.add(nums[k])
                        remainingExprs.add(exprs[k])
                    }
                }

                for (candidate in getPossibleCandidates(nums[i], nums[j], exprs[i], exprs[j])) {
                    remainingNums.add(candidate.value)
                    remainingExprs.add(candidate.expression)
                    if (backtrack(remainingNums, remainingExprs) != null) {
                        return Triple(nums[i].toInt(), nums[j].toInt(), candidate.operator)
                    }
                    remainingNums.removeAt(remainingNums.lastIndex)
                    remainingExprs.removeAt(remainingExprs.lastIndex)
                }
            }
        }
        return null
    }

    private fun backtrack(nums: List<Double>, exprs: List<String>): String? {
        if (nums.size == 1) {
            return if (abs(nums[0] - TARGET) < EPSILON) exprs[0] else null
        }

        for (i in nums.indices) {
            for (j in nums.indices) {
                if (i == j) continue
                val nextNums = mutableListOf<Double>()
                val nextExprs = mutableListOf<String>()
                for (k in nums.indices) {
                    if (k != i && k != j) {
                        nextNums.add(nums[k])
                        nextExprs.add(exprs[k])
                    }
                }
                for (candidate in getPossibleCandidates(nums[i], nums[j], exprs[i], exprs[j])) {
                    nextNums.add(candidate.value)
                    nextExprs.add(candidate.expression)
                    val solution = backtrack(nextNums, nextExprs)
                    if (solution != null) return solution
                    nextNums.removeAt(nextNums.lastIndex)
                    nextExprs.removeAt(nextExprs.lastIndex)
                }
            }
        }
        return null
    }

    private fun getPossibleCandidates(a: Double, b: Double, exprA: String, exprB: String): List<Candidate> {
        val list = mutableListOf<Candidate>()
        list.add(Candidate(a + b, "($exprA+$exprB)", "+"))
        list.add(Candidate(a - b, "($exprA-$exprB)", "-"))
        list.add(Candidate(a * b, "($exprA*$exprB)", "*"))
        if (abs(b) > EPSILON) {
            list.add(Candidate(a / b, "($exprA/$exprB)", "/"))
        }
        return list
    }

    /**
     * Generates a random, solvable 4-card puzzle whose numbers are within [1, maxNumber].
     *
     * With high [maxNumber] values, four large random numbers are rarely able to make 24.
     * To keep every difficulty playable (and avoid a near-infinite retry loop), the generator
     * mixes two small numbers (1..13) with two numbers from the full range. A small guaranteed
     * fallback puzzle is returned if the randomized search exceeds the attempt budget.
     */
    fun generateSolvablePuzzle(maxNumber: Int = 13, random: Random = Random.Default): List<Int> {
        repeat(MAX_GENERATION_ATTEMPTS) {
            val numbers = if (maxNumber <= SMALL_POOL_MAX) {
                List(4) { random.nextInt(1, maxNumber + 1) }
            } else {
                buildList {
                    repeat(2) { add(random.nextInt(1, SMALL_POOL_MAX + 1)) }
                    repeat(2) { add(random.nextInt(1, maxNumber + 1)) }
                }
            }
            if (solve(numbers) != null) return numbers
        }
        return listOf(1, 2, 3, 4)
    }
}