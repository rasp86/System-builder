package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardTab
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.game.TerminalEngine
import com.example.data.game.TerminalLine
import com.example.data.game.TerminalLineType
import com.example.ui.components.CpuHardwareVisualizer
import com.example.ui.theme.ConsoleBackground
import com.example.ui.theme.ConsoleBorder
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDark
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberPink
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.regex.Pattern

/**
 * Builds an AnnotatedString with dynamic color highlights for keywords:
 * 'error', 'failed', 'panic', 'success', 'pass', 'ok', 'warning', 'warn',
 * memory hex addresses ('0x...'), registers, and hardware concepts.
 */
fun highlightTerminalKeywords(text: String, baseColor: Color): AnnotatedString {
    return buildAnnotatedString {
        append(text)

        // Highlight kernel log timestamps like [   0.001420]
        val dmesgTimePattern = Pattern.compile("\\[\\s*[0-9]+\\.[0-9]{6}\\]")
        val dmesgTimeMatcher = dmesgTimePattern.matcher(text)
        while (dmesgTimeMatcher.find()) {
            addStyle(
                style = SpanStyle(color = CyberCyan, fontWeight = FontWeight.Bold),
                start = dmesgTimeMatcher.start(),
                end = dmesgTimeMatcher.end()
            )
        }

        // Highlight error tokens
        val errorPattern = Pattern.compile("(?i)\\b(error|failed|fail|fatal|panic|exception|fault)\\b")
        val errorMatcher = errorPattern.matcher(text)
        while (errorMatcher.find()) {
            addStyle(
                style = SpanStyle(color = CyberPink, fontWeight = FontWeight.Bold),
                start = errorMatcher.start(),
                end = errorMatcher.end()
            )
        }

        // Highlight success tokens
        val successPattern = Pattern.compile("(?i)\\b(success|passed|pass|ok|done|verified|ready|online|unlocked)\\b")
        val successMatcher = successPattern.matcher(text)
        while (successMatcher.find()) {
            addStyle(
                style = SpanStyle(color = CyberGreen, fontWeight = FontWeight.Bold),
                start = successMatcher.start(),
                end = successMatcher.end()
            )
        }

        // Highlight warning tokens
        val warningPattern = Pattern.compile("(?i)\\b(warning|warn|caution|halted|blocked)\\b")
        val warningMatcher = warningPattern.matcher(text)
        while (warningMatcher.find()) {
            addStyle(
                style = SpanStyle(color = CyberAmber, fontWeight = FontWeight.Bold),
                start = warningMatcher.start(),
                end = warningMatcher.end()
            )
        }

        // Highlight memory hex addresses
        val hexPattern = Pattern.compile("\\b0x[0-9a-fA-F]+\\b")
        val hexMatcher = hexPattern.matcher(text)
        while (hexMatcher.find()) {
            addStyle(
                style = SpanStyle(color = CyberPurple, fontWeight = FontWeight.SemiBold),
                start = hexMatcher.start(),
                end = hexMatcher.end()
            )
        }

        // Highlight CPU registers
        val regPattern = Pattern.compile("(?i)\\b(eax|ebx|ecx|edx|esp|ebp|eip|cr0|cr3|eflags|cs|ds)\\b")
        val regMatcher = regPattern.matcher(text)
        while (regMatcher.find()) {
            addStyle(
                style = SpanStyle(color = CyberAmber, fontWeight = FontWeight.Bold),
                start = regMatcher.start(),
                end = regMatcher.end()
            )
        }

        // Highlight kernel & hardware keywords
        val systemPattern = Pattern.compile("(?i)\\b(kernel|cpu|mmu|gdt|idt|pic|pit|vga|vfs|syscall|qemu|x86|ring0|ring3|round-robin|protected mode)\\b")
        val systemMatcher = systemPattern.matcher(text)
        while (systemMatcher.find()) {
            addStyle(
                style = SpanStyle(color = CyberCyan, fontWeight = FontWeight.SemiBold),
                start = systemMatcher.start(),
                end = systemMatcher.end()
            )
        }
    }
}

