package com.example

import com.example.data.game.TerminalEngine
import com.example.game.terminal.CommandContext
import com.example.game.terminal.DefaultTerminalCommandRegistry
import com.example.game.terminal.TerminalCommand
import com.example.game.terminal.TerminalCommandRegistry
import com.example.data.game.TerminalLine
import com.example.data.game.TerminalLineType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TerminalCommandRegistryTest {

    @Test
    fun testDefaultRegistryContainsStandardCommands() {
        val registry = DefaultTerminalCommandRegistry.create()
        assertTrue(registry.hasCommand("help"))
        assertTrue(registry.hasCommand("ls"))
        assertTrue(registry.hasCommand("cat"))
        assertTrue(registry.hasCommand("ps"))
        assertTrue(registry.hasCommand("uptime"))
        assertTrue(registry.hasCommand("quests"))
        assertTrue(registry.hasCommand("theme"))
        assertTrue(registry.hasCommand("man"))
    }

    @Test
    fun testCustomCommandRegistrationAndExecution() {
        val registry = TerminalCommandRegistry()
        val customCmd = object : TerminalCommand {
            override val name = "testcmd"
            override val aliases = listOf("tcmd")
            override val description = "Test command"

            override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
                return listOf(TerminalLine("Output: ${args.joinToString(",")}", TerminalLineType.OUTPUT))
            }
        }

        registry.register(customCmd)
        assertTrue(registry.hasCommand("testcmd"))
        assertTrue(registry.hasCommand("tcmd"))
        assertEquals("Test command", registry.getCommand("testcmd")?.description)

        val engine = TerminalEngine()
        val context = CommandContext(
            commandStr = "testcmd 1 2 3",
            args = listOf("1", "2", "3"),
            currentQuest = null,
            userCode = "",
            completedQuestIds = emptySet(),
            vfsManager = engine.vfsManager,
            cpuSimulator = engine.cpuSimulator,
            processScheduler = engine.processScheduler,
            dmesgBuffer = engine.dmesgBuffer,
            getBootTimeMillis = { 0L },
            getThemeId = { "amber" },
            setThemeId = {},
            getSnapshotRepo = { null },
            getHistory = { emptyList() },
            onSelectQuest = {},
            onRunTests = {},
            onBootVm = {},
            onAskAi = {}
        )

        val result = registry.execute("testcmd", listOf("1", "2", "3"), context)
        assertNotNull(result)
        assertEquals(1, result?.size)
        assertEquals("Output: 1,2,3", result?.first()?.text)
    }

    @Test
    fun testNonExistentCommandReturnsNull() {
        val registry = DefaultTerminalCommandRegistry.create()
        assertFalse(registry.hasCommand("nonexistent_command_xyz"))
    }
}
