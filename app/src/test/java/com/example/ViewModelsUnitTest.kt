package com.example

import com.example.data.ai.AiMentorResult
import com.example.data.db.AiMessageEntity
import com.example.data.db.GameSaveEntity
import com.example.data.db.QuestProgressEntity
import com.example.data.db.VirtualFileEntity
import com.example.data.game.OSBootStatus
import com.example.data.game.Quest
import com.example.data.game.QuestsData
import com.example.data.game.TerminalEngine
import com.example.data.game.quest.QuestVerificationEngine
import com.example.data.game.vm.VirtualMachineEngine
import com.example.data.questprovider.QuestRepositoryImpl
import com.example.domain.model.GameResult
import com.example.domain.repository.GameDomainRepository
import com.example.domain.usecase.BootVirtualMachineUseCase
import com.example.domain.usecase.CompleteQuestUseCase
import com.example.domain.usecase.ExecuteTerminalCommandUseCase
import com.example.domain.usecase.RunQuestTestsUseCase
import com.example.domain.usecase.SaveUserCodeUseCase
import com.example.ui.viewmodel.EditorViewModel
import com.example.ui.viewmodel.QuestViewModel
import com.example.ui.viewmodel.TerminalViewModel
import com.example.ui.viewmodel.VirtualMachineViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelsUnitTest {

    @Test
    fun testTerminalViewModelBootSequenceAndCommands() {
        val terminalEngine = TerminalEngine()
        val useCase = ExecuteTerminalCommandUseCase(terminalEngine)
        val fakeRepo = FakeGameRepository()
        val vm = TerminalViewModel(useCase, fakeRepo.asRealRepository(), terminalEngine)

        assertNotNull(vm.terminalLines.value)
        vm.executeCommand(
            input = "uptime",
            currentQuest = null,
            userCode = "",
            completedQuestIds = emptySet()
        )
        assertTrue(vm.terminalLines.value.any { it.text.contains("uptime") || it.text.contains("up") })

        vm.clearTerminal()
        assertTrue(vm.terminalLines.value.isEmpty())
    }

    @Test
    fun testQuestViewModelSelectAndProgress() = runTest {
        val fakeRepo = FakeGameRepository()
        val questProvider = QuestRepositoryImpl()
        val completeQuestUseCase = CompleteQuestUseCase(fakeRepo)
        val vm = QuestViewModel(completeQuestUseCase, questProvider, fakeRepo.asRealRepository())

        assertEquals("Q1_1_MBR_MAGIC", vm.activeQuestId.value)
        assertEquals("Q1_1_MBR_MAGIC", vm.getActiveQuest()?.id)

        vm.selectQuest("Q1_2_GDT_PROTECTED_MODE")
        assertEquals("Q1_2_GDT_PROTECTED_MODE", vm.activeQuestId.value)
        assertEquals("Q1_2_GDT_PROTECTED_MODE", vm.getActiveQuest()?.id)

        val completeResult = vm.completeQuest("Q1_1_MBR_MAGIC", stars = 3)
        assertTrue(completeResult is GameResult.Success)
    }

    @Test
    fun testEditorViewModelRunTestsAndReset() = runTest {
        val fakeRepo = FakeGameRepository()
        val runTestsUseCase = RunQuestTestsUseCase(QuestVerificationEngine())
        val saveCodeUseCase = SaveUserCodeUseCase(fakeRepo)
        val vm = EditorViewModel(runTestsUseCase, saveCodeUseCase, fakeRepo.asRealRepository())

        val mbrQuest = QuestsData.allQuests.first { it.id == "Q1_1_MBR_MAGIC" }
        vm.loadCodeForQuest(mbrQuest)

        vm.resetToReferenceSolution(mbrQuest)
        assertEquals(mbrQuest.referenceSolution, vm.editorCode.value)

        vm.resetToDefaultCode(mbrQuest)
        assertEquals(mbrQuest.defaultCode, vm.editorCode.value)
    }

    @Test
    fun testVirtualMachineViewModelState() = runTest {
        val fakeRepo = FakeGameRepository()
        val bootUseCase = BootVirtualMachineUseCase(VirtualMachineEngine())
        val vm = VirtualMachineViewModel(bootUseCase, fakeRepo.asRealRepository())

        assertEquals(OSBootStatus.POWERED_OFF, vm.vmStatus.value)

        vm.switchToCli()
        assertEquals(OSBootStatus.RUNNING_CLI, vm.vmStatus.value)

        vm.switchToGui()
        assertEquals(OSBootStatus.RUNNING_GUI, vm.vmStatus.value)

        vm.powerOffVm()
        assertEquals(OSBootStatus.POWERED_OFF, vm.vmStatus.value)
    }
}

/**
 * In-memory test fake implementing GameDomainRepository for fast isolated unit tests.
 */
