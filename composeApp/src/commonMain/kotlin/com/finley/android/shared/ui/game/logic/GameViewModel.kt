package com.finley.android.shared.ui.game.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finley.android.shared.data.model.User
import com.finley.android.shared.domain.GameDifficulty
import com.finley.android.shared.logic.ExpressionEvaluator
import com.finley.android.shared.logic.TwentyFourSolver
import com.finley.android.shared.logic.UserManager
import com.finley.android.shared.ui.game.mvi.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.abs

import com.finley.android.shared.data.repository.GameRepository
import com.finley.android.shared.*
import org.jetbrains.compose.resources.getString

import kotlin.time.Clock

class GameViewModel(
    private val userManager: UserManager,
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _effect = Channel<GameEffect>()
    val effect = _effect.receiveAsFlow()

    private var timerJob: Job? = null

    init {
        observeUserChanges()
        handleIntent(GameIntent.LoadNewPuzzle)
    }

    private fun observeUserChanges() {
        viewModelScope.launch {
            userManager.currentUser.collect { user ->
                if (user != null) {
                    _state.update {
                        it.copy(
                            username = user.username,
                            nickname = user.nickname,
                            score = user.score,
                            difficulty = try { GameDifficulty.valueOf(user.currentLevel) } catch(_: Exception) { GameDifficulty.PRIMARY },
                            correctCountInLevel = user.correctCountInLevel,
                            hintCredits = user.hintCredits,
                            isLevelLockActive = user.isLevelLockActive,
                            lockAdsWatched = user.lockAdsWatched
                        )
                    }
                }
            }
        }
    }

    fun handleIntent(intent: GameIntent) {
        when (intent) {
            is GameIntent.LoadNewPuzzle -> loadNewPuzzle()
            is GameIntent.SelectNumber -> selectNumber(intent.index, intent.value)
            is GameIntent.SelectOperator -> selectOperator(intent.operator)
            is GameIntent.Undo -> undo()
            is GameIntent.Reset -> reset()
            is GameIntent.Submit -> submit()
            is GameIntent.RequestHint -> _state.update { it.copy(showAdDialog = true) }
            is GameIntent.UseHint -> useHintFromIntent()
            is GameIntent.WatchAd -> _effect.trySend(GameEffect.NavigateToAd)
            is GameIntent.WatchLockAd -> _effect.trySend(GameEffect.NavigateToAd)
            is GameIntent.DismissAdDialog -> _state.update { it.copy(showAdDialog = false) }
            is GameIntent.Logout -> logout()
            is GameIntent.ToggleFeverMode -> toggleFeverMode()
        }
    }

    private fun toggleFeverMode() {
        val nextMode = !_state.value.isFeverMode
        _state.update { it.copy(isFeverMode = nextMode) }
        if (nextMode) startFeverTimer() else timerJob?.cancel()
        loadNewPuzzle()
    }

    private fun startFeverTimer() {
        timerJob?.cancel()
        _state.update { it.copy(timeLeft = 30, comboCount = 0) }
        timerJob = viewModelScope.launch {
            while (_state.value.timeLeft > 0) {
                delay(1000)
                _state.update { it.copy(timeLeft = it.timeLeft - 1) }
            }
            _effect.send(GameEffect.FeverGameOver)
            _state.update { it.copy(isFeverMode = false) }
            loadNewPuzzle()
        }
    }

    private fun logout() {
        timerJob?.cancel()
        userManager.logout()
        viewModelScope.launch { _effect.send(GameEffect.NavigateToAuth) }
    }

    private fun loadNewPuzzle() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val currentDifficulty = _state.value.difficulty
            val puzzle = TwentyFourSolver.generateSolvablePuzzle(currentDifficulty.maxNumber)
            _state.update { 
                it.copy(
                    numbers = puzzle,
                    selectedIndices = emptySet(),
                    currentExpression = "",
                    expressionHistory = emptyList(),
                    isLoading = false,
                    isSuccess = false,
                    hintContent = null,
                    hintLevel = 0,
                    currentPuzzleStartTime = Clock.System.now().toEpochMilliseconds()
                )
            }
        }
    }

    private fun selectNumber(index: Int, value: Int) {
        if (_state.value.selectedIndices.contains(index)) return
        val text = value.toString()
        _state.update {
            it.copy(
                selectedIndices = it.selectedIndices + index,
                currentExpression = it.currentExpression + text,
                expressionHistory = it.expressionHistory + ExpressionMove(text, index)
            )
        }
    }

    private fun selectOperator(operator: String) {
        _state.update {
            it.copy(
                currentExpression = it.currentExpression + operator,
                expressionHistory = it.expressionHistory + ExpressionMove(operator, null)
            )
        }
    }

    private fun undo() {
        _state.update { state ->
            if (state.expressionHistory.isEmpty()) return@update state
            val lastMove = state.expressionHistory.last()
            val newHistory = state.expressionHistory.dropLast(1)
            val newExpression = state.currentExpression.removeSuffix(lastMove.text)
            val newIndices = if (lastMove.numberIndex != null) state.selectedIndices - lastMove.numberIndex else state.selectedIndices
            state.copy(currentExpression = newExpression, expressionHistory = newHistory, selectedIndices = newIndices)
        }
    }

    private fun reset() {
        _state.update { it.copy(selectedIndices = emptySet(), currentExpression = "", expressionHistory = emptyList()) }
    }

    private fun useHintFromIntent() {
        if (_state.value.hintCredits > 0) {
            _state.update { it.copy(showAdDialog = false) }
            useHint()
        }
    }

    fun onAdCompleted() {
        val user = userManager.currentUser.value ?: return
        if (_state.value.isLevelLockActive) {
            val newWatchedCount = _state.value.lockAdsWatched + 1
            if (newWatchedCount >= 2) {
                val updatedUser = user.copy(isLevelLockActive = false, lockAdsWatched = 0)
                updateUserFully(updatedUser)
                loadNewPuzzle()
                viewModelScope.launch { _effect.send(GameEffect.ShowError(getString(Res.string.game_lock_unlocked_msg))) }
            } else {
                val updatedUser = user.copy(lockAdsWatched = newWatchedCount)
                updateUserFully(updatedUser)
                viewModelScope.launch { _effect.send(GameEffect.ShowError(getString(Res.string.game_hint_ad_progress, newWatchedCount))) }
            }
        } else {
            val newCredits = (user.hintCredits + 1).coerceAtMost(5)
            val updatedUser = user.copy(hintCredits = newCredits)
            updateUserFully(updatedUser)
            viewModelScope.launch { _effect.send(GameEffect.ShowError(getString(Res.string.game_hint_ad_reward))) }
        }
    }

    private fun updateUserFully(user: User) {
        userManager.updateCurrentUser(user)
        viewModelScope.launch { gameRepository.updateUser(user) }
    }

    private fun useHint() {
        val numbers = _state.value.numbers
        val hintData = TwentyFourSolver.getSmartHintData(numbers)
        val fullSolution = TwentyFourSolver.solve(numbers)
        if (hintData != null && fullSolution != null) {
            val nextHintLevel = _state.value.hintLevel + 1
            viewModelScope.launch {
                val content = when(nextHintLevel) {
                    1 -> {
                        val opResource = when(hintData.third) {
                            "+" -> Res.string.op_add
                            "-" -> Res.string.op_sub
                            "*" -> Res.string.op_mul
                            "/" -> Res.string.op_div
                            else -> null
                        }
                        val opLabel = if (opResource != null) getString(opResource) else hintData.third
                        getString(Res.string.game_hint_smart, hintData.first, hintData.second, opLabel)
                    }
                    else -> {
                        val hintLength = (nextHintLevel - 1) * 5
                        if (fullSolution.length > hintLength) {
                            getString(Res.string.game_hint_expression, fullSolution.take(hintLength))
                        } else getString(Res.string.game_hint_full_solution, fullSolution)
                    }
                }
                _state.update { it.copy(hintContent = content, hintLevel = nextHintLevel) }
                val user = userManager.currentUser.value ?: return@launch
                updateUserFully(user.copy(hintCredits = user.hintCredits - 1))
            }
        }
    }

    private fun submit() {
        val expression = _state.value.currentExpression
        if (expression.isEmpty()) return
        if (_state.value.selectedIndices.size != 4) {
            viewModelScope.launch { _effect.send(GameEffect.ShowError(getString(Res.string.game_error_all_numbers))) }
            return
        }
        val result = ExpressionEvaluator.evaluate(expression)
        if (result != null && abs(result - 24.0) < 1e-6) handleSuccess() else handleFailure()
    }

    private fun handleSuccess() {
        val user = userManager.currentUser.value ?: return
        val oldState = _state.value
        val scoreIncrement = oldState.difficulty.scorePerQuestion
        val newScore = user.score + scoreIncrement
        val newCorrectCount = user.correctCountInLevel + 1
        val newCombo = if (oldState.isFeverMode) oldState.comboCount + 1 else 0
        if (oldState.isFeverMode) _state.update { it.copy(timeLeft = (it.timeLeft + 5).coerceAtMost(60)) }
        var nextDifficultyKey = user.currentLevel
        var finalCorrectCount = newCorrectCount
        if (!oldState.isFeverMode && newCorrectCount >= 10) {
            val up = oldState.difficulty.next()
            if (up != null) {
                nextDifficultyKey = up.name
                finalCorrectCount = 0
                viewModelScope.launch {
                    _effect.send(GameEffect.LevelUp(nextDifficultyKey))
                    _state.update { it.copy(showLevelUp = nextDifficultyKey) }
                    delay(3000)
                    _state.update { it.copy(showLevelUp = null) }
                }
            }
        }
        _state.update { it.copy(isSuccess = true, answerFeedback = AnswerFeedback(type = AnswerFeedbackType.CORRECT, scoreChange = scoreIncrement, combo = newCombo), comboCount = newCombo) }
        val updatedUser = user.copy(score = newScore, currentLevel = nextDifficultyKey, correctCountInLevel = finalCorrectCount)
        updateUserFully(updatedUser)
        viewModelScope.launch {
            _effect.send(GameEffect.ShowSuccessAnim)
            delay(1500)
            _state.update { it.copy(answerFeedback = null) }
            if (!oldState.isFeverMode && finalCorrectCount == 9) {
                updateUserFully(updatedUser.copy(isLevelLockActive = true, lockAdsWatched = 0))
            } else loadNewPuzzle()
        }
    }

    private fun handleFailure() {
        if (_state.value.isFeverMode) _state.update { it.copy(timeLeft = (it.timeLeft - 5).coerceAtLeast(0)) }
        _state.update { it.copy(answerFeedback = AnswerFeedback(type = AnswerFeedbackType.INCORRECT)) }
        viewModelScope.launch {
            _effect.send(GameEffect.ShowError(getString(Res.string.game_error_wrong_result)))
            delay(1000)
            _state.update { it.copy(answerFeedback = null) }
        }
    }
}
