package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiMentorResult
import com.example.data.db.AppDatabase
import com.example.data.db.GameSaveEntity
import com.example.data.db.QuestProgressEntity
import com.example.data.game.CpuRegisters
import com.example.data.game.OSBootStatus
import com.example.data.game.OsSimulationEngine
import com.example.data.game.Quest
import com.example.data.game.QuestsData
import com.example.data.game.TerminalEngine
import com.example.data.game.TerminalLine
import com.example.data.game.TerminalLineType
import com.example.data.repository.GameRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab {
    TERMINAL,
    QUESTS,
    EDITOR,
    VIRTUAL_MACHINE,
    ARCHITECTURE,
    AI_MENTOR
}

data class TestResultItem(
    val title: String,
    val isPassed: Boolean,
    val message: String
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = GameRepository(database)
    private val terminalEngine = TerminalEngine()

    val gameSave: StateFlow<GameSaveEntity?> = repository.gameSave
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allQuestsProgress: StateFlow<List<QuestProgressEntity>> = repository.allQuests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val aiMessages = repository.aiMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTab = MutableStateFlow(AppTab.TERMINAL)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    private val _activeQuestId = MutableStateFlow<String>("Q1_1_MBR_MAGIC")
    val activeQuestId: StateFlow<String> = _activeQuestId.asStateFlow()

    private val _editorCode = MutableStateFlow<String>("")
    val editorCode: StateFlow<String> = _editorCode.asStateFlow()

    private val _terminalLines = MutableStateFlow<List<TerminalLine>>(emptyList())
    val terminalLines: StateFlow<List<TerminalLine>> = _terminalLines.asStateFlow()

    private val _guestTerminalLines = MutableStateFlow<List<TerminalLine>>(emptyList())
    val guestTerminalLines: StateFlow<List<TerminalLine>> = _guestTerminalLines.asStateFlow()

    private val _vmStatus = MutableStateFlow(OSBootStatus.POWERED_OFF)
    val vmStatus: StateFlow<OSBootStatus> = _vmStatus.asStateFlow()

    private val _vmLogs = MutableStateFlow<List<String>>(emptyList())
    val vmLogs: StateFlow<List<String>> = _vmLogs.asStateFlow()

    private val _testResults = MutableStateFlow<List<TestResultItem>>(emptyList())
    val testResults: StateFlow<List<TestResultItem>> = _testResults.asStateFlow()

    private val _isCompiling = MutableStateFlow(false)
    val isCompiling: StateFlow<Boolean> = _isCompiling.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _cpuRegisters = MutableStateFlow(CpuRegisters())
    val cpuRegisters: StateFlow<CpuRegisters> = _cpuRegisters.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.initializeGameIfNeeded()
            // Set initial active quest code
            val quest = QuestsData.allQuests.first()
            val progress = repository.getQuestById(quest.id)
            _editorCode.value = progress?.userCode ?: quest.defaultCode

            // Initial welcome terminal message
            _terminalLines.value = listOf(
                TerminalLine("═══════════════════════════════════════════════════════════════", TerminalLineType.HEADER),
                TerminalLine(" GENESIS OS ARCHITECT v1.0 - ŚRODOWISKO TWORZENIA JĄDRA OS", TerminalLineType.HEADER),
                TerminalLine("═══════════════════════════════════════════════════════════════", TerminalLineType.HEADER),
                TerminalLine("Twoja misja: Zbuduj działający system operacyjny krok po kroku.", TerminalLineType.SYSTEM),
                TerminalLine("Wpisz 'help' aby zobaczyć komendy lub 'quests' aby wybrać zadanie.", TerminalLineType.OUTPUT),
                TerminalLine("Wskazówka: Użyj przycisku [Ada AI Mentor] w razie wątpliwości!", TerminalLineType.SUCCESS)
            )
        }
    }

    fun selectTab(tab: AppTab) {
        _selectedTab.value = tab
    }

    fun selectQuest(questId: String) {
        viewModelScope.launch {
            _activeQuestId.value = questId
            val quest = QuestsData.allQuests.find { it.id == questId } ?: return@launch
            val progress = repository.getQuestById(questId)
            _editorCode.value = progress?.userCode ?: quest.defaultCode
            _testResults.value = emptyList()
        }
    }

    fun updateEditorCode(newCode: String) {
        _editorCode.value = newCode
        viewModelScope.launch {
            val currentQuest = QuestsData.allQuests.find { it.id == _activeQuestId.value }
            if (currentQuest != null) {
                repository.saveUserCode(_activeQuestId.value, newCode, currentQuest.filePath)
            }
        }
    }

    fun resetToDefaultCode() {
        val quest = QuestsData.allQuests.find { it.id == _activeQuestId.value } ?: return
        updateEditorCode(quest.defaultCode)
        _toastEvent.tryEmit("Przywrócono domyślny szablon kodu.")
    }

    fun resetToReferenceSolution() {
        val quest = QuestsData.allQuests.find { it.id == _activeQuestId.value } ?: return
        updateEditorCode(quest.referenceSolution)
        _toastEvent.tryEmit("Załadowano rozwiązanie referencyjne!")
    }

    fun executeTerminalCommand(input: String) {
        if (input.isBlank()) return
        val currentQuest = QuestsData.allQuests.find { it.id == _activeQuestId.value }
        val completedIds = allQuestsProgress.value.filter { it.isCompleted }.map { it.questId }.toSet()

        if (input.trim().equals("clear", ignoreCase = true) || input.trim().equals("cls", ignoreCase = true)) {
            _terminalLines.value = emptyList()
            return
        }

        val newLines = terminalEngine.executeHostCommand(
            commandStr = input,
            currentQuest = currentQuest,
            userCode = _editorCode.value,
            completedQuestIds = completedIds,
            onSelectQuest = { selectQuest(it) },
            onRunTests = { runTests() },
            onBootVm = { bootVirtualMachine() },
            onAskAi = { askAiMentor(it) }
        )

        _terminalLines.value = _terminalLines.value + newLines
    }

    fun compileCurrentCode() {
        val currentQuest = QuestsData.allQuests.find { it.id == _activeQuestId.value } ?: return
        viewModelScope.launch {
            _isCompiling.value = true
            delay(400) // Simulated compilation time

            val output = mutableListOf<TerminalLine>()
            output.add(TerminalLine("> build ${currentQuest.filePath}", TerminalLineType.INPUT))
            output.add(TerminalLine("[COMPILER] Parsing ${currentQuest.filePath}...", TerminalLineType.OUTPUT))
            output.add(TerminalLine("[COMPILER] Optimizing AST & Register Allocations...", TerminalLineType.OUTPUT))
            output.add(TerminalLine("[COMPILER] Linking binary into /boot/kernel.bin -> SUCCESS", TerminalLineType.SUCCESS))

            _terminalLines.value = _terminalLines.value + output
            _isCompiling.value = false
            _toastEvent.emit("Kompilacja powiodła się!")
        }
    }

    fun runTests() {
        val currentQuest = QuestsData.allQuests.find { it.id == _activeQuestId.value } ?: return
        viewModelScope.launch {
            _isCompiling.value = true
            delay(500)

            val results = currentQuest.testCases.map { tc ->
                val (passed, message) = tc.check(_editorCode.value)
                TestResultItem(tc.description, passed, message)
            }
            _testResults.value = results
            _isCompiling.value = false

            val allPassed = results.all { it.isPassed }
            val logLines = mutableListOf<TerminalLine>()
            logLines.add(TerminalLine("=== WYNIKI TESTÓW JEDNOSTKOWYCH DLA ${currentQuest.id} ===", TerminalLineType.HEADER))

            results.forEachIndexed { i, res ->
                val icon = if (res.isPassed) "✔ PASS" else "✖ FAIL"
                val type = if (res.isPassed) TerminalLineType.SUCCESS else TerminalLineType.ERROR
                logLines.add(TerminalLine("[$icon] Test ${i + 1}: ${res.title} - ${res.message}", type))
            }

            if (allPassed) {
                val (xp, bits) = repository.completeQuest(currentQuest.id, stars = 3)
                logLines.add(TerminalLine("🎉 GRATULACJE! Moduł zweryfikowany pomyślnie!", TerminalLineType.SUCCESS))
                logLines.add(TerminalLine("Nagroda: +$xp XP | +$bits Bits | Odblokowano: ${currentQuest.unlockedPerk}", TerminalLineType.SYSTEM))
                _toastEvent.emit("Zadanie zaliczone! +$xp XP")
            } else {
                logLines.add(TerminalLine("Niektóre testy nie przeszły. Sprawdź błędy lub zapytaj mentorkę Adę.", TerminalLineType.WARNING))
            }

            _terminalLines.value = _terminalLines.value + logLines
        }
    }

    fun bootVirtualMachine() {
        val completedIds = allQuestsProgress.value.filter { it.isCompleted }.map { it.questId }.toSet()
        val save = gameSave.value
        val osName = save?.osName ?: "GenesisOS"

        viewModelScope.launch {
            repository.incrementBootCount()
            _vmStatus.value = OSBootStatus.BIOS_POST
            _vmLogs.value = emptyList()
            _guestTerminalLines.value = emptyList()

            val bootSequence = OsSimulationEngine.generateBootSequence(osName, completedIds)

            for (log in bootSequence) {
                _vmLogs.value = _vmLogs.value + log
                delay(80) // Typewriter boot animation
            }

            if (bootSequence.any { it.contains("PANIC") || it.contains("FATAL") }) {
                _vmStatus.value = OSBootStatus.KERNEL_PANIC
            } else if (completedIds.contains("Q8_1_GUI_FRAMEBUFFER")) {
                _vmStatus.value = OSBootStatus.RUNNING_GUI
            } else {
                _vmStatus.value = OSBootStatus.RUNNING_CLI
                _guestTerminalLines.value = listOf(
                    TerminalLine("$osName Kernel v1.0.0 Ready. Type 'help' for commands.", TerminalLineType.SUCCESS),
                    TerminalLine("gsh root@genesis:/# ", TerminalLineType.SYSTEM)
                )
            }
        }
    }

    fun executeGuestOsCommand(command: String) {
        if (command.isBlank()) return
        val save = gameSave.value
        val osName = save?.osName ?: "GenesisOS"
        val completedIds = allQuestsProgress.value.filter { it.isCompleted }.map { it.questId }.toSet()

        val output = mutableListOf<TerminalLine>()
        output.add(TerminalLine("gsh root@genesis:/# $command", TerminalLineType.INPUT))

        if (command.trim().equals("clear", ignoreCase = true)) {
            _guestTerminalLines.value = emptyList()
            return
        }

        if (command.trim().equals("reboot", ignoreCase = true)) {
            bootVirtualMachine()
            return
        }

        if (command.trim().equals("poweroff", ignoreCase = true) || command.trim().equals("shutdown", ignoreCase = true)) {
            _vmStatus.value = OSBootStatus.POWERED_OFF
            _guestTerminalLines.value = emptyList()
            _vmLogs.value = emptyList()
            return
        }

        if (command.trim().equals("gui", ignoreCase = true) && completedIds.contains("Q8_1_GUI_FRAMEBUFFER")) {
            _vmStatus.value = OSBootStatus.RUNNING_GUI
            return
        }

        val resultLines = OsSimulationEngine.executeGuestOsCommand(command, osName, completedIds)
        resultLines.forEach { line ->
            output.add(TerminalLine(line, TerminalLineType.OUTPUT))
        }

        _guestTerminalLines.value = _guestTerminalLines.value + output
    }

    fun switchGuestToCli() {
        _vmStatus.value = OSBootStatus.RUNNING_CLI
    }

    fun switchGuestToGui() {
        _vmStatus.value = OSBootStatus.RUNNING_GUI
    }

    fun powerOffVm() {
        _vmStatus.value = OSBootStatus.POWERED_OFF
        _vmLogs.value = emptyList()
        _guestTerminalLines.value = emptyList()
    }

    fun toggleScanlines() {
        viewModelScope.launch {
            repository.toggleCrtScanlines()
        }
    }

    fun askAiMentor(prompt: String) {
        val currentQuest = QuestsData.allQuests.find { it.id == _activeQuestId.value }
        viewModelScope.launch {
            _isAiThinking.value = true
            val result = repository.askAi(prompt, currentQuest, _editorCode.value)
            _isAiThinking.value = false

            // Also output summary to Host Terminal
            val lines = mutableListOf<TerminalLine>()
            lines.add(TerminalLine("🤖 Ada (Kernel Architect Copilot):", TerminalLineType.HEADER))
            result.replyText.lines().take(12).forEach { l ->
                lines.add(TerminalLine(l, TerminalLineType.OUTPUT))
            }
            _terminalLines.value = _terminalLines.value + lines
        }
    }

    fun clearAiChat() {
        viewModelScope.launch {
            repository.clearAiChat()
        }
    }
}
