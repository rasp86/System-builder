package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.game.OSBootStatus
import com.example.data.game.TerminalLine
import com.example.ui.theme.ConsoleBackground
import com.example.ui.theme.ConsoleBorder
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDark
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberPink
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.TerminalCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun VirtualMachineView(
    modifier: Modifier = Modifier,
    osName: String,
    status: OSBootStatus,
    bootLogs: List<String>,
    guestTerminalLines: List<TerminalLine>,
    onBoot: () -> Unit,
    onPowerOff: () -> Unit,
    onExecuteGuestCommand: (String) -> Unit,
    onSwitchToCli: () -> Unit,
    onSwitchToGui: () -> Unit
) {
    val bootLogListState = rememberLazyListState()

    LaunchedEffect(bootLogs.size) {
        if (bootLogs.isNotEmpty()) {
            bootLogListState.animateScrollToItem(bootLogs.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
            .padding(8.dp)
    ) {
        // VM Top Control Header (Power, Reset, Status LEDs)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF111928),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, ConsoleBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Computer,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "VIRTUAL PC x86 (QEMU / SeaBIOS)",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Status LED
                            val ledColor = when (status) {
                                OSBootStatus.POWERED_OFF -> Color.Gray
                                OSBootStatus.BIOS_POST, OSBootStatus.BOOTLOADER_RUNNING, OSBootStatus.KERNEL_BOOTING -> CyberAmber
                                OSBootStatus.RUNNING_CLI, OSBootStatus.RUNNING_GUI -> CyberGreen
                                OSBootStatus.KERNEL_PANIC -> CyberPink
                            }
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(ledColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (status) {
                                    OSBootStatus.POWERED_OFF -> "ZASILANIE WYŁĄCZONE"
                                    OSBootStatus.BIOS_POST -> "BIOS POST & INICJALIZACJA"
                                    OSBootStatus.BOOTLOADER_RUNNING -> "BOOTLOADER 0x7C00"
                                    OSBootStatus.KERNEL_BOOTING -> "ŁADOWANIE JĄDRA..."
                                    OSBootStatus.RUNNING_CLI -> "JĄDRO AKTYWNE (Tryb CLI)"
                                    OSBootStatus.RUNNING_GUI -> "JĄDRO AKTYWNE (Pulpit GUI VBE)"
                                    OSBootStatus.KERNEL_PANIC -> "KERNEL PANIC / BŁĄD ARCHITEKTURY"
                                },
                                color = ledColor,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (status == OSBootStatus.POWERED_OFF) {
                        Button(
                            onClick = onBoot,
                            colors = ButtonDefaults.buttonColors(containerColor = CyberGreen),
                            modifier = Modifier.height(34.dp).testTag("vm_power_on_btn")
                        ) {
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WŁĄCZ PC", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        IconButton(
                            onClick = onBoot,
                            modifier = Modifier.size(34.dp).testTag("vm_reset_btn")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = CyberAmber)
                        }
                        IconButton(
                            onClick = onPowerOff,
                            modifier = Modifier.size(34.dp).testTag("vm_power_off_btn")
                        ) {
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = "Wyłącz", tint = CyberPink)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // VM Screen Display Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Black, RoundedCornerShape(10.dp))
                .border(2.dp, if (status == OSBootStatus.KERNEL_PANIC) CyberPink else ConsoleBorder, RoundedCornerShape(10.dp))
        ) {
            when (status) {
                OSBootStatus.POWERED_OFF -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Computer,
                            contentDescription = null,
                            tint = Color(0xFF1E293B),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Maszyna Wirtualna jest wyłączona",
                            color = TextMuted,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Kliknij 'WŁĄCZ PC' aby przetestować swój bootloader i jądro.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                OSBootStatus.BIOS_POST, OSBootStatus.BOOTLOADER_RUNNING, OSBootStatus.KERNEL_BOOTING, OSBootStatus.KERNEL_PANIC -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "=== LOGI ROZRUCHU SYSTEMU (SeaBIOS / Kernel Console) ===",
                            color = CyberCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        LazyColumn(
                            state = bootLogListState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(bootLogs) { log ->
                                val color = when {
                                    log.contains("PANIC") || log.contains("FATAL") -> CyberPink
                                    log.contains("WARNING") -> CyberAmber
                                    log.contains("OK") || log.contains("WELCOME") -> CyberGreen
                                    else -> TextPrimary
                                }
                                Text(
                                    text = log,
                                    color = color,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }

                OSBootStatus.RUNNING_CLI -> {
                    // Running interactive CLI inside guest OS
                    TerminalView(
                        title = "$osName 1.0 (tty0) - Tryb Tekstowy VGA 80x25",
                        lines = guestTerminalLines,
                        promptPrefix = "root@genesis:/# ",
                        quickCommands = listOf("help", "uname -a", "sysinfo", "ls /", "cat /etc/os-release", "ps", "mem", "calc 256*16", "matrix", "clear"),
                        onExecuteCommand = onExecuteGuestCommand
                    )
                }

                OSBootStatus.RUNNING_GUI -> {
                    // Running Graphical VESA/VBE Window Desktop
                    GraphicalDesktopView(
                        osName = osName,
                        onSwitchToCli = onSwitchToCli
                    )
                }
            }
        }
    }
}

@Composable
fun GraphicalDesktopView(
    osName: String,
    onSwitchToCli: () -> Unit
) {
    var activeWindow by remember { mutableStateOf("calc") }
    var calcDisplay by remember { mutableStateOf("42") }
    var snakeScore by remember { mutableStateOf(120) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // Desktop Wallpaper Grid Pattern
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 32.dp.toPx()
            var x = 0f
            while (x < size.width) {
                drawLine(Color(0x1538BDF8), Offset(x, 0f), Offset(x, size.height), 1f)
                x += step
            }
            var y = 0f
            while (y < size.height) {
                drawLine(Color(0x1538BDF8), Offset(0f, y), Offset(size.width, y), 1f)
                y += step
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Desktop Top Menubar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF0B0F19),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⚡ $osName Desktop (VBE 800x600)",
                            color = CyberCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Aplikacje", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Menedżer Zadań", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("RAM: 15.2MB / 64MB", color = CyberGreen, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("12:42:00", color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            // Desktop Area with Apps
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Window Frame
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    color = Color(0xFF161F30),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Window Title Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F172A))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (activeWindow) {
                                    "calc" -> "🧮 Genesis Scientific Calculator"
                                    "sysmon" -> "📊 System Monitor (CPU & RAM Usage)"
                                    "snake" -> "🐍 Kernel Snake 2D Game"
                                    else -> "Pulpit"
                                },
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(10.dp).background(CyberGreen, CircleShape))
                                Box(modifier = Modifier.size(10.dp).background(CyberAmber, CircleShape))
                                Box(modifier = Modifier.size(10.dp).background(CyberPink, CircleShape))
                            }
                        }

                        // Window Body
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            when (activeWindow) {
                                "calc" -> {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Surface(
                                            modifier = Modifier.fillMaxWidth().height(44.dp),
                                            color = Color(0xFF090E1A),
                                            shape = RoundedCornerShape(6.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, ConsoleBorder)
                                        ) {
                                            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp), contentAlignment = Alignment.CenterEnd) {
                                                Text(calcDisplay, color = CyberGreen, fontSize = 20.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        val buttons = listOf(
                                            listOf("7", "8", "9", "/"),
                                            listOf("4", "5", "6", "*"),
                                            listOf("1", "2", "3", "-"),
                                            listOf("C", "0", "=", "+")
                                        )
                                        buttons.forEach { row ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                row.forEach { btn ->
                                                    Button(
                                                        onClick = {
                                                            when (btn) {
                                                                "C" -> calcDisplay = "0"
                                                                "=" -> calcDisplay = "0x" + (calcDisplay.toLongOrNull() ?: 42L).toString(16).uppercase()
                                                                else -> {
                                                                    calcDisplay = if (calcDisplay == "0") btn else calcDisplay + btn
                                                                }
                                                            }
                                                        },
                                                        modifier = Modifier.weight(1f).height(36.dp),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = if (btn in listOf("+", "-", "*", "/", "=")) CyberCyan else Color(0xFF1F293D)
                                                        ),
                                                        shape = RoundedCornerShape(4.dp),
                                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                                    ) {
                                                        Text(btn, color = if (btn in listOf("+", "-", "*", "/", "=")) Color.Black else TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                "sysmon" -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OsResourceDashboard(
                                            simulatedRamMb = 64f,
                                            simulatedRamUsedMb = 18.4f,
                                            diskReadKbps = 1850f,
                                            diskWriteKbps = 720f,
                                            title = "VIRTUAL PC SYSTEM MONITOR"
                                        )
                                        Text(
                                            text = "Virtual Memory MMU: 4096 KB Identity Mapped (Paging CR0.PG=1)",
                                            color = CyberPurple,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "Processes active in Ring 3: 4 tasks (scheduler: Round-Robin)",
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                "snake" -> {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text("🐍 Genesis Snake Game v1.0", color = CyberGreen, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                        Text("Wynik: $snakeScore punktów", color = CyberAmber, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = { snakeScore += 10 },
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberGreen)
                                        ) {
                                            Text("Nakarm Węża (+10)", color = Color.Black, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Desktop Taskbar (Bottom App Switcher)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF090E1A),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = onSwitchToCli,
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F293D)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.Terminal, contentDescription = null, tint = CyberGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Terminal CLI", fontSize = 10.sp, color = TextPrimary)
                    }

                    Button(
                        onClick = { activeWindow = "calc" },
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeWindow == "calc") CyberCyanDark else Color(0xFF1F293D)
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Kalkulator", fontSize = 10.sp, color = TextPrimary)
                    }

                    Button(
                        onClick = { activeWindow = "sysmon" },
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeWindow == "sysmon") CyberCyanDark else Color(0xFF1F293D)
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("System Monitor", fontSize = 10.sp, color = TextPrimary)
                    }

                    Button(
                        onClick = { activeWindow = "snake" },
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeWindow == "snake") CyberCyanDark else Color(0xFF1F293D)
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Gra Snake", fontSize = 10.sp, color = TextPrimary)
                    }
                }
            }
        }
    }
}
