package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.OsArchitectApplication
import com.example.data.db.GameSaveEntity
import com.example.data.game.CpuRegisters
import com.example.data.game.Quest
import com.example.data.game.TerminalLine
import com.example.data.game.TerminalLineType
import com.example.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Coordinator ViewModel managing global game UI state, active tabs, CRT shader effects,
 * and delegating subsystem actions to feature-specific ViewModels.
 */
class GameScreenViewModel(
    private val repository: GameRepository,
    val terminalVm: TerminalViewModel,
    val questVm: QuestViewModel,
    val editorVm: EditorViewModel,
    val vmViewModel: VirtualMachineViewModel,
    val aiMentorVm: AiMentorViewModel
) : ViewModel() {

    val gameSave: StateFlow<GameSaveEntity?> = repository.gameSave
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _selectedTab = MutableStateFlow(AppTab.TERMINAL)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    private val _cpuRegisters = MutableStateFlow(CpuRegisters())
    val cpuRegisters: StateFlow<CpuRegisters> = _cpuRegisters.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as OsArchitectApplication)
                val container = application.container

                val terminalVm = TerminalViewModel(
                    executeTerminalUseCase = container.executeTerminalCommandUseCase,
                    repository = container.gameRepository,
                    terminalEngine = container.terminalEngine
                )
                val questVm = QuestViewModel(
                    completeQuestUseCase = container.completeQuestUseCase,
                    questProvider = container.questProvider,
                    repository = container.gameRepository
                )
                val editorVm = EditorViewModel(
                    runTestsUseCase = container.runQuestTestsUseCase,
                    saveCodeUseCase = container.saveUserCodeUseCase,
                    repository = container.gameRepository
                )
                val vmViewModel = VirtualMachineViewModel(
                    bootVmUseCase = container.bootVirtualMachineUseCase,
                    repository = container.gameRepository
                )
                val aiMentorVm = AiMentorViewModel(
                    askAiUseCase = container.askAiMentorUseCase,
                    repository = container.gameRepository
                )

                GameScreenViewModel(
                    repository = container.gameRepository,
                    terminalVm = terminalVm,
                    questVm = questVm,
                    editorVm = editorVm,
                    vmViewModel = vmViewModel,
                    aiMentorVm = aiMentorVm
                )
            }
        }
    }

    init {
        viewModelScope.launch {
            repository.initializeGameIfNeeded()
            val initialQuest = questVm.getActiveQuest()
            if (initialQuest != null) {
                editorVm.loadCodeForQuest(initialQuest)
            }
        }
    }

    fun selectTab(tab: AppTab) {
        _selectedTab.value = tab
    }

    fun selectQuest(questId: String) {
        questVm.selectQuest(questId)
        val quest = questVm.getActiveQuest()
        if (quest != null) {
            editorVm.loadCodeForQuest(quest)
        }
    }

    fun executeTerminalCommand(input: String) {
        val currentQuest = questVm.getActiveQuest()
        val completedIds = questVm.getCompletedQuestIds()

        terminalVm.executeCommand(
            input = input,
            currentQuest = currentQuest,
            userCode = editorVm.editorCode.value,
            completedQuestIds = completedIds,
            onSelectQuest = { selectQuest(it) },
            onRunTests = { runTests() },
            onBootVm = { bootVm() },
            onAskAi = { askAiMentor(it) }
        )
    }

    fun runTests() {
        val currentQuest = questVm.getActiveQuest() ?: return
        editorVm.runTests(currentQuest) { qid ->
            val result = questVm.completeQuest(qid, stars = 3)
            val logLines = mutableListOf<TerminalLine>()
            logLines.add(TerminalLine("=== WYNIKI TESTÓW JEDNOSTKOWYCH DLA $qid ===", TerminalLineType.HEADER))
            logLines.add(TerminalLine("🎉 GRATULACJE! Wszystkie testy zaliczone pomyślnie!", TerminalLineType.SUCCESS))
            terminalVm.appendLines(logLines)
            _toastEvent.emit("Zadanie zaliczone!")
        }
    }

    fun bootVm() {
        val osName = gameSave.value?.osName ?: "GenesisOS"
        val completedIds = questVm.getCompletedQuestIds()
        vmViewModel.bootVm(osName, completedIds)
    }

    fun executeGuestOsCommand(cmd: String) {
        val osName = gameSave.value?.osName ?: "GenesisOS"
        val completedIds = questVm.getCompletedQuestIds()
        vmViewModel.executeGuestCommand(cmd, osName, completedIds)
    }

    fun toggleScanlines() {
        viewModelScope.launch {
            repository.toggleCrtScanlines()
        }
    }

    fun askAiMentor(prompt: String) {
        val currentQuest = questVm.getActiveQuest()
        aiMentorVm.askMentor(prompt, currentQuest, editorVm.editorCode.value) { result ->
            val lines = mutableListOf<TerminalLine>()
            lines.add(TerminalLine("🤖 Ada (Kernel Architect Copilot):", TerminalLineType.HEADER))
            result.replyText.lines().take(12).forEach { l ->
                lines.add(TerminalLine(l, TerminalLineType.OUTPUT))
            }
            terminalVm.appendLines(lines)
        }
    }

    fun showToast(message: String) {
        viewModelScope.launch {
            _toastEvent.emit(message)
        }
    }
}
