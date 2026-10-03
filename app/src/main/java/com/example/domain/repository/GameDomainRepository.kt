package com.example.domain.repository

import com.example.data.ai.AiMentorResult
import com.example.data.db.AiMessageEntity
import com.example.data.db.GameSaveEntity
import com.example.data.db.QuestProgressEntity
import com.example.data.db.VirtualFileEntity
import com.example.data.game.Quest
import com.example.domain.model.GameState
import com.example.domain.model.QuestProgress
import kotlinx.coroutines.flow.Flow

/**
 * Domain-level abstraction of the Game Repository.
 */
interface GameDomainRepository {
    val gameSave: Flow<GameSaveEntity?>
    val allQuests: Flow<List<QuestProgressEntity>>
    val allFiles: Flow<List<VirtualFileEntity>>
    val aiMessages: Flow<List<AiMessageEntity>>

    suspend fun initializeGameIfNeeded()
    suspend fun getQuestById(questId: String): QuestProgressEntity?
    suspend fun saveUserCode(questId: String, code: String, filePath: String)
    suspend fun completeQuest(questId: String, stars: Int = 3): Pair<Int, Int>
    suspend fun toggleCrtScanlines()
    suspend fun updateTerminalColorScheme(schemeKey: String)
    suspend fun incrementBootCount()
    suspend fun askAi(prompt: String, currentQuest: Quest?, code: String): AiMentorResult
    suspend fun clearAiChat()
}
