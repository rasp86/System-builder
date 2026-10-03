package com.example.game.terminal

import com.example.data.game.Quest
import com.example.data.game.TerminalLine
import com.example.data.game.engine.DmesgRingBuffer
import com.example.data.game.engine.HardwareCpuSimulator
import com.example.data.game.engine.ProcessScheduler
import com.example.data.game.engine.TerminalCommandDispatcher
import com.example.data.game.engine.VirtualFileSystemManager
import com.example.data.local.OsSnapshotRepository

/**
 * Execution context provided to each terminal command.
 */
data class CommandContext(
    val commandStr: String,
    val args: List<String>,
    val currentQuest: Quest?,
    val userCode: String,
    val completedQuestIds: Set<String>,
    val vfsManager: VirtualFileSystemManager,
    val cpuSimulator: HardwareCpuSimulator,
    val processScheduler: ProcessScheduler,
    val dmesgBuffer: DmesgRingBuffer,
    val getBootTimeMillis: () -> Long,
    val getThemeId: () -> String,
    val setThemeId: (String) -> Unit,
    val getSnapshotRepo: () -> OsSnapshotRepository?,
    val getHistory: () -> List<String>,
    val onSelectQuest: (String) -> Unit,
    val onRunTests: () -> Unit,
    val onBootVm: () -> Unit,
    val onAskAi: (String) -> Unit,
    val dispatcher: TerminalCommandDispatcher? = null
)

/**
 * Interface representing a modular terminal shell command.
 */
interface TerminalCommand {
    val name: String
    val aliases: List<String> get() = emptyList()
    val description: String
    val usage: String get() = name

    fun execute(args: List<String>, context: CommandContext): List<TerminalLine>
}
