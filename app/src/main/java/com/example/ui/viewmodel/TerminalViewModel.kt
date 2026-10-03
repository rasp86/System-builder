package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.game.Quest
import com.example.data.game.TerminalEngine
import com.example.data.game.TerminalLine
import com.example.data.game.TerminalLineType
import com.example.data.repository.GameRepository
import com.example.domain.usecase.ExecuteTerminalCommandUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Feature ViewModel managing terminal shell interaction, command execution, and CRT output.
 */
class TerminalViewModel(
    private val executeTerminalUseCase: ExecuteTerminalCommandUseCase,
    private val repository: GameRepository,
    val terminalEngine: TerminalEngine
) : ViewModel() {

    private val _terminalLines = MutableStateFlow<List<TerminalLine>>(emptyList())
    val terminalLines: StateFlow<List<TerminalLine>> = _terminalLines.asStateFlow()

    private val _isCompiling = MutableStateFlow(false)
    val isCompiling: StateFlow<Boolean> = _isCompiling.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    private var activeExecutionJob: Job? = null

    init {
        runTerminalBootSequence()
    }

    fun runTerminalBootSequence() {
        viewModelScope.launch {
            _terminalLines.value = emptyList()
            com.example.core.audio.KernelSoundManager.playBiosPostBeep()

            val bootSequence = listOf(
                TerminalLine("SeaBIOS (version 1.15.0-rel-0-genesis)...", TerminalLineType.HEADER),
                TerminalLine("Machine: x86 Standard PC (i440FX + PIIX, 1996) 64 MB RAM", TerminalLineType.OUTPUT),
                TerminalLine("Booting from Hard Disk (drive 0x80)...", TerminalLineType.SYSTEM),
                TerminalLine("[   0.000000] Loading kernel image /boot/genesis_kernel.elf (ELF32)...", TerminalLineType.SYSTEM),
                TerminalLine("[   0.001420] Initializing GDT & IDT interrupt descriptor tables...", TerminalLineType.SUCCESS),
                TerminalLine("[   0.004200] Initializing 2-level paging MMU (CR0.PG=1, CR3=0x9C000)...", TerminalLineType.SUCCESS),
                TerminalLine("[   0.008900] Initializing hardware drivers: PIT Timer (100Hz), VGA 80x25...", TerminalLineType.SUCCESS),
                TerminalLine("[   0.014200] Mounting root Virtual File System (VFS) on /...", TerminalLineType.SUCCESS),
                TerminalLine("[   0.021000] Starting background services: init (PID 1), scheduler (PID 2)...", TerminalLineType.SYSTEM),
                TerminalLine("═══════════════════════════════════════════════════════════════", TerminalLineType.HEADER),
                TerminalLine(" GENESIS OS ARCHITECT v1.0 - DEVELOPER HOST SHELL (/dev/tty1)", TerminalLineType.HEADER),
                TerminalLine("═══════════════════════════════════════════════════════════════", TerminalLineType.HEADER),
                TerminalLine("Wskazówka: Wpisz 'help', 'ps', 'dmesg' lub 'quests'. Skróty: Ctrl+L, Ctrl+C", TerminalLineType.OUTPUT),
                TerminalLine("Stan systemu: Gotowy do pracy. Mentorka Ada czeka na Twoje rozkazy!", TerminalLineType.SUCCESS)
            )

            for (line in bootSequence) {
                _terminalLines.value = _terminalLines.value + line
                delay(40)
            }
        }
    }

    fun executeCommand(
        input: String,
        currentQuest: Quest?,
        userCode: String,
        completedQuestIds: Set<String>,
        onSelectQuest: (String) -> Unit = {},
        onRunTests: () -> Unit = {},
        onBootVm: () -> Unit = {},
        onAskAi: (String) -> Unit = {}
    ) {
        if (input.isBlank()) return

        val rawTrimmed = input.trim()
        val firstToken = rawTrimmed.split("\\s+".toRegex()).firstOrNull()?.lowercase() ?: ""
        val expandedCommand = if (terminalEngine.customAliases.containsKey(firstToken)) {
            val target = terminalEngine.customAliases[firstToken] ?: ""
            val rest = rawTrimmed.split("\\s+".toRegex()).drop(1).joinToString(" ")
            if (rest.isNotBlank()) "$target $rest" else target
        } else {
            rawTrimmed
        }

        if (expandedCommand.equals("clear", ignoreCase = true) || expandedCommand.equals("cls", ignoreCase = true) ||
            rawTrimmed.equals("clear", ignoreCase = true) || rawTrimmed.equals("cls", ignoreCase = true)) {
            clearTerminal()
            return
        }

        if (rawTrimmed.equals("interrupt", ignoreCase = true) || rawTrimmed.equals("^C", ignoreCase = true)) {
            interruptProcess()
            return
        }

        if (rawTrimmed.equals("reboot", ignoreCase = true)) {
            runTerminalBootSequence()
            return
        }

        val newLines = executeTerminalUseCase.execute(
            commandStr = input,
            currentQuest = currentQuest,
            userCode = userCode,
            completedQuestIds = completedQuestIds,
            onSelectQuest = onSelectQuest,
            onRunTests = onRunTests,
            onBootVm = onBootVm,
            onAskAi = onAskAi
        )

        _terminalLines.value = _terminalLines.value + newLines

        if (rawTrimmed.startsWith("theme ", ignoreCase = true) || rawTrimmed.startsWith("colorscheme ", ignoreCase = true) || rawTrimmed.startsWith("scheme ", ignoreCase = true)) {
            viewModelScope.launch {
                repository.updateTerminalColorScheme(terminalEngine.currentThemeId)
            }
        }
    }

    fun clearTerminal() {
        _terminalLines.value = emptyList()
        terminalEngine.clearHistory()
        _toastEvent.tryEmit("Wyczyszczono ekran terminala.")
    }

    fun appendLines(lines: List<TerminalLine>) {
        _terminalLines.value = _terminalLines.value + lines
    }

    fun interruptProcess() {
        if (_isCompiling.value || activeExecutionJob?.isActive == true) {
            activeExecutionJob?.cancel()
            _isCompiling.value = false
        }
        val sigintLines = listOf(
            TerminalLine("^C", TerminalLineType.INPUT),
            TerminalLine("[SIGINT: Proces zatrzymany przez użytkownika (Ctrl+C)]", TerminalLineType.ERROR)
        )
        _terminalLines.value = _terminalLines.value + sigintLines
        _toastEvent.tryEmit("Przerwano działanie procesu (Ctrl+C)")
    }
}
