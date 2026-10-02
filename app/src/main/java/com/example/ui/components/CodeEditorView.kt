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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.game.Quest
import com.example.ui.theme.CodeComment
import com.example.ui.theme.CodeFunction
import com.example.ui.theme.CodeKeyword
import com.example.ui.theme.CodeNumber
import com.example.ui.theme.CodeString
import com.example.ui.theme.CodeType
import com.example.ui.theme.ConsoleBackground
import com.example.ui.theme.ConsoleBorder
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberPink
import com.example.ui.theme.TerminalCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TestResultItem

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

        // Code Area (Line numbers + Text field)
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

                // Editor Text Input
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
                    cursorBrush = SolidColor(CyberCyan)
                )
            }
        }

        // Quick Code Snippets Helper
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0B1120))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val snippets = if (quest.filePath.endsWith(".asm")) {
                listOf("dw 0xAA55", "lgdt [gdt]", "mov eax, cr0", "or eax, 1", "mov cr0, eax", "int 0x10", "cli; hlt")
            } else {
                listOf("uint32_t", "VGA_BUFFER", "CR0 |= 0x80000000", "schedule()", "regs->eax", "PAGE_PRESENT")
            }

            snippets.forEach { snip ->
                Surface(
                    modifier = Modifier
                        .clickable {
                            onCodeChange(code + (if (code.endsWith("\n") || code.isEmpty()) "" else " ") + snip)
                        },
                    color = Color(0xFF16233B),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = snip,
                        color = CyberCyan,
                        fontSize = 10.sp,
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
