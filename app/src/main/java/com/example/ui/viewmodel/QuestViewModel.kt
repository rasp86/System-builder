package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.QuestProgressEntity
import com.example.data.game.Quest
import com.example.data.repository.GameRepository
import com.example.domain.model.GameResult
import com.example.domain.provider.QuestProvider
import com.example.domain.usecase.CompleteQuestUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Feature ViewModel managing quest progression, active quest selection, and rewards.
 */
class QuestViewModel(
    private val completeQuestUseCase: CompleteQuestUseCase,
    private val questProvider: QuestProvider,
    private val repository: GameRepository
) : ViewModel() {

    val allQuests: List<Quest> = questProvider.getAllQuests()

    val allQuestsProgress: StateFlow<List<QuestProgressEntity>> = repository.allQuests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeQuestId = MutableStateFlow(questProvider.getInitialQuest().id)
    val activeQuestId: StateFlow<String> = _activeQuestId.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    fun selectQuest(questId: String) {
        val quest = questProvider.getQuestById(questId) ?: return
        _activeQuestId.value = quest.id
    }

    fun getActiveQuest(): Quest? {
        return questProvider.getQuestById(_activeQuestId.value)
    }

    suspend fun completeQuest(questId: String, stars: Int = 3): GameResult<Pair<Int, Int>> {
        val result = completeQuestUseCase.execute(questId, stars)
        if (result is GameResult.Success) {
            val (xp, bits) = result.data
            _toastEvent.emit("Zadanie zaliczone! +$xp XP | +$bits Bits")
        }
        return result
    }

    fun getCompletedQuestIds(): Set<String> {
        return allQuestsProgress.value.filter { it.isCompleted }.map { it.questId }.toSet()
    }
}