/**
 * TerminalScreen composable mimicking a command-line interface (CLI)
 * with CPU clockspeed/cycle visualizer, 'dmesg' kernel log viewer, 'man' manual pages,
 * interactive memory cell grid editing, simulated assembler 'asm', and dynamic syntax coloring.
 */
@Composable
fun TerminalScreen(
    modifier: Modifier = Modifier,
    title: String = "DEVELOPER HOST SHELL (tty1)",
    lines: List<TerminalLine>,
    promptPrefix: String = "dev@genesis-os:~$ ",
    scanlinesEnabled: Boolean = true,
    onExecuteCommand: (String) -> Unit
) {
    val terminalEngine = remember { TerminalEngine() }
    var currentInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val commandHistory = remember { mutableStateListOf<String>() }
    var historyIndex by remember { mutableStateOf(-1) }

    // Interactive Memory Grid & Hex Cell Editor State
    var showMemoryGridDialog by remember { mutableStateOf(false) }
    var editingAddress by remember { mutableStateOf<Long?>(null) }
    var editingByteIndex by remember { mutableStateOf(0) }
    var editingHexInput by remember { mutableStateOf("AA") }
    var memoryVersion by remember { mutableStateOf(0) }

    // Man Manual Pages Viewer State
    var showManModal by remember { mutableStateOf(false) }
    var selectedManPage by remember { mutableStateOf("man") }

    // Dynamic Tab Completion Suggestions based on current input text
    val tabSuggestions by remember(currentInput) {
        derivedStateOf {
            terminalEngine.getTabCompletions(currentInput)
        }
    }

    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) {
            listState.animateScrollToItem(lines.size - 1)
        }
    }

    fun applyCompletion(completion: String) {
        val trimmed = currentInput.trimStart()
        val parts = trimmed.split("\\s+".toRegex()).toMutableList()

        if (parts.size <= 1) {
            currentInput = "$completion "
        } else {
            parts[parts.size - 1] = completion
            currentInput = parts.joinToString(" ") + " "
        }
    }

    fun navigateHistoryUp() {
        if (commandHistory.isNotEmpty()) {
            historyIndex = (historyIndex + 1).coerceAtMost(commandHistory.size - 1)
            currentInput = commandHistory[commandHistory.size - 1 - historyIndex]
        }
    }

    fun navigateHistoryDown() {
        if (historyIndex > 0) {
            historyIndex--
            currentInput = commandHistory[commandHistory.size - 1 - historyIndex]
        } else if (historyIndex == 0) {
            historyIndex = -1
            currentInput = ""
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ConsoleBackground)
            .then(
                if (scanlinesEnabled) {
                    Modifier.drawWithContent {
                        drawContent()
                        val lineHeight = 4.dp.toPx()
                        var y = 0f
                        while (y < size.height) {
                            drawLine(
                                color = Color(0x12000000),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.5f
                            )
                            y += lineHeight
                        }
                    }
                } else Modifier
            )
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(6.dp)) {
            // Top Window Bar with retro terminal aesthetic & Tools (DMESG, MAN, MEM GRID, CLEAR)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF0D1527),
                shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ConsoleBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(9.dp).background(Color(0xFFFF5F56), CircleShape))
                        Spacer(modifier = Modifier.width(5.dp))
                        Box(modifier = Modifier.size(9.dp).background(Color(0xFFFFBD2E), CircleShape))
                        Spacer(modifier = Modifier.width(5.dp))
                        Box(modifier = Modifier.size(9.dp).background(Color(0xFF27C93F), CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = title,
                            color = CyberCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                    }

                    // Top Action Tools: DMESG, MAN, MEM GRID, CLEAR
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier
                                .clickable { onExecuteCommand("dmesg") }
                                .testTag("top_dmesg_btn"),
                            color = Color(0xFF132238),
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF254B7A))
                        ) {
                            Text("DMESG", color = Color(0xFF93C5FD), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                        }

                        Surface(
                            modifier = Modifier
                                .clickable {
                                    showManModal = true
                                    selectedManPage = "man"
                                }
                                .testTag("top_man_btn"),
                            color = Color(0xFF1E283D),
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, CyberCyan)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Podręcznik man", tint = CyberCyan, modifier = Modifier.size(11.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("MAN", color = CyberCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .clickable { showMemoryGridDialog = true }
                                .testTag("top_mem_grid_btn"),
                            color = Color(0xFF2E1E3D),
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, CyberPurple)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.GridOn, contentDescription = "Siatka pamięci", tint = CyberPurple, modifier = Modifier.size(11.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("MEM GRID", color = CyberPurple, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }

                        IconButton(
                            onClick = { onExecuteCommand("clear") },
                            modifier = Modifier.size(22.dp).testTag("terminal_clear_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Wyczyść konsolę",
                                tint = TextMuted,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            // CPU Cycles, Clockspeed & Hardware Limiter Telemetry Visualizer
            CpuHardwareVisualizer(
                cpuState = terminalEngine.cpuHardwareState,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Scrolling Output Area with dynamic keyword highlighting & distinct dmesg format
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF060913))
                    .border(1.dp, ConsoleBorder)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    items(lines) { line ->
                        val isDmesgLog = line.text.startsWith("[") && line.text.contains("]") && line.text.length > 15
                        val baseColor = when {
                            isDmesgLog && line.type == TerminalLineType.SUCCESS -> CyberGreen
                            isDmesgLog && line.type == TerminalLineType.ERROR -> CyberPink
                            isDmesgLog && line.type == TerminalLineType.WARNING -> CyberAmber
                            isDmesgLog -> Color(0xFF8BA2C4)
                            line.type == TerminalLineType.INPUT -> CyberCyan
                            line.type == TerminalLineType.SUCCESS -> CyberGreen
                            line.type == TerminalLineType.ERROR -> CyberPink
                            line.type == TerminalLineType.WARNING -> CyberAmber
                            line.type == TerminalLineType.HEADER -> CyberPurple
                            line.type == TerminalLineType.SYSTEM -> Color(0xFF93C5FD)
                            line.type == TerminalLineType.MATRIX -> CyberGreen
                            else -> TextPrimary
                        }

                        val annotated = highlightTerminalKeywords(line.text, baseColor)

                        Text(
                            text = annotated,
                            color = baseColor,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Tab-Completion Helper Bar & Command History Navigation ('Up'/'Down' arrows)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF090F1E),
                border = androidx.compose.foundation.BorderStroke(1.dp, ConsoleBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // History Up arrow button (cycles to older commands)
                    IconButton(
                        onClick = { navigateHistoryUp() },
                        modifier = Modifier.size(26.dp).testTag("history_up_btn")
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Historia w górę", tint = if (commandHistory.isNotEmpty()) CyberCyan else TextMuted, modifier = Modifier.size(17.dp))
                    }

                    // History Down arrow button (cycles to newer commands)
                    IconButton(
                        onClick = { navigateHistoryDown() },
                        modifier = Modifier.size(26.dp).testTag("history_down_btn")
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Historia w dół", tint = if (historyIndex >= 0) CyberCyan else TextMuted, modifier = Modifier.size(17.dp))
                    }

                    // TAB Completion button
                    Surface(
                        modifier = Modifier
                            .clickable {
                                if (tabSuggestions.isNotEmpty()) {
                                    applyCompletion(tabSuggestions.first())
                                }
                            }
                            .testTag("tab_completion_btn"),
                        color = Color(0xFF1E293D),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardTab, contentDescription = "Tab Autouzupełnianie", tint = CyberCyan, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("TAB", color = CyberCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Tab suggestions chips
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        tabSuggestions.forEach { suggestion ->
                            val isPath = suggestion.startsWith("/")
                            val isMem = suggestion in listOf("mem", "reg", "0x7C00", "0x9C000", "0xB8000", "eax", "cr0")
                            val isAsm = suggestion in listOf("asm", "nop", "cli", "hlt", "mov", "xor")
                            val isDmesg = suggestion == "dmesg"
                            Surface(
                                modifier = Modifier
                                    .clickable {
                                        applyCompletion(suggestion)
                                    }
                                    .testTag("tab_suggest_$suggestion"),
                                color = when {
                                    isPath -> Color(0xFF1B2A1E)
                                    isMem -> Color(0xFF281E3B)
                                    isAsm -> Color(0xFF3B281E)
                                    isDmesg -> Color(0xFF122338)
                                    else -> Color(0xFF131F33)
                                },
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    0.5.dp,
                                    when {
                                        isPath -> CyberGreen
                                        isMem -> CyberPurple
                                        isAsm -> CyberAmber
                                        isDmesg -> Color(0xFF60A5FA)
                                        else -> Color(0xFF1E3A5F)
                                    }
                                )
                            ) {
                                Text(
                                    text = suggestion,
                                    color = when {
                                        isPath -> CyberGreen
                                        isMem -> CyberPurple
                                        isAsm -> CyberAmber
                                        isDmesg -> Color(0xFF93C5FD)
                                        else -> CyberCyan
                                    },
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Interactive Input Prompt Bar with Hardware/Soft Keyboard Up/Down Arrow Interceptor
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF080D1A),
                shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ConsoleBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = promptPrefix,
                        color = CyberGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    BasicTextField(
                        value = currentInput,
                        onValueChange = {
                            currentInput = it
                            historyIndex = -1
                        },
                        modifier = Modifier
                            .weight(1f)
                            .onPreviewKeyEvent { keyEvent ->
                                if (keyEvent.type == KeyEventType.KeyDown) {
                                    when (keyEvent.key) {
                                        Key.DirectionUp -> {
                                            navigateHistoryUp()
                                            true
                                        }
                                        Key.DirectionDown -> {
                                            navigateHistoryDown()
                                            true
                                        }
                                        Key.Tab -> {
                                            if (tabSuggestions.isNotEmpty()) {
                                                applyCompletion(tabSuggestions.first())
                                                true
                                            } else false
                                        }
                                        else -> false
                                    }
                                } else false
                            }
                            .testTag("terminal_input_field"),
                        textStyle = TextStyle(
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        cursorBrush = SolidColor(CyberGreen),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (currentInput.isNotBlank()) {
                                    val text = currentInput.trim()
                                    if (!commandHistory.contains(text)) {
                                        commandHistory.add(text)
                                    }
                                    historyIndex = -1
                                    onExecuteCommand(text)
                                    currentInput = ""
                                }
                            }
                        )
                    )

                    IconButton(
                        onClick = {
                            if (currentInput.isNotBlank()) {
                                val text = currentInput.trim()
                                if (!commandHistory.contains(text)) {
                                    commandHistory.add(text)
                                }
                                historyIndex = -1
                                onExecuteCommand(text)
                                currentInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("terminal_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Wyślij polecenie",
                            tint = CyberGreen,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }

    // 1. Interactive Memory Grid Dialog with Clickable Hex Cells
    if (showMemoryGridDialog) {
        AlertDialog(
            onDismissRequest = { showMemoryGridDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.GridOn, contentDescription = null, tint = CyberPurple, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("INTERAKTYWNA SIATKA PAMIĘCI RAM", color = CyberPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    IconButton(onClick = { showMemoryGridDialog = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Zamknij", tint = TextMuted)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Kliknij na dowolny bajt, aby edytować jego wartość szesnastkową w czasie rzeczywistym.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    val memory = terminalEngine.getMemoryMap()
                    memory.forEach { (addr, bytes) ->
                        val label = when (addr) {
                            0x7C00L -> "MBR BOOT SECTOR (0x7C00)"
                            0x9C000L -> "PAGE DIRECTORY (0x9C000)"
                            0xB8000L -> "VGA BUFFER (0xB8000)"
                            0x100000L -> "KERNEL ELF IMAGE (0x100000)"
                            else -> "RAM REGION (0x${java.lang.Long.toHexString(addr).uppercase()})"
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF0C1424),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                Text(
                                    text = label,
                                    color = CyberCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                // Grid of 16 byte cells
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    bytes.toList().chunked(8).forEachIndexed { rowIdx, rowBytes ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            rowBytes.forEachIndexed { colIdx, b ->
                                                val byteOffset = rowIdx * 8 + colIdx
                                                val hexVal = String.format("%02X", b)
                                                val isSpecial = (addr == 0x7C00L && (byteOffset == 12 || byteOffset == 13)) || (b != 0.toByte())

                                                Surface(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable {
                                                            editingAddress = addr
                                                            editingByteIndex = byteOffset
                                                            editingHexInput = hexVal
                                                        }
                                                        .testTag("byte_cell_${addr}_$byteOffset"),
                                                    color = if (isSpecial) Color(0xFF22163B) else Color(0xFF090E1A),
                                                    shape = RoundedCornerShape(3.dp),
                                                    border = androidx.compose.foundation.BorderStroke(
                                                        1.dp,
                                                        if (isSpecial) CyberPurple else Color(0xFF1F293D)
                                                    )
                                                ) {
                                                    Box(
                                                        modifier = Modifier.padding(vertical = 4.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = hexVal,
                                                            color = if (isSpecial) CyberPurple else TextPrimary,
                                                            fontSize = 10.sp,
                                                            fontFamily = FontFamily.Monospace,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showMemoryGridDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPurple)
                ) {
                    Text("Zamknij", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            },
            containerColor = Color(0xFF090E1A)
        )
    }

    // 2. Inline Byte Hex Value Editor Sub-Dialog
    if (editingAddress != null) {
        val targetAddr = editingAddress!!
        AlertDialog(
            onDismissRequest = { editingAddress = null },
            title = {
                Text(
                    text = "EDYTUJ BAJT PAMIĘCI (0x${java.lang.Long.toHexString(targetAddr).uppercase()}[+$editingByteIndex])",
                    color = CyberCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Podaj nową wartość szesnastkową (00 - FF):", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    OutlinedTextField(
                        value = editingHexInput,
                        onValueChange = { if (it.length <= 2) editingHexInput = it.uppercase() },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = ConsoleBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val byteVal = editingHexInput.toInt(16).toByte()
                            terminalEngine.setMemoryByte(targetAddr, editingByteIndex, byteVal)
                            memoryVersion++
                            onExecuteCommand("mem dump 0x${java.lang.Long.toHexString(targetAddr).uppercase()}")
                        } catch (e: Exception) {
                            // invalid hex
                        }
                        editingAddress = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                ) {
                    Text("Zapisz", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { editingAddress = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                ) {
                    Text("Anuluj", color = TextSecondary, fontSize = 11.sp)
                }
            },
            containerColor = Color(0xFF0B101D)
        )
    }

    // 3. Unix 'man' Manual Viewer Modal
    if (showManModal) {
        AlertDialog(
            onDismissRequest = { showManModal = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PODRĘCZNIK SYSTEMOWY GENESIS OS", color = CyberCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    IconButton(onClick = { showManModal = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Zamknij", tint = TextMuted)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                ) {
                    // Quick command manual selector tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("man", "dmesg", "mem", "asm", "whoami", "build", "test", "boot", "quests", "ai").forEach { mCmd ->
                            Surface(
                                modifier = Modifier.clickable { selectedManPage = mCmd },
                                color = if (selectedManPage == mCmd) CyberCyanDark else Color(0xFF131D31),
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, if (selectedManPage == mCmd) CyberCyan else Color(0xFF1E3A5F))
                            ) {
                                Text(
                                    text = mCmd,
                                    color = if (selectedManPage == mCmd) Color.Black else CyberCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    // Formatted, scrollable manual text content
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        color = Color(0xFF060913),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ConsoleBorder)
                    ) {
                        val manLines = remember(selectedManPage) {
                            terminalEngine.getManualPage(selectedManPage)
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(manLines) { ml ->
                                val color = when (ml.type) {
                                    TerminalLineType.HEADER -> CyberCyan
                                    TerminalLineType.SYSTEM -> CyberAmber
                                    TerminalLineType.SUCCESS -> CyberGreen
                                    TerminalLineType.ERROR -> CyberPink
                                    else -> TextPrimary
                                }
                                Text(
                                    text = ml.text,
                                    color = color,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showManModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                ) {
                    Text("Zamknij", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF090E1A)
        )
    }
}
