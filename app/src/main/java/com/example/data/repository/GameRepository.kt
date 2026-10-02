package com.example.data.repository

import com.example.data.ai.AiMentorResult
import com.example.data.ai.GeminiRepository
import com.example.data.db.AiMessageEntity
import com.example.data.db.AppDatabase
import com.example.data.db.GameSaveEntity
import com.example.data.db.QuestProgressEntity
import com.example.data.db.VirtualFileEntity
import com.example.data.game.OSBootStatus
import com.example.data.game.OSProcess
import com.example.data.game.Quest
import com.example.data.game.QuestsData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class GameRepository(
    private val database: AppDatabase,
    private val geminiRepository: GeminiRepository = GeminiRepository()
) {
    private val gameDao = database.gameDao()

    val gameSave: Flow<GameSaveEntity?> = gameDao.getGameSave()
    val allQuests: Flow<List<QuestProgressEntity>> = gameDao.getAllQuests()
    val allFiles: Flow<List<VirtualFileEntity>> = gameDao.getAllFiles()
    val aiMessages: Flow<List<AiMessageEntity>> = gameDao.getAllAiMessages()

    suspend fun initializeGameIfNeeded() {
        var save = gameDao.getGameSaveSync()
        if (save == null) {
            save = GameSaveEntity(
                id = 1,
                osName = "GenesisOS",
                playerNick = "KernelArchitect",
                level = 1,
                xp = 0,
                bits = 100,
                currentPhase = 1
            )
            gameDao.insertGameSave(save)
        }

        // Initialize Quests progress entities if empty
        val initialQuests = QuestsData.allQuests.mapIndexed { index, quest ->
            QuestProgressEntity(
                questId = quest.id,
                phase = quest.phase,
                isUnlocked = (index == 0), // First quest unlocked by default
                isCompleted = false,
                userCode = quest.defaultCode,
                stars = 0
            )
        }
        gameDao.insertQuestsIfNotExist(initialQuests)

        // Initialize Virtual Files if empty
        val initialFiles = QuestsData.allQuests.map { quest ->
            VirtualFileEntity(
                path = quest.filePath,
                fileName = quest.filePath.substringAfterLast('/'),
                content = quest.defaultCode,
                language = when {
                    quest.filePath.endsWith(".asm") -> "asm"
                    quest.filePath.endsWith(".c") -> "c"
                    quest.filePath.endsWith(".h") -> "h"
                    else -> "txt"
                },
                isKernelCore = true
            )
        }
        gameDao.insertFilesIfNotExist(initialFiles)
    }

    suspend fun getQuestById(questId: String): QuestProgressEntity? {
        return gameDao.getQuestById(questId)
    }

    suspend fun saveUserCode(questId: String, code: String, filePath: String) {
        val currentQuest = gameDao.getQuestById(questId)
        if (currentQuest != null) {
            gameDao.insertOrUpdateQuest(currentQuest.copy(userCode = code))
        }
        gameDao.insertOrUpdateFile(
            VirtualFileEntity(
                path = filePath,
                fileName = filePath.substringAfterLast('/'),
                content = code,
                language = when {
                    filePath.endsWith(".asm") -> "asm"
                    filePath.endsWith(".c") -> "c"
                    filePath.endsWith(".h") -> "h"
                    else -> "txt"
                },
                isKernelCore = true,
                lastModified = System.currentTimeMillis()
            )
        )
    }

    suspend fun completeQuest(questId: String, stars: Int = 3): Pair<Int, Int> {
        val quest = QuestsData.allQuests.find { it.id == questId } ?: return (0 to 0)
        val questProgress = gameDao.getQuestById(questId) ?: return (0 to 0)

        if (!questProgress.isCompleted) {
            gameDao.insertOrUpdateQuest(
                questProgress.copy(
                    isCompleted = true,
                    stars = stars,
                    completedAt = System.currentTimeMillis()
                )
            )

            // Unlock next quest in sequence
            val questList = QuestsData.allQuests
            val currentIndex = questList.indexOfFirst { it.id == questId }
            if (currentIndex in 0 until questList.size - 1) {
                val nextQuest = questList[currentIndex + 1]
                val nextProgress = gameDao.getQuestById(nextQuest.id)
                if (nextProgress != null && !nextProgress.isUnlocked) {
                    gameDao.insertOrUpdateQuest(nextProgress.copy(isUnlocked = true))
                }
            }

            // Award XP, Bits and level up check
            val currentSave = gameDao.getGameSaveSync() ?: GameSaveEntity()
            val newXp = currentSave.xp + quest.xpReward
            val newBits = currentSave.bits + quest.bitsReward
            val newLevel = 1 + (newXp / 300)
            val newPhase = maxOf(currentSave.currentPhase, quest.phase + (if (currentIndex == questList.size - 1) 0 else 1))
            val newBugs = currentSave.bugsFixed + 1
            val newLines = currentSave.totalLinesWritten + quest.defaultCode.lines().size

            gameDao.updateGameSave(
                currentSave.copy(
                    xp = newXp,
                    bits = newBits,
                    level = newLevel,
                    currentPhase = newPhase,
                    bugsFixed = newBugs,
                    totalLinesWritten = newLines
                )
            )

            return quest.xpReward to quest.bitsReward
        }
        return (0 to 0)
    }

    suspend fun toggleCrtScanlines() {
        val save = gameDao.getGameSaveSync() ?: return
        gameDao.updateGameSave(save.copy(crtScanlinesEnabled = !save.crtScanlinesEnabled))
    }

    suspend fun incrementBootCount() {
        val save = gameDao.getGameSaveSync() ?: return
        gameDao.updateGameSave(save.copy(bootCount = save.bootCount + 1))
    }

    suspend fun askAi(prompt: String, currentQuest: Quest?, code: String): AiMentorResult {
        // Save user message in history
        gameDao.insertAiMessage(
            AiMessageEntity(
                role = "user",
                prompt = prompt,
                content = prompt,
                questId = currentQuest?.id ?: ""
            )
        )

        val result = geminiRepository.askKernelArchitectMentor(
            userPrompt = prompt,
            currentQuestContext = currentQuest?.let { "${it.title} (${it.category})" } ?: "General OS Architecture",
            codeContext = code
        )

        // Save AI reply in history
        gameDao.insertAiMessage(
            AiMessageEntity(
                role = "model",
                prompt = prompt,
                content = result.replyText,
                reasoning = result.reasoningSteps.joinToString("\n• "),
                questId = currentQuest?.id ?: ""
            )
        )

        return result
    }

    suspend fun clearAiChat() {
        gameDao.clearAiHistory()
    }
}
