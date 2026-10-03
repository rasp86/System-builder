package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.game.Quest
import com.example.ui.theme.ConsoleBackground
import com.example.ui.theme.ConsoleBorder
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberPink
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TestResultItem
import java.util.regex.Pattern

private val ASM_MNEMONIC_PATTERN = Pattern.compile(
    "\\b(?i)(MOV|PUSH|POP|CALL|RET|JMP|JE|JNE|JZ|JNZ|JA|JB|JAE|JBE|JG|JL|JGE|JLE|CMP|TEST|XOR|AND|OR|NOT|ADD|SUB|INC|DEC|MUL|DIV|SHL|SHR|INT|CLI|STI|HLT|NOP|LGDT|LIDT|IN|OUT|LEA|REP|MOVSB|STOSB|CLD|STD|IRET|PUSHA|POPA|PUSHAD|POPAD|PUSHF|POPF|PUSHFD|POPFD)\\b"
)

private val ASM_DIRECTIVE_PATTERN = Pattern.compile(
    "\\b(?i)(DB|DW|DD|DQ|DT|RESB|RESW|RESD|EQU|TIMES|SECTION|GLOBAL|EXTERN|BITS|ORG|ALIGN)\\b"
)

private val C_KEYWORD_PATTERN = Pattern.compile(
    "\\b(void|int|char|short|long|unsigned|signed|struct|typedef|enum|union|const|static|volatile|extern|if|else|while|for|do|switch|case|default|break|continue|return|sizeof|NULL|uint8_t|uint16_t|uint32_t|uint64_t|size_t)\\b"
)

private val REGISTER_PATTERN = Pattern.compile(
    "\\b(?i)(EAX|EBX|ECX|EDX|ESI|EDI|EBP|ESP|EIP|AX|BX|CX|DX|SI|DI|BP|SP|AL|AH|BL|BH|CL|CH|DL|DH|CS|DS|ES|SS|FS|GS|CR0|CR2|CR3|CR4|EFLAGS)\\b"
)

private val MEMORY_DEREFERENCE_PATTERN = Pattern.compile("\\[[^\\]\\n]+\\]")
private val HEX_ADDRESS_PATTERN = Pattern.compile("\\b0x[0-9a-fA-F]+\\b")
private val NUMBER_PATTERN = Pattern.compile("\\b[0-9]+\\b")
private val LABEL_PATTERN = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*):")
private val STRING_PATTERN = Pattern.compile("(\"[^\"]*\"|'[^']*')")
private val COMMENT_PATTERN = Pattern.compile("(;.*|//.*)")

fun highlightSourceCode(text: String, isAsm: Boolean): AnnotatedString {
    val builder = AnnotatedString.Builder(text)

    // 1. Numbers (Decimal)
    val numMatcher = NUMBER_PATTERN.matcher(text)
    while (numMatcher.find()) {
        builder.addStyle(SpanStyle(color = CyberAmber), numMatcher.start(), numMatcher.end())
    }

    // 2. Memory Hex Addresses (e.g. 0x7C00, 0x9C000, 0xAA55)
    val hexMatcher = HEX_ADDRESS_PATTERN.matcher(text)
    while (hexMatcher.find()) {
        builder.addStyle(SpanStyle(color = CyberAmber, fontWeight = FontWeight.Bold), hexMatcher.start(), hexMatcher.end())
    }

    // 3. Memory Brackets Dereference (e.g. [gdt], [ebx+4], [esp])
    val memMatcher = MEMORY_DEREFERENCE_PATTERN.matcher(text)
    while (memMatcher.find()) {
        builder.addStyle(SpanStyle(color = CyberPurple, fontWeight = FontWeight.SemiBold), memMatcher.start(), memMatcher.end())
    }

    // 4. Assembly Directives
    if (isAsm) {
        val dirMatcher = ASM_DIRECTIVE_PATTERN.matcher(text)
        while (dirMatcher.find()) {
            builder.addStyle(SpanStyle(color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold), dirMatcher.start(), dirMatcher.end())
        }

        val labelMatcher = LABEL_PATTERN.matcher(text)
        while (labelMatcher.find()) {
            builder.addStyle(SpanStyle(color = Color(0xFFFDE047), fontWeight = FontWeight.Bold), labelMatcher.start(), labelMatcher.end())
        }
    }

    // 5. Registers (e.g. EAX, EBX, CR0, ESP)
    val regMatcher = REGISTER_PATTERN.matcher(text)
    while (regMatcher.find()) {
        builder.addStyle(SpanStyle(color = CyberCyan, fontWeight = FontWeight.Bold), regMatcher.start(), regMatcher.end())
    }

    // 6. Keywords / Mnemonics (e.g. MOV, PUSH, POP, CALL, RET, JMP, CLI, HLT)
    val kwMatcher = if (isAsm) ASM_MNEMONIC_PATTERN.matcher(text) else C_KEYWORD_PATTERN.matcher(text)
    while (kwMatcher.find()) {
        builder.addStyle(SpanStyle(color = CyberPink, fontWeight = FontWeight.Bold), kwMatcher.start(), kwMatcher.end())
    }

    // 7. Strings
    val strMatcher = STRING_PATTERN.matcher(text)
    while (strMatcher.find()) {
        builder.addStyle(SpanStyle(color = CyberGreen), strMatcher.start(), strMatcher.end())
    }

    // 8. Comments
    val commentMatcher = COMMENT_PATTERN.matcher(text)
    while (commentMatcher.find()) {
        builder.addStyle(SpanStyle(color = Color(0xFF64748B), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic), commentMatcher.start(), commentMatcher.end())
    }

    return builder.toAnnotatedString()
}

