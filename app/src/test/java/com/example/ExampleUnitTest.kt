package com.example

import androidx.compose.ui.graphics.Color
import com.example.data.game.FsNodeType
import com.example.data.game.OsSimulationEngine
import com.example.data.game.QuestsData
import com.example.data.game.TerminalEngine
import com.example.ui.screens.highlightTerminalKeywords
import com.example.ui.theme.TerminalThemeId
import com.example.ui.theme.TerminalThemes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun testVirtualFileSystemStructure() {
        val engine = TerminalEngine()
        val vfs = engine.virtualFileSystem
        assertTrue(vfs.containsKey("/"))
        assertTrue(vfs.containsKey("/boot"))
        assertTrue(vfs.containsKey("/boot/boot.asm"))
        assertTrue(vfs.containsKey("/etc/os-release"))
        assertTrue(vfs.containsKey("/proc/cpuinfo"))
        assertEquals(FsNodeType.DIRECTORY, vfs["/boot"]?.type)
        assertEquals(FsNodeType.FILE, vfs["/boot/boot.asm"]?.type)
    }

    @Test
    fun testUptimeCommandCalculatesSystemSession() {
        val engine = TerminalEngine()
        val uptimeLines = engine.executeHostCommand("uptime", null, "", emptySet(), {}, {}, {}, {})
        assertTrue(uptimeLines.any { it.text.contains("up") && it.text.contains("load average") })
        assertTrue(uptimeLines.any { it.text.contains("Hardware: x86") && it.text.contains("CPU Cycles") })
    }

    @Test
    fun testChmodCommandUpdatesFilePermissions() {
        val engine = TerminalEngine()
        // Check initial permission
        assertEquals("-rw-r--r--", engine.virtualFileSystem["/boot/boot.asm"]?.permissions)

        // Change to 755
        val chmodLines755 = engine.executeHostCommand("chmod 755 /boot/boot.asm", null, "", emptySet(), {}, {}, {}, {})
        assertTrue(chmodLines755.any { it.text.contains("Zmieniono uprawnienia") })
        assertEquals("-rwxr-xr-x", engine.virtualFileSystem["/boot/boot.asm"]?.permissions)

        // Change to 644
        engine.executeHostCommand("chmod 644 /boot/boot.asm", null, "", emptySet(), {}, {}, {}, {})
        assertEquals("-rw-r--r--", engine.virtualFileSystem["/boot/boot.asm"]?.permissions)

        // Symbolic +x
        engine.executeHostCommand("chmod +x /kernel/vga.c", null, "", emptySet(), {}, {}, {}, {})
        assertTrue(engine.virtualFileSystem["/kernel/vga.c"]?.permissions?.contains("x") == true)
    }

    @Test
    fun testDfCommandReportsDiskSpaceStatistics() {
        val engine = TerminalEngine()
        val dfLines = engine.executeHostCommand("df", null, "", emptySet(), {}, {}, {}, {})
        assertTrue(dfLines.any { it.text.contains("System plików") && it.text.contains("Rozmiar") })
        assertTrue(dfLines.any { it.text.contains("/dev/ram0") && it.text.contains("64M") })
        assertTrue(dfLines.any { it.text.contains("i-węzłów aktywnych") })
    }

    @Test
    fun testPsCommandProcessStatusListing() {
        val engine = TerminalEngine()
        val psLines = engine.executeHostCommand("ps", null, "", emptySet(), {}, {}, {}, {})
        assertTrue(psLines.any { it.text.contains("PROCESS TABLE") || it.text.contains("ps aux") })
        assertTrue(psLines.any { it.text.contains("PID") && it.text.contains("USER") && it.text.contains("STAT") && it.text.contains("RSS") })
        assertTrue(psLines.any { it.text.contains("/sbin/init") || it.text.contains("qemu-system-i386") || it.text.contains("gsh") })
        assertTrue(psLines.any { it.text.contains("Procesy:") })
        assertTrue(psLines.any { it.text.contains("Planista") })
    }

    @Test
    fun testDmesgCommandKernelMessageBuffer() {
        val engine = TerminalEngine()
        val dmesgLines = engine.executeHostCommand("dmesg", null, "", emptySet(), {}, {}, {}, {})
        assertTrue(dmesgLines.any { it.text.contains("GENESIS KERNEL RING BUFFER") })
        assertTrue(dmesgLines.any { it.text.contains("x86/cpu") || it.text.contains("bios/e820") || it.text.contains("scheduler") })
        assertTrue(dmesgLines.any { it.text.contains("Łącznie wpisów w buforze") })

        // Test dmesg -k
        val dmesgKernel = engine.executeHostCommand("dmesg -k", null, "", emptySet(), {}, {}, {}, {})
        assertTrue(dmesgKernel.isNotEmpty())
    }

    @Test
    fun testToggleTerminalColorSchemes() {
        val engine = TerminalEngine()
        // Test list themes
        val themeHelp = engine.executeHostCommand("theme", null, "", emptySet(), {}, {}, {}, {})
        assertTrue(themeHelp.any { it.text.contains("SCHEMATY KOLORYSTYCZNE") })
        assertTrue(themeHelp.any { it.text.contains("Retro Green") })
        assertTrue(themeHelp.any { it.text.contains("Classic Amber") })
        assertTrue(themeHelp.any { it.text.contains("Monochrome") })

        // Test switch to Retro Green
        val setGreen = engine.executeHostCommand("theme green", null, "", emptySet(), {}, {}, {}, {})
        assertTrue(setGreen.any { it.text.contains("Zmieniono schemat") })
        assertEquals("RETRO_GREEN", engine.currentThemeId)
        val greenPalette = TerminalThemes.getPaletteById(engine.currentThemeId)
        assertEquals(TerminalThemeId.RETRO_GREEN, greenPalette.id)

        // Test switch to Classic Amber
        engine.executeHostCommand("theme amber", null, "", emptySet(), {}, {}, {}, {})
        assertEquals("CLASSIC_AMBER", engine.currentThemeId)

        // Test switch to Monochrome
        engine.executeHostCommand("theme mono", null, "", emptySet(), {}, {}, {}, {})
        assertEquals("MONOCHROME", engine.currentThemeId)

        // Test switch to Solarized Dark
        engine.executeHostCommand("theme solarized", null, "", emptySet(), {}, {}, {}, {})
        assertEquals("SOLARIZED_DARK", engine.currentThemeId)
    }

    @Test
    fun testGrepCommandSearch() {
        val engine = TerminalEngine()
        // Test grep in specific file
        val grepFile = engine.executeHostCommand("grep start /boot/boot.asm", null, "", emptySet(), {}, {}, {}, {})
        assertTrue(grepFile.any { it.text.contains("start:") || it.text.contains("Znaleziono") })

        // Test grep across all files
        val grepAll = engine.executeHostCommand("grep GenesisOS", null, "", emptySet(), {}, {}, {}, {})
        assertTrue(grepAll.any { it.text.contains("os-release") || it.text.contains("boot.asm") })
    }

    @Test
    fun testBenchmarkCommandPerformanceReport() {
        val engine = TerminalEngine()
        val benchLines = engine.executeHostCommand("benchmark", null, "", emptySet(), {}, {}, {}, {})
        assertTrue(benchLines.any { it.text.contains("GENESIS HARDWARE PERFORMANCE") })
        assertTrue(benchLines.any { it.text.contains("GENESIS-MIPS") })
        assertTrue(benchLines.any { it.text.contains("Integer ALU") })
        assertTrue(engine.dmesgEntries.any { it.subsystem == "cpu/bench" })
    }

    @Test
    fun testTabCompletionForCommandsAndPaths() {
        val engine = TerminalEngine()
        // Command tab completion
        val complGre = engine.getTabCompletions("gre")
        assertTrue(complGre.contains("grep"))

        val complB = engine.getTabCompletions("bench")
        assertTrue(complB.contains("benchmark"))

        val complUptime = engine.getTabCompletions("upt")
        assertTrue(complUptime.contains("uptime"))

        val complChmod = engine.getTabCompletions("chm")
        assertTrue(complChmod.contains("chmod"))

        val complDf = engine.getTabCompletions("d")
        assertTrue(complDf.contains("df"))

        val complTheme = engine.getTabCompletions("the")
        assertTrue(complTheme.contains("theme"))

        val complThemeVal = engine.getTabCompletions("theme gr")
        assertTrue(complThemeVal.contains("green"))

        // Path tab completion
        val complPath = engine.getTabCompletions("cat /boot/b")
        assertTrue(complPath.contains("/boot/boot.asm"))
    }

    @Test
    fun testSyntaxHighlightingEngineLabelsCommentsOpcodes() {
        val codeLine = "start:   mov eax, 0x7C00 ; initialize boot sector stack"
        val highlighted = highlightTerminalKeywords(codeLine, Color.White, TerminalThemes.CyberMatrix)
        assertEquals(codeLine, highlighted.text)
        assertTrue(highlighted.spanStyles.size >= 3)
    }

    @Test
    fun testMkdirAndRmCommands() {
        val engine = TerminalEngine()
        val mkdirLines = engine.executeHostCommand("mkdir /src", null, "", emptySet(), {}, {}, {}, {})
        assertTrue(mkdirLines.any { it.text.contains("Utworzono katalog") })
        assertTrue(engine.virtualFileSystem.containsKey("/src"))
        assertEquals(FsNodeType.DIRECTORY, engine.virtualFileSystem["/src"]?.type)

        engine.executeHostCommand("touch /src/main.c", null, "", emptySet(), {}, {}, {}, {})
        assertTrue(engine.virtualFileSystem.containsKey("/src/main.c"))

        val rmLines = engine.executeHostCommand("rm /src/main.c", null, "", emptySet(), {}, {}, {}, {})
        assertTrue(rmLines.any { it.text.contains("Usunięto plik") })
        assertFalse(engine.virtualFileSystem.containsKey("/src/main.c"))

        val rmDirLines = engine.executeHostCommand("rm -r /src", null, "", emptySet(), {}, {}, {}, {})
        assertTrue(rmDirLines.any { it.text.contains("Usunięto") })
        assertFalse(engine.virtualFileSystem.containsKey("/src"))
    }

    @Test
    fun testHistoryCommandLast20() {
        val engine = TerminalEngine()
        for (i in 1..25) {
            engine.executeHostCommand("echo test$i", null, "", emptySet(), {}, {}, {}, {})
        }
        val historyLines = engine.executeHostCommand("history", null, "", emptySet(), {}, {}, {}, {})
        assertTrue(historyLines.any { it.text.contains("HISTORIA OSTATNICH 20 POLECEŃ") })
        assertTrue(historyLines.any { it.text.contains("echo test25") })
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
    fun testAliasCommandCreatesShortcutAndPersistsToVfsConfiguration() {
        val engine = TerminalEngine()
        // 1. Verify /etc/aliases.cfg is initialized in VFS
        assertTrue(engine.virtualFileSystem.containsKey("/etc/aliases.cfg"))
        val initialCfg = engine.virtualFileSystem["/etc/aliases.cfg"]?.content ?: ""
        assertTrue(initialCfg.contains("alias c='clear'"))
        assertTrue(initialCfg.contains("alias b='build'"))

        // 2. Define a new alias
        val aliasOut = engine.executeHostCommand("alias k9='kill -9'", null, "", emptySet(), {}, {}, {}, {})
        assertTrue(aliasOut.any { it.text.contains("Zdefiniowano nowy alias") && it.text.contains("k9") })
        assertEquals("kill -9", engine.customAliases["k9"])

        // 3. Verify it was written to /etc/aliases.cfg in VFS
        val updatedCfg = engine.virtualFileSystem["/etc/aliases.cfg"]?.content ?: ""
        assertTrue(updatedCfg.contains("alias k9='kill -9'"))

        // 4. Test unalias removes the shortcut and updates VFS config
        val unaliasOut = engine.executeHostCommand("unalias k9", null, "", emptySet(), {}, {}, {}, {})
        assertTrue(unaliasOut.any { it.text.contains("Usunięto alias 'k9'") })
        assertFalse(engine.customAliases.containsKey("k9"))
        val cfgAfterUnalias = engine.virtualFileSystem["/etc/aliases.cfg"]?.content ?: ""
        assertFalse(cfgAfterUnalias.contains("alias k9="))
    }

    @Test
    fun testClearCommandResetsTerminalBuffer() {
        val engine = TerminalEngine()
        val clearResult = engine.executeHostCommand("clear", null, "", emptySet(), {}, {}, {}, {})
        assertTrue(clearResult.isEmpty())

        val clsResult = engine.executeHostCommand("cls", null, "", emptySet(), {}, {}, {}, {})
        assertTrue(clsResult.isEmpty())
    }

    @Test
    fun testKillCommandTerminatesProcessAndUpdatesProcessTable() {
        val engine = TerminalEngine()
        // 1. Verify PID 108 is running
        assertTrue(engine.backgroundProcesses.any { it.pid == 108 })

        // 2. Kill PID 108
        val killOut = engine.executeHostCommand("kill -9 108", null, "", emptySet(), {}, {}, {}, {})
        assertTrue(killOut.any { it.text.contains("został pomyślnie zakończony") || it.text.contains("SIGKILL") })

        // 3. Verify PID 108 is removed from backgroundProcesses
        assertFalse(engine.backgroundProcesses.any { it.pid == 108 })

        // 4. Verify dmesg logged the kill event
        assertTrue(engine.dmesgEntries.any { it.subsystem == "scheduler/kill" && it.message.contains("108") })

        // 5. Verify ps output no longer includes PID 108
        val psOut = engine.executeHostCommand("ps", null, "", emptySet(), {}, {}, {}, {})
        assertFalse(psOut.any { it.text.contains("108") && it.text.contains("qemu-system-i386") })

        // 6. Test Kernel Ring 0 Protection on PID 1 (init)
        val killInitOut = engine.executeHostCommand("kill 1", null, "", emptySet(), {}, {}, {}, {})
        assertTrue(killInitOut.any { it.text.contains("Operacja zabroniona") || it.text.contains("Ochrona jądra") })
        assertTrue(engine.backgroundProcesses.any { it.pid == 1 }) // Init is preserved
    }
}
