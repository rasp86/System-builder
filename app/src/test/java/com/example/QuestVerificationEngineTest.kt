package com.example

import com.example.data.game.QuestsData
import com.example.data.game.quest.QuestVerificationEngine
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestVerificationEngineTest {

    private val engine = QuestVerificationEngine()

    @Test
    fun testAllQuestsReferenceSolutionsPassValidation() {
        QuestsData.allQuests.forEach { quest ->
            val (results, allPassed) = engine.runTests(quest, quest.referenceSolution)
            assertTrue("Quest '${quest.id}' reference solution should pass all tests", allPassed)
            assertTrue("Quest '${quest.id}' results must not be empty", results.isNotEmpty())
            results.forEach { res ->
                assertTrue("Subtest '${res.title}' in quest '${quest.id}' should pass", res.isPassed)
            }
        }
    }

    @Test
    fun testInvalidMbrCodeFailsValidation() {
        val quest = QuestsData.allQuests.first { it.id == "Q1_1_MBR_MAGIC" }
        val brokenCode = """
            [BITS 16]
            [ORG 0x7C00]
            start:
                cli
                hlt
            ; Missing boot signature and padding
        """.trimIndent()

        val (results, allPassed) = engine.runTests(quest, brokenCode)
        assertFalse("Broken MBR code should fail test verification", allPassed)
        assertTrue(results.any { !it.isPassed && it.message.contains("0xAA55") })
    }

    @Test
    fun testInvalidPagingCodeFailsValidation() {
        val quest = QuestsData.allQuests.first { it.id == "Q3_1_PAGING_MMU" }
        val brokenCode = """
            void paging_init(void) {
                // missing CR0 and CR3 operations
            }
        """.trimIndent()

        val (results, allPassed) = engine.runTests(quest, brokenCode)
        assertFalse("Broken Paging code should fail validation", allPassed)
        assertTrue(results.any { !it.isPassed })
    }
}
