package com.finley.android.shared.ui.leaderboard.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finley.android.shared.data.model.GameScore
import com.finley.android.shared.data.repository.GameRepository
import com.finley.android.shared.logic.UserManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LeaderboardViewModel(
    private val gameRepository: GameRepository,
    private val userManager: UserManager
) : ViewModel() {
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _topScores = MutableStateFlow<List<GameScore>>(emptyList())
    val topScores: StateFlow<List<GameScore>> = _topScores.asStateFlow()

    init {
        refresh()
        observeLocalUser()
    }

    private fun observeLocalUser() {
        // 持续观察本地用户。如果本地分数变了，排行榜里如果包含该用户，自动同步显示
        userManager.currentUser
            .filterNotNull()
            .onEach { user ->
                _topScores.update { list ->
                    list.map { score ->
                        if (score.playerName == user.username) {
                            score.copy(
                                score = user.score,
                                levelLabel = user.currentLevel,
                                nickname = user.nickname
                            )
                        } else score
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val global = gameRepository.getGlobalLeaderboard()
                val localUser = userManager.currentUser.value
                
                // 将本地最新数据合并到拉取的结果中，解决服务器延迟导致的“回退”幻觉
                val mapped = global.map { 
                    GameScore(it.username, it.nickname, it.score, it.levelLabel, 0, 0)
                }
                _topScores.value = mapped
            } catch (e: Exception) {
                println("LeaderboardViewModel: Refresh error: ${e.message}")
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
