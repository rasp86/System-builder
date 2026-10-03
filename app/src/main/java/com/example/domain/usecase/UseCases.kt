package com.example.domain.usecase

import com.example.data.ai.AiMentorResult
import com.example.data.ai.AiMentorService
import com.example.data.game.OSBootStatus
import com.example.data.game.OsSimulationEngine
import com.example.data.game.Quest
import com.example.data.game.QuestsData
import com.example.data.game.TerminalEngine
import com.example.data.game.TerminalLine
import com.example.data.game.quest.QuestVerificationEngine
import com.example.data.game.vm.VirtualMachineEngine
import com.example.domain.model.GameResult
import com.example.domain.repository.GameDomainRepository
import com.example.ui.viewmodel.TestResultItem

/**
 * UseCase to validate and run acceptance test suite for a kernel module.
 */
class RunQuestTestsUseCase(
    private val questVerificationEngine: QuestVerificationEngine = QuestVerificationEngine()
) {
    fun execute(quest: Quest, userCode: String): Pair<List<TestResultItem>, Boolean> {
        return questVerificationEngine.runTests(quest, userCode)
    }
}

/**
 * UseCase to complete a quest, grant XP/bits rewards and unlock next kernel milestones.
 */
class CompleteQuestUseCase(
    private val repository: GameDomainRepository
) {
    suspend fun execute(questId: String, stars: Int = 3): GameResult<Pair<Int, Int>> {
        return try {
            val rewards = repository.completeQuest(questId, stars)
            GameResult.Success(rewards)
        } catch (e: Exception) {
            GameResult.Error(e, "Błąd podczas zaliczania zadania: ${e.message}")
        }
    }
}

/**
 * UseCase to save user kernel source code in VFS and database.
 */
class SaveUserCodeUseCase(
    private val repository: GameDomainRepository
) {
    suspend fun execute(questId: String, code: String, filePath: String): GameResult<Unit> {
        return try {
            repository.saveUserCode(questId, code, filePath)
            GameResult.Success(Unit)
        } catch (e: Exception) {
            GameResult.Error(e, "Nie udało się zapisać kodu: ${e.message}")
        }
    }
}

/**
 * UseCase to execute developer host terminal commands.
 */
class ExecuteTerminalCommandUseCase(
    private val terminalEngine: TerminalEngine
) {
    fun execute(
        commandStr: String,
        currentQuest: Quest?,
        userCode: String,
        completedQuestIds: Set<String>,
        onSelectQuest: (String) -> Unit,
        onRunTests: () -> Unit,
        onBootVm: () -> Unit,
        onAskAi: (String) -> Unit
    ): List<TerminalLine> {
        return terminalEngine.executeHostCommand(
            commandStr = commandStr,
            currentQuest = currentQuest,
            userCode = userCode,
            completedQuestIds = completedQuestIds,
            onSelectQuest = onSelectQuest,
            onRunTests = onRunTests,
            onBootVm = onBootVm,
            onAskAi = onAskAi
        )
    }
}

/**
 * UseCase to consult Ada (Senior Kernel Architect Mentor).
 */
class AskAiMentorUseCase(
    private val repository: GameDomainRepository
) {
    suspend fun execute(prompt: String, currentQuest: Quest?, code: String): GameResult<AiMentorResult> {
        return try {
            val result = repository.askAi(prompt, currentQuest, code)
            GameResult.Success(result)
        } catch (e: Exception) {
            GameResult.Error(e, "Błąd mentorki AI: ${e.message}")
        }
    }
}

/**
 * UseCase to simulate virtual machine BIOS/kernel boot sequence.
 */
class BootVirtualMachineUseCase(
    private val vmEngine: VirtualMachineEngine = VirtualMachineEngine()
) {
    fun generateBootSequence(osName: String, completedQuestIds: Set<String>): List<String> {
        return OsSimulationEngine.generateBootSequence(osName, completedQuestIds)
    }

    fun executeGuestCommand(command: String, osName: String, completedQuestIds: Set<String>): List<String> {
        return OsSimulationEngine.executeGuestOsCommand(command, osName, completedQuestIds)
    }
}
