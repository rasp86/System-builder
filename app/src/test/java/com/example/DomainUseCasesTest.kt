package com.example

import com.example.data.game.QuestsData
import com.example.data.game.TerminalEngine
import com.example.data.game.quest.QuestVerificationEngine
import com.example.data.game.vm.VirtualMachineEngine
import com.example.domain.usecase.BootVirtualMachineUseCase
import com.example.domain.usecase.ExecuteTerminalCommandUseCase
import com.example.domain.usecase.RunQuestTestsUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainUseCasesTest {

    @Test
    fun testRunQuestTestsUseCaseWithValidCode() {
        val useCase = RunQuestTestsUseCase(QuestVerificationEngine())
        val mbrQuest = QuestsData.allQuests.first { it.id == "Q1_1_MBR_MAGIC" }

        val (results, allPassed) = useCase.execute(mbrQuest, mbrQuest.referenceSolution)
        assertTrue(allPassed)
        assertEquals(mbrQuest.testCases.size, results.size)
        assertTrue(results.all { it.isPassed })
    }

    @Test
    fun testExecuteTerminalCommandUseCaseExecutesUptime() {
        val terminalEngine = TerminalEngine()
        val useCase = ExecuteTerminalCommandUseCase(terminalEngine)

        val output = useCase.execute(
            commandStr = "uptime",
            currentQuest = null,
            userCode = "",
            completedQuestIds = emptySet(),
            onSelectQuest = {},
            onRunTests = {},
            onBootVm = {},
            onAskAi = {}
        )

        assertNotNull(output)
        assertTrue(output.any { it.text.contains("up") })
    }

    @Test
    fun testBootVirtualMachineUseCaseGeneratesLogs() {
        val useCase = BootVirtualMachineUseCase(VirtualMachineEngine())
        val logs = useCase.generateBootSequence("GenesisOS", setOf("Q1_1_MBR_MAGIC"))

        assertTrue(logs.isNotEmpty())
        assertTrue(logs.any { it.contains("SeaBIOS") || it.contains("GenesisOS") })
    }

    @Test
    fun testBootVirtualMachineUseCaseGuestCommand() {
        val useCase = BootVirtualMachineUseCase(VirtualMachineEngine())
        val output = useCase.executeGuestCommand("help", "GenesisOS", emptySet())

        assertTrue(output.isNotEmpty())
        assertTrue(output.any { it.contains("GenesisOS") || it.contains("Polecenia") || it.contains("help") })
    }
}
