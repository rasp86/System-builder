package com.example

import com.example.game.vm.BootSequenceGenerator
import com.example.game.vm.BootStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BootSequenceGeneratorTest {

    @Test
    fun testBootSequenceWithoutMbrCausesPanic() {
        val generator = BootSequenceGenerator()
        val result = generator.generateBootSequence("GenesisOS", completedQuestIds = emptySet())

        assertTrue(result.hasPanic)
        assertNotNull(result.failedStage)
        assertEquals("mbr", result.failedStage?.id)
        assertTrue(result.logs.any { it.contains("FATAL BOOT ERROR") || it.contains("0xAA55") })
    }

    @Test
    fun testBootSequenceWithMbrOnlyFailsAtGdt() {
        val generator = BootSequenceGenerator()
        val result = generator.generateBootSequence("GenesisOS", completedQuestIds = setOf("Q1_1_MBR_MAGIC"))

        assertTrue(result.hasPanic)
        assertNotNull(result.failedStage)
        assertEquals("gdt", result.failedStage?.id)
        assertTrue(result.logs.any { it.contains("PANIC") })
    }

    @Test
    fun testFullBootSequenceSucceedsWhenAllRequiredCompleted() {
        val generator = BootSequenceGenerator()
        val allRequired = setOf(
            "Q1_1_MBR_MAGIC",
            "Q1_2_GDT_PROTECTED_MODE",
            "Q3_1_IDT_SETUP",
            "Q2_1_PAGING_INIT"
        )
        val result = generator.generateBootSequence("GenesisOS", completedQuestIds = allRequired)

        assertFalse(result.hasPanic)
        assertNull(result.failedStage)
        assertTrue(result.logs.any { it.contains("Booted Successfully") })
    }

    @Test
    fun testCustomBootStages() {
        val customStages = listOf(
            BootStage("stage1", "Custom Stage 1", "Log 1", null),
            BootStage("stage2", "Custom Stage 2", "Log 2 for {osName}", "REQ_1", isCriticalFailure = true)
        )
        val generator = BootSequenceGenerator(customStages)

        val failed = generator.generateBootSequence("MyCustomOS", emptySet())
        assertTrue(failed.hasPanic)
        assertEquals("stage2", failed.failedStage?.id)

        val success = generator.generateBootSequence("MyCustomOS", setOf("REQ_1"))
        assertFalse(success.hasPanic)
        assertTrue(success.logs.contains("Log 2 for MyCustomOS"))
    }
}