class FakeGameRepository : GameDomainRepository {
    private val _gameSave = MutableStateFlow<GameSaveEntity?>(GameSaveEntity())
    override val gameSave: Flow<GameSaveEntity?> = _gameSave.asStateFlow()

    private val _allQuests = MutableStateFlow<List<QuestProgressEntity>>(emptyList())
    override val allQuests: Flow<List<QuestProgressEntity>> = _allQuests.asStateFlow()

    private val _allFiles = MutableStateFlow<List<VirtualFileEntity>>(emptyList())
    override val allFiles: Flow<List<VirtualFileEntity>> = _allFiles.asStateFlow()

    private val _aiMessages = MutableStateFlow<List<AiMessageEntity>>(emptyList())
    override val aiMessages: Flow<List<AiMessageEntity>> = _aiMessages.asStateFlow()

    val completedQuests = mutableMapOf<String, Int>()
    val savedCodes = mutableMapOf<String, String>()

    override suspend fun initializeGameIfNeeded() {}

    override suspend fun getQuestById(questId: String): QuestProgressEntity? {
        val quest = QuestsData.allQuests.find { it.id == questId } ?: return null
        return QuestProgressEntity(
            questId = quest.id,
            phase = quest.phase,
            isUnlocked = true,
            isCompleted = completedQuests.containsKey(questId),
            userCode = savedCodes[questId] ?: quest.defaultCode,
            stars = completedQuests[questId] ?: 0
        )
    }

    override suspend fun saveUserCode(questId: String, code: String, filePath: String) {
        savedCodes[questId] = code
    }

    override suspend fun completeQuest(questId: String, stars: Int): Pair<Int, Int> {
        completedQuests[questId] = stars
        return Pair(150, 50)
    }

    override suspend fun toggleCrtScanlines() {}

    override suspend fun updateTerminalColorScheme(schemeKey: String) {}

    override suspend fun incrementBootCount() {}

    override suspend fun askAi(prompt: String, currentQuest: Quest?, code: String): AiMentorResult {
        return AiMentorResult(
            replyText = "Wskazówka od Ady: test response",
            reasoningSteps = listOf("Krok 1", "Krok 2")
        )
    }

    override suspend fun clearAiChat() {
        _aiMessages.value = emptyList()
    }

    fun asRealRepository(): com.example.data.repository.GameRepository {
        return com.example.data.repository.GameRepository(
            database = createMockAppDb(),
            aiMentorService = com.example.data.ai.GeminiAiMentorService()
        )
    }

    private fun createMockAppDb(): com.example.data.db.AppDatabase {
        return object : com.example.data.db.AppDatabase() {
            override fun gameDao(): com.example.data.db.GameDao {
                return MockGameDao()
            }
            override fun clearAllTables() {}
            override fun createInvalidationTracker(): androidx.room.InvalidationTracker {
                return androidx.room.InvalidationTracker(this, emptyMap(), emptyMap(), "game_save")
            }
            override fun createOpenHelper(config: androidx.room.DatabaseConfiguration): androidx.sqlite.db.SupportSQLiteOpenHelper {
                throw UnsupportedOperationException()
            }
        }
    }
}

class MockGameDao : com.example.data.db.GameDao {
    override fun getGameSave(): Flow<GameSaveEntity?> =
        kotlinx.coroutines.flow.flowOf(GameSaveEntity(id = 1, osName = "GenesisOS"))

    override suspend fun getGameSaveSync(): GameSaveEntity? =
        GameSaveEntity(id = 1, osName = "GenesisOS")

    override suspend fun insertGameSave(save: GameSaveEntity) {}

    override suspend fun updateGameSave(save: GameSaveEntity) {}

    override fun getAllQuests(): Flow<List<QuestProgressEntity>> =
        kotlinx.coroutines.flow.flowOf(emptyList())

    override suspend fun getQuestById(questId: String): QuestProgressEntity? = null

    override suspend fun insertOrUpdateQuest(quest: QuestProgressEntity) {}

    override suspend fun insertQuestsIfNotExist(quests: List<QuestProgressEntity>) {}

    override fun getAllFiles(): Flow<List<VirtualFileEntity>> =
        kotlinx.coroutines.flow.flowOf(emptyList())

    override suspend fun getFileByPath(path: String): VirtualFileEntity? = null

    override suspend fun insertOrUpdateFile(file: VirtualFileEntity) {}

    override suspend fun insertFilesIfNotExist(files: List<VirtualFileEntity>) {}

    override suspend fun deleteFileByPath(path: String) {}

    override fun getAllAiMessages(): Flow<List<AiMessageEntity>> =
        kotlinx.coroutines.flow.flowOf(emptyList())

    override suspend fun insertAiMessage(message: AiMessageEntity) {}

    override suspend fun clearAiHistory() {}
}
