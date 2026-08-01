package com.finley.android.shared.logic

import kotlin.math.abs

object TwentyFourSolver {
    private const val TARGET = 24.0
    private const val EPSILON = 1e-6

    fun solve(numbers: List<Int>): String? {
        val solutions = findAllSolutions(numbers)
        return solutions.firstOrNull()
    }

    fun findAllSolutions(numbers: List<Int>): Set<String> {
        if (numbers.size != 4) return emptySet()
        val solutions = mutableSetOf<String>()
        backtrackAll(numbers.map { it.toDouble() }, numbers.map { it.toString() }, solutions)
        return solutions
    }

    private fun backtrackAll(nums: List<Double>, exprs: List<String>, solutions: MutableSet<String>) {
        if (nums.size == 1) {
            if (abs(nums[0] - TARGET) < EPSILON) {
                solutions.add(exprs[0])
            }
            return
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

                val results = getPossibleResults(nums[i], nums[j], exprs[i], exprs[j])
                for (res in results) {
                    backtrackAll(nextNums + res.first, nextExprs + res.second, solutions)
                }
            }
        }
    }

    /**
     * Returns a Triple containing the two numbers and the operator as a hint.
     * The actual string formatting is left to the caller (ViewModel/UI) for localization.
     */
    fun getSmartHintData(numbers: List<Int>): Triple<Int, Int, String>? {
        if (numbers.size != 4) return null
        
        val nums = numbers.map { it.toDouble() }
        val exprs = numbers.map { it.toString() }
        
        for (i in nums.indices) {
            for (j in nums.indices) {
                if (i == j) continue
                
                val results = getPossibleResults(nums[i], nums[j], exprs[i], exprs[j])
                for (res in results) {
                    val remainingNums = mutableListOf<Double>()
                    val remainingExprs = mutableListOf<String>()
                    for (k in nums.indices) {
                        if (k != i && k != j) {
                            remainingNums.add(nums[k])
                            remainingExprs.add(exprs[k])
                        }
                    }
                    
                    if (backtrack(remainingNums + res.first, remainingExprs + res.second) != null) {
                        val op = res.second.substring(res.second.indexOfAny(charArrayOf('+', '-', '*', '/')), res.second.indexOfAny(charArrayOf('+', '-', '*', '/')) + 1)
                        return Triple(nums[i].toInt(), nums[j].toInt(), op)
                    }
                }
            }
        }
        return null
    }

    @Deprecated("Use getSmartHintData instead for localization", ReplaceWith("getSmartHintData(numbers)"))
    fun getSmartHint(numbers: List<Int>, opNames: Map<String, String>): String? {
        val data = getSmartHintData(numbers) ?: return null
        return "尝试先将 ${data.first} 和 ${data.second} 进行 ${opNames[data.third] ?: data.third}"
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
                val results = getPossibleResults(nums[i], nums[j], exprs[i], exprs[j])
                for (res in results) {
                    val solution = backtrack(nextNums + res.first, nextExprs + res.second)
                    if (solution != null) return solution
                }
            }
        }
        return null
    }

    private fun getPossibleResults(a: Double, b: Double, exprA: String, exprB: String): List<Pair<Double, String>> {
        val list = mutableListOf<Pair<Double, String>>()
        list.add(a + b to "($exprA+$exprB)")
        list.add(a - b to "($exprA-$exprB)")
        list.add(a * b to "($exprA*$exprB)")
        if (abs(b) > EPSILON) {
            list.add(a / b to "($exprA/$exprB)")
        }
        return list
    }

    fun generateSolvablePuzzle(maxNumber: Int = 13): List<Int> {
        while (true) {
            val puzzle = List(4) { (1..maxNumber).random() }
            if (solve(puzzle) != null) return puzzle
        }
    }
}
