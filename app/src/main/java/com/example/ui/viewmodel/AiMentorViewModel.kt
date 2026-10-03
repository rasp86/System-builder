package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiMentorResult
import com.example.data.ai.AiMentorService
import com.example.data.db.AiMessageEntity
import com.example.data.game.Quest
import com.example.data.repository.GameRepository
import com.example.domain.model.GameResult
import com.example.domain.usecase.AskAiMentorUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Feature ViewModel managing AI mentor (Ada) chat history and real-time reasoning.
 */
class AiMentorViewModel(
    private val askAiUseCase: AskAiMentorUseCase,
    private val repository: GameRepository
) : ViewModel() {

    val aiMessages: StateFlow<List<AiMessageEntity>> = repository.aiMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _lastResult = MutableStateFlow<AiMentorResult?>(null)
    val lastResult: StateFlow<AiMentorResult?> = _lastResult.asStateFlow()

    fun askMentor(
        prompt: String,
        currentQuest: Quest?,
        userCode: String,
        onReplyReceived: (AiMentorResult) -> Unit = {}
    ) {
        if (prompt.isBlank()) return

        viewModelScope.launch {
            _isAiThinking.value = true
            val result = askAiUseCase.execute(prompt, currentQuest, userCode)
            _isAiThinking.value = false

            when (result) {
                is GameResult.Success -> {
                    _lastResult.value = result.data
                    onReplyReceived(result.data)
                }
                is GameResult.Error -> {
                    val fallback = AiMentorResult(
                        replyText = "Nie udało się połączyć z mentorką Adą: ${result.message}",
                        reasoningSteps = listOf("Błąd komunikacji z modelem AI.")
                    )
                    _lastResult.value = fallback
                    onReplyReceived(fallback)
                }
                is GameResult.Loading -> {}
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearAiChat()
        }
    }
}
