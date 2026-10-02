package com.example

import androidx.compose.ui.graphics.Color
import com.example.data.game.OsSimulationEngine
import com.example.data.game.QuestsData
import com.example.data.game.TerminalEngine
import com.example.ui.screens.highlightTerminalKeywords
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testAllQuestsAreLoadedAndValid() {
        assertEquals(12, QuestsData.allQuests.size)
        QuestsData.allQuests.forEach { quest ->
            assertNotNull(quest.id)
            assertNotNull(quest.title)
            assertNotNull(quest.filePath)
            assertTrue(quest.testCases.isNotEmpty())
            assertTrue(quest.xpReward > 0)
        }
    }

    @Test
    fun testMbrBootloaderReferenceSolutionPasses() {
        val mbrQuest = QuestsData.allQuests.find { it.id == "Q1_1_MBR_MAGIC" }
        assertNotNull(mbrQuest)
        mbrQuest?.let { q ->
            q.testCases.forEach { tc ->
                val (passed, message) = tc.check(q.referenceSolution)
                assertTrue("Test failed: ${tc.description} - $message", passed)
            }
        }
    }

    @Test
    fun testPagingReferenceSolutionPasses() {
        val pagingQuest = QuestsData.allQuests.find { it.id == "Q3_1_PAGING_MMU" }
        assertNotNull(pagingQuest)
        pagingQuest?.let { q ->
            q.testCases.forEach { tc ->
                val (passed, message) = tc.check(q.referenceSolution)
                assertTrue("Test failed: ${tc.description} - $message", passed)
            }
        }
    }

    @Test
    fun testGuestOsSimulationCommands() {
        val out = OsSimulationEngine.executeGuestOsCommand(
            command = "uname",
            osName = "GenesisOS",
            completedQuestIds = setOf("Q1_1_MBR_MAGIC", "Q1_2_GDT_PROTECTED_MODE")
        )
        assertTrue(out.any { it.contains("GenesisOS") })
    }

    @Test
    fun testHostTerminalEngineHelpAndWhoami() {
        val engine = TerminalEngine()
        val helpLines = engine.executeHostCommand(
            commandStr = "help",
            currentQuest = QuestsData.allQuests.first(),
            userCode = "",
            completedQuestIds = emptySet(),
            onSelectQuest = {},
            onRunTests = {},
            onBootVm = {},
            onAskAi = {}
        )
        assertTrue(helpLines.any { it.text.contains("GENESIS OS ARCHITECT") })

        val whoamiLines = engine.executeHostCommand(
            commandStr = "whoami",
            currentQuest = QuestsData.allQuests.first(),
            userCode = "",
            completedQuestIds = emptySet(),
            onSelectQuest = {},
            onRunTests = {},
            onBootVm = {},
            onAskAi = {}
        )
        assertTrue(whoamiLines.any { it.text.contains("Ring 0") || it.text.contains("root") })
    }

    @Test
    fun testDmesgLogBuffer() {
        val engine = TerminalEngine()
        val dmesgLines = engine.executeHostCommand("dmesg", null, "", emptySet(), {}, {}, {}, {})
        assertTrue(dmesgLines.any { it.text.contains("GENESIS KERNEL RING BUFFER") })
        assertTrue(dmesgLines.any { it.text.contains("Page Directory initialized") || it.text.contains("kernel") })
    }

    @Test
    fun testCpuCycleTrackerInAsm() {
        val engine = TerminalEngine()
        val initialCycles = engine.cpuHardwareState.totalCycles

        // Assemble 35-cycle instruction INT 0x80
        val (opcodesInt, _, cyclesInt) = engine.assembleInstruction("int 0x80")
        assertEquals(35, cyclesInt)

        engine.executeHostCommand("asm 0x7C00 int 0x80; nop", null, "", emptySet(), {}, {}, {}, {})
        assertTrue(engine.cpuHardwareState.totalCycles > initialCycles)
        assertEquals(36L, engine.cpuHardwareState.totalCycles - initialCycles)
    }

    @Test
    fun testMemViewAndHexManipulationCommands() {
        val engine = TerminalEngine()
        // Test visual mem dump
        val memLines = engine.executeHostCommand("mem", null, "", emptySet(), {}, {}, {}, {})
        assertTrue(memLines.any { it.text.contains("VIRTUAL MEMORY & REGISTER MAP") })
        assertTrue(memLines.any { it.text.contains("0x00007C00") })

        // Test direct memory byte modification
        engine.setMemoryByte(0x7C00L, 0, 0x90.toByte())
        assertEquals(0x90.toByte(), engine.getMemoryMap()[0x7C00L]?.get(0))

        // Test mem set hex bytes
        val setLines = engine.executeHostCommand("mem set 0x7C00 AA 55 90", null, "", emptySet(), {}, {}, {}, {})
        assertTrue(setLines.any { it.text.contains("Zapisano pomyślnie") })

        // Test mem reg set
        val regLines = engine.executeHostCommand("mem reg eax 0x00001337", null, "", emptySet(), {}, {}, {}, {})
        assertTrue(regLines.any { it.text.contains("EAX") })
        assertEquals("0x00001337", engine.cpuRegisters.eax)
    }

    @Test
    fun testTabCompletionHelper() {
        val engine = TerminalEngine()
        val completionsForB = engine.getTabCompletions("b")
        assertTrue(completionsForB.contains("build") || completionsForB.contains("boot"))

        val completionsForD = engine.getTabCompletions("dm")
        assertTrue(completionsForD.contains("dmesg"))
    }

    @Test
    fun testKeywordHighlighting() {
        val text = "[   0.001420] [ x86/cpu ] Intel CPU online with success at 0x00100000"
        val annotated = highlightTerminalKeywords(text, Color.White)
        assertEquals(text, annotated.text)
        assertTrue(annotated.spanStyles.isNotEmpty())
    }
}
