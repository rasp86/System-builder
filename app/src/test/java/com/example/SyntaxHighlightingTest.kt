package com.example

import com.example.ui.components.highlightSourceCode
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SyntaxHighlightingTest {

    @Test
    fun testAsmMnemonicsAndRegistersHighlighting() {
        val asmCode = """
            mov eax, cr0
            or eax, 1
            mov cr0, eax
            push eax
            pop ebx
            lgdt [gdt_descriptor]
            mov [0x7C00], eax
            int 0x10
            cli
            hlt
        """.trimIndent()

        val annotated = highlightSourceCode(asmCode, isAsm = true)
        assertNotNull(annotated)
        assertTrue(annotated.text.contains("mov"))
        // Check multiple styled spans for mnemonics, registers, memory brackets, and hex addresses
        assertTrue(annotated.spanStyles.size >= 8)
    }

    @Test
    fun testCKeywordsHighlighting() {
        val cCode = """
            #include <stdint.h>
            void kernel_main() {
                uint32_t cr0 = 0x80000000;
                if (cr0 != NULL) {
                    return;
                }
            }
        """.trimIndent()

        val annotated = highlightSourceCode(cCode, isAsm = false)
        assertNotNull(annotated)
        assertTrue(annotated.spanStyles.isNotEmpty())
    }
}
