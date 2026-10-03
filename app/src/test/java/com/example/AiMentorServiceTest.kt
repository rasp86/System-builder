package com.example

import com.example.data.ai.AiMentorService
import com.example.data.ai.AiSourceType
import com.example.data.ai.GeminiAiMentorService
import com.example.data.ai.GeminiRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiMentorServiceTest {

    private val geminiRepo = GeminiRepository()
    private val aiService: AiMentorService = GeminiAiMentorService(geminiRepo)

    @Test
    fun testOfflineReasoningGeneratesGdtArchitectureExplanations() = runBlocking {
        val result = geminiRepo.generateOfflineArchitectResponse(
            prompt = "Jak skonfigurować GDT i przejść do 32-bit Protected Mode?",
            questContext = "GDT & Protected Mode (Bootloader ASM)",
            codeContext = "lgdt [gdt_descriptor]"
        )

        assertNotNull(result)
        assertEquals(AiSourceType.OFFLINE_REASONING, result.sourceType)
        assertTrue(result.replyText.contains("GDT") || result.replyText.contains("Protected Mode"))
        assertTrue(result.replyText.contains("CR0"))
        assertTrue(result.reasoningSteps.isNotEmpty())
    }

    @Test
    fun testOfflineReasoningGeneratesIdtAndInterruptExplanations() = runBlocking {
        val result = geminiRepo.generateOfflineArchitectResponse(
            prompt = "Wyjaśnij strukturę wpisu IDT i remapowanie PIC 8259",
            questContext = "IDT Interrupts (Kernel Core)",
            codeContext = "void idt_set_gate(uint8_t num, uint32_t base, uint16_t sel, uint8_t flags)"
        )

        assertNotNull(result)
        assertTrue(result.replyText.contains("IDT") || result.replyText.contains("przerwań"))
        assertEquals(AiSourceType.OFFLINE_REASONING, result.sourceType)
    }

    @Test
    fun testOfflineReasoningGeneratesPagingExplanations() = runBlocking {
        val result = geminiRepo.generateOfflineArchitectResponse(
            prompt = "Czym jest dwupoziomowe stronicowanie i jak zainicjalizować CR3?",
            questContext = "Paging MMU (Memory Subsystem)",
            codeContext = "mov cr3, eax"
        )

        assertNotNull(result)
        assertTrue(result.replyText.contains("Paging") || result.replyText.contains("Stronicowanie") || result.replyText.contains("CR3"))
        assertEquals(AiSourceType.OFFLINE_REASONING, result.sourceType)
    }

    @Test
    fun testAiMentorServiceFallbackWhenApiKeyUnconfigured() = runBlocking {
        val result = aiService.askMentor(
            userPrompt = "Jak działa planista Round-Robin?",
            questContext = "Scheduler (Multitasking)",
            codeContext = "void schedule(void)"
        )

        assertNotNull(result)
        assertTrue(result.replyText.isNotBlank())
        assertTrue(result.reasoningSteps.isNotEmpty())
    }
}
