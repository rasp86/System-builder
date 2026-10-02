package com.example.data.game

import com.example.data.game.engine.DmesgRingBuffer
import com.example.data.game.engine.HardwareCpuSimulator
import com.example.data.game.engine.ManualPagesProvider
import com.example.data.game.engine.ProcessScheduler
import com.example.data.game.engine.TerminalCommandDispatcher
import com.example.data.game.engine.VirtualFileSystemManager
import com.example.data.local.OsSnapshotRepository

/**
 * Unified Terminal & OS Simulation Engine.
 * Acts as a clean facade orchestrating VFS, CPU simulation, process scheduling, and kernel buffers.
 */
class TerminalEngine {

    val systemBootTimestampMillis: Long = System.currentTimeMillis()
    private val executionHistory = mutableListOf<String>()
    var snapshotRepository: OsSnapshotRepository? = null
    var currentThemeId: String = "CYBER_MATRIX"

    // Subsystem components
    val dmesgBuffer = DmesgRingBuffer()
    val vfsManager = VirtualFileSystemManager()
    val cpuSimulator = HardwareCpuSimulator(dmesgBuffer)
    val processScheduler = ProcessScheduler(dmesgBuffer)

    // Delegated properties for complete compatibility
    val customAliases: MutableMap<String, String> get() = vfsManager.customAliases
    val virtualFileSystem: MutableMap<String, VirtualFsNode> get() = vfsManager.virtualFileSystem
    val backgroundProcesses: MutableList<SystemProcess> get() = processScheduler.backgroundProcesses
    val dmesgEntries: MutableList<DmesgEntry> get() = dmesgBuffer.entries
    val memoryMap: MutableMap<Long, ByteArray> get() = cpuSimulator.memoryMap

    var cpuHardwareState: CpuHardwareState
        get() = cpuSimulator.cpuHardwareState
        set(value) { cpuSimulator.cpuHardwareState = value }

    var cpuRegisters: CpuRegisters
        get() = cpuSimulator.cpuRegisters
        set(value) { cpuSimulator.cpuRegisters = value }

    private val dispatcher = TerminalCommandDispatcher(
        vfsManager = vfsManager,
        cpuSim = cpuSimulator,
        processScheduler = processScheduler,
        dmesgBuffer = dmesgBuffer,
        getBootTimeMillis = { systemBootTimestampMillis },
        getThemeId = { currentThemeId },
        setThemeId = { currentThemeId = it },
        getSnapshotRepo = { snapshotRepository },
        getHistory = { executionHistory.toList() }
    )

    fun clearHistory() {
        executionHistory.clear()
    }

    fun getHistory(): List<String> = executionHistory.toList()

    fun syncAliasesToVfs() {
        vfsManager.syncAliasesToVfs()
    }

    fun loadAliasesFromVfs() {
        vfsManager.loadAliasesFromVfs()
    }

    fun setMemoryByte(address: Long, byteOffset: Int, value: Byte) {
        cpuSimulator.setMemoryByte(address, byteOffset, value)
    }

    fun assembleInstruction(mnemonic: String): Triple<ByteArray, String, Int> {
        return cpuSimulator.assembleInstruction(mnemonic)
    }

    fun getManualPage(command: String): List<TerminalLine> {
        return ManualPagesProvider.getManualPage(command)
    }

    val allAvailableCommands: List<String> = listOf(
        "help",
        "ls",
        "cat",
        "touch",
        "mkdir",
        "rm",
        "chmod",
        "df",
        "tree",
        "grep",
        "save",
        "load",
        "benchmark",
        "alias",
        "unalias",
        "int",
        "interrupt",
        "ps",
        "dmesg",
        "kill",
        "pkill",
        "killall",
        "man",
        "asm",
        "mem",
        "reg",
        "clear",
        "cls",
        "whoami",
        "date",
        "uptime",
        "theme",
        "colorscheme",
        "scheme",
        "pwd",
        "echo",
        "version",
        "history",
        "quests",
        "quest",
        "build",
        "compile",
        "test",
        "boot",
        "run",
        "arch",
        "specs",
        "registers",
        "hint",
        "files",
        "ai",
        "status",
        "stats"
    )

