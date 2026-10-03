package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.game.OSBootStatus
import com.example.data.game.OsSimulationEngine
import com.example.data.game.TerminalLine
import com.example.data.game.TerminalLineType
import com.example.data.repository.GameRepository
import com.example.domain.usecase.BootVirtualMachineUseCase
import com.example.game.vm.BootSequenceGenerator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Feature ViewModel managing QEMU/x86 virtual machine simulation and guest OS CLI/GUI.
 */
class VirtualMachineViewModel(
    private val bootVmUseCase: BootVirtualMachineUseCase,
    private val repository: GameRepository,
    private val bootSequenceGenerator: BootSequenceGenerator = BootSequenceGenerator()
) : ViewModel() {

    private val _vmStatus = MutableStateFlow(OSBootStatus.POWERED_OFF)
    val vmStatus: StateFlow<OSBootStatus> = _vmStatus.asStateFlow()

    private val _vmLogs = MutableStateFlow<List<String>>(emptyList())
    val vmLogs: StateFlow<List<String>> = _vmLogs.asStateFlow()

    private val _guestTerminalLines = MutableStateFlow<List<TerminalLine>>(emptyList())
    val guestTerminalLines: StateFlow<List<TerminalLine>> = _guestTerminalLines.asStateFlow()

    private var bootJob: Job? = null

    fun bootVm(osName: String, completedQuestIds: Set<String>) {
        bootJob?.cancel()
        bootJob = viewModelScope.launch {
            repository.incrementBootCount()
            _vmStatus.value = OSBootStatus.BIOS_POST
            _vmLogs.value = emptyList()
            _guestTerminalLines.value = emptyList()

            // Synthetic PC speaker POST beep when initializing boot sequence
            com.example.core.audio.KernelSoundManager.playBiosPostBeep()

            val bootResult = bootSequenceGenerator.generateBootSequence(osName, completedQuestIds)

            for (log in bootResult.logs) {
                _vmLogs.value = _vmLogs.value + log
                delay(60) // Typewriter cadence
            }

            if (bootResult.hasPanic) {
                _vmStatus.value = OSBootStatus.KERNEL_PANIC
                com.example.core.audio.KernelSoundManager.playKernelPanicBeep()
            } else if (completedQuestIds.contains("Q8_1_GUI_FRAMEBUFFER")) {
                _vmStatus.value = OSBootStatus.RUNNING_GUI
                com.example.core.audio.KernelSoundManager.playKernelBootChime()
            } else {
                _vmStatus.value = OSBootStatus.RUNNING_CLI
                com.example.core.audio.KernelSoundManager.playKernelBootChime()
                _guestTerminalLines.value = listOf(
                    TerminalLine("$osName Kernel v1.0.0 Ready. Type 'help' for commands.", TerminalLineType.SUCCESS),
                    TerminalLine("gsh root@genesis:/# ", TerminalLineType.SYSTEM)
                )
            }
        }
    }

    fun executeGuestCommand(command: String, osName: String, completedQuestIds: Set<String>) {
        if (command.isBlank()) return

        val output = mutableListOf<TerminalLine>()
        output.add(TerminalLine("gsh root@genesis:/# $command", TerminalLineType.INPUT))

        val trimmed = command.trim()
        if (trimmed.equals("clear", ignoreCase = true)) {
            _guestTerminalLines.value = emptyList()
            return
        }

        if (trimmed.equals("reboot", ignoreCase = true)) {
            bootVm(osName, completedQuestIds)
            return
        }

        if (trimmed.equals("poweroff", ignoreCase = true) || trimmed.equals("shutdown", ignoreCase = true)) {
            powerOffVm()
            return
        }

        if (trimmed.equals("gui", ignoreCase = true) && completedQuestIds.contains("Q8_1_GUI_FRAMEBUFFER")) {
            _vmStatus.value = OSBootStatus.RUNNING_GUI
            return
        }

        val resultLines = bootVmUseCase.executeGuestCommand(command, osName, completedQuestIds)
        resultLines.forEach { line ->
            output.add(TerminalLine(line, TerminalLineType.OUTPUT))
        }

        _guestTerminalLines.value = _guestTerminalLines.value + output
    }

    fun switchToCli() {
        _vmStatus.value = OSBootStatus.RUNNING_CLI
    }

    fun switchToGui() {
        _vmStatus.value = OSBootStatus.RUNNING_GUI
    }

    fun powerOffVm() {
        bootJob?.cancel()
        _vmStatus.value = OSBootStatus.POWERED_OFF
        _vmLogs.value = emptyList()
        _guestTerminalLines.value = emptyList()
    }
}