class SyntaxHighlightingTransformation(private val isAsm: Boolean) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            highlightSourceCode(text.text, isAsm),
            OffsetMapping.Identity
        )
    }
}

@Composable
fun CodeEditorView(
    modifier: Modifier = Modifier,
    quest: Quest,
    code: String,
    testResults: List<TestResultItem>,
    isCompiling: Boolean,
    onCodeChange: (String) -> Unit,
    onCompile: () -> Unit,
    onRunTests: () -> Unit,
    onResetTemplate: () -> Unit,
    onLoadSolution: () -> Unit,
    onAskAiHint: () -> Unit
) {
    val isAsm = remember(quest.filePath) { quest.filePath.endsWith(".asm") }
    val syntaxTransformation = remember(isAsm) { SyntaxHighlightingTransformation(isAsm) }
    val lineCount = remember(code) { code.lines().size.coerceAtLeast(1) }
    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()

    Column(modifier = modifier.fillMaxSize()) {
        // Editor Action Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF0E1726),
            shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, ConsoleBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = quest.filePath,
                        color = CyberCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onResetTemplate,
                        modifier = Modifier.height(30.dp).testTag("reset_code_btn"),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Reset", fontSize = 10.sp, color = TextSecondary)
                    }

                    OutlinedButton(
                        onClick = onLoadSolution,
                        modifier = Modifier.height(30.dp).testTag("load_solution_btn"),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Rozwiązanie", fontSize = 10.sp, color = CyberAmber)
                    }

                    Button(
                        onClick = onRunTests,
                        modifier = Modifier.height(30.dp).testTag("run_tests_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberGreen),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                    ) {
                        if (isCompiling) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Testuj", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Code Area (Line numbers + Text field with Syntax Highlighting)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(ConsoleBackground)
                .border(1.dp, ConsoleBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(verticalScroll)
            ) {
                // Line Numbers Gutter
                Column(
                    modifier = Modifier
                        .background(Color(0xFF090E1A))
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    for (i in 1..lineCount) {
                        Text(
                            text = i.toString().padStart(3, ' '),
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Editor Text Input with Real-time Syntax Highlighting Visual Transformation
                BasicTextField(
                    value = code,
                    onValueChange = onCodeChange,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .horizontalScroll(horizontalScroll)
                        .testTag("code_editor_input"),
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    ),
                    cursorBrush = SolidColor(CyberCyan),
                    visualTransformation = syntaxTransformation
                )
            }
        }

        // Mnemonic Instruction Chips / Quick Keywords Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0B1120))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val mnemonics = if (isAsm) {
                listOf("MOV", "PUSH", "POP", "CALL", "RET", "JMP", "XOR", "INT", "CLI", "HLT", "dw 0xAA55", "lgdt [gdt]")
            } else {
                listOf("uint32_t", "VGA_BUFFER", "CR0 |= 0x80000000", "schedule()", "regs->eax", "PAGE_PRESENT")
            }

            mnemonics.forEach { mnemonic ->
                Surface(
                    modifier = Modifier
                        .clickable {
                            onCodeChange(code + (if (code.endsWith("\n") || code.isEmpty()) "" else " ") + mnemonic)
                        }
                        .testTag("mnemonic_chip_$mnemonic"),
                    color = Color(0xFF16233B),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = mnemonic,
                        color = if (mnemonic.all { it.isUpperCase() }) CyberPink else CyberCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Test Results Panel (if available)
        if (testResults.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF0C1425),
                border = androidx.compose.foundation.BorderStroke(1.dp, ConsoleBorder)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "WYNIKI TESTÓW JEDNOSTKOWYCH (${testResults.count { it.isPassed }}/${testResults.size})",
                        color = if (testResults.all { it.isPassed }) CyberGreen else CyberAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    testResults.forEach { tr ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = if (tr.isPassed) "✔ PASS" else "✖ FAIL",
                                color = if (tr.isPassed) CyberGreen else CyberPink,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${tr.title} — ${tr.message}",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