    val allKnownFilePaths: List<String> get() = virtualFileSystem.keys.toList()

    fun getTabCompletions(currentText: String): List<String> {
        val trimmed = currentText.trimStart()
        if (trimmed.isEmpty()) return listOf("help", "ls", "cat", "touch", "chmod", "df", "mkdir", "rm", "tree", "grep", "save", "load", "benchmark", "alias", "uptime", "ps", "kill", "clear")

        val parts = trimmed.split("\\s+".toRegex())
        val lastToken = parts.last()

        if (parts.size <= 1) {
            val matches = allAvailableCommands.filter { it.startsWith(lastToken, ignoreCase = true) }
            return if (matches.isNotEmpty()) matches else allAvailableCommands.take(6)
        } else {
            val cmd = parts[0].lowercase()
            if (cmd == "man") {
                return allAvailableCommands.filter { it.contains(lastToken, ignoreCase = true) }
            }
            if (cmd in listOf("cat", "ls", "touch", "chmod", "mkdir", "rm", "tree", "grep", "open", "edit")) {
                val matches = virtualFileSystem.keys.filter { it.contains(lastToken, ignoreCase = true) }
                return if (matches.isNotEmpty()) matches else virtualFileSystem.keys.take(6).toList()
            }
            if (cmd in listOf("kill", "pkill", "killall")) {
                val procPids = backgroundProcesses.map { it.pid.toString() }
                val procNames = backgroundProcesses.map { it.command.substringBefore(" ").substringAfterLast('/') }
                val killCandidates = (procPids + procNames + listOf("-9", "-15", "-l")).distinct()
                return killCandidates.filter { it.contains(lastToken, ignoreCase = true) }
            }
            if (cmd in listOf("alias", "unalias")) {
                val aliasCandidates = customAliases.keys.toList() + listOf("-r", "-d", "-a", "--reload", "--delete")
                return aliasCandidates.filter { it.contains(lastToken, ignoreCase = true) }
            }
            if (cmd == "int" || cmd == "interrupt") {
                val intVectors = listOf("0x80", "0x10", "0x20", "0x21", "0x0E", "0x0D", "0x00")
                return intVectors.filter { it.contains(lastToken, ignoreCase = true) }
            }
            if (cmd == "asm") {
                val asmKeywords = listOf("0x7C00", "0x9C000", "0xB8000", "0x100000", "mov", "xor", "cli", "sti", "hlt", "nop", "int", "dw", "jmp")
                return asmKeywords.filter { it.contains(lastToken, ignoreCase = true) }
            }
            if (cmd == "mem" || cmd == "reg") {
                val memKeywords = listOf("set", "dump", "reg", "reset", "0x7C00", "0x9C000", "0xB8000", "0x100000", "eax", "ebx", "ecx", "edx", "cr0", "cr3")
                return memKeywords.filter { it.contains(lastToken, ignoreCase = true) }
            }
            if (cmd == "quest") {
                val questIds = QuestsData.allQuests.map { it.id }
                return questIds.filter { it.contains(lastToken, ignoreCase = true) }
            }
            if (cmd in listOf("theme", "colorscheme", "scheme")) {
                val themeOptions = listOf("green", "amber", "mono", "cyber", "solarized", "retro", "vt220", "vt100")
                return themeOptions.filter { it.contains(lastToken, ignoreCase = true) }
            }
            val allCandidates = allAvailableCommands + virtualFileSystem.keys
            return allCandidates.filter { it.contains(lastToken, ignoreCase = true) }.take(5)
        }
    }

    fun executeHostCommand(
        commandStr: String,
        currentQuest: Quest?,
        userCode: String,
        completedQuestIds: Set<String>,
        onSelectQuest: (String) -> Unit,
        onRunTests: () -> Unit,
        onBootVm: () -> Unit,
        onAskAi: (String) -> Unit
    ): List<TerminalLine> {
        val trimmed = commandStr.trim()
        if (trimmed.isNotBlank()) {
            executionHistory.add(trimmed)
        }
        return dispatcher.execute(
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
