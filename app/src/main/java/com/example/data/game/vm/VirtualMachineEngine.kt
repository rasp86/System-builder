package com.example.data.game.vm

import com.example.data.game.OSBootStatus
import com.example.data.game.OsSimulationEngine
import com.example.data.game.Quest
import com.example.data.game.QuestsData
import com.example.data.game.TerminalLine
import com.example.data.game.TerminalLineType
import kotlinx.coroutines.delay

/**
 * Handles virtual machine lifecycle, QEMU boot sequence generation, and guest OS terminal emulation.
 */
class VirtualMachineEngine {

    suspend fun runBootSequence(
        activeQuestId: String,
        onStatusChange: (OSBootStatus) -> Unit,
        onLogAdded: (String) -> Unit
    ): Pair<OSBootStatus, List<String>> {
        val logs = mutableListOf<String>()
        onStatusChange(OSBootStatus.KERNEL_BOOTING)

        fun log(msg: String) {
            logs.add(msg)
            onLogAdded(msg)
        }

        log("[SeaBIOS 1.15.0] Initializing hardware devices...")
        delay(250)
        log("[SeaBIOS] Searching for bootable media on IDE 0:0...")
        delay(250)
        log("[SeaBIOS] Reading MBR sector at 0x7C00 (512 bytes)...")
        delay(300)

        val currentQuest = QuestsData.allQuests.find { it.id == activeQuestId }
        val finalStatus = if (currentQuest?.id == "Q1_1_MBR_MAGIC" || currentQuest?.phase == 1) {
            log("[SeaBIOS] ✔ Magic boot signature 0xAA55 found at offset 510!")
            log("[CPU IA-32] Jumping to 0x0000:0x7C00 (Real Mode)...")
            delay(200)
            log(">>> [GenesisOS Bootloader] Loading Kernel descriptors...")
            OSBootStatus.RUNNING_CLI
        } else {
            log("[SeaBIOS] ✔ Magic boot signature 0xAA55 found.")
            log("[GDT] Loading 32-bit Protected Mode descriptors...")
            log("[PAGING] CR3 initialized with Page Directory at 0x9C000...")
            log("[KERNEL] Jumping to 32-bit kernel entry point...")
            log("[VGA] Framebuffer active at 0xB8000.")
            log(">>> [GenesisOS Kernel] System Online & Multi-tasking active.")
            OSBootStatus.RUNNING_CLI
        }

        onStatusChange(finalStatus)
        return finalStatus to logs
    }

    fun executeGuestCommand(
        input: String,
        osName: String,
        completedQuestIds: Set<String>
    ): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        lines.add(TerminalLine("$osName# $input", TerminalLineType.INPUT))

        val output = OsSimulationEngine.executeGuestOsCommand(
            command = input,
            osName = osName,
            completedQuestIds = completedQuestIds
        )

        output.forEach { out ->
            val type = when {
                out.startsWith("[ERROR]") || out.contains("failed", ignoreCase = true) -> TerminalLineType.ERROR
                out.startsWith("[OK]") || out.contains("GenesisOS", ignoreCase = true) -> TerminalLineType.SUCCESS
                out.startsWith("====") -> TerminalLineType.HEADER
                else -> TerminalLineType.OUTPUT
            }
            lines.add(TerminalLine(out, type))
        }
        return lines
    }
}
