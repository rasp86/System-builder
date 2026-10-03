package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.game.CpuRegisters
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

@Composable
fun ArchitectureDiagramView(
    modifier: Modifier = Modifier,
    completedQuestIds: Set<String>,
    cpuRegisters: CpuRegisters,
    osName: String = "GenesisOS",
    currentPhase: Int = 1
) {
    var selectedLayerDetail by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ConsoleBackground)
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // OS Specification & Architecture Info Panel
        OsInfoPanel(
            osName = osName,
            currentPhase = currentPhase,
            cpuArch = "x86_64 / IA-32 (Protected Mode)",
            memoryCapacity = "64 MB Physical RAM (4KB 2-Level Paging)",
            filesystemType = "GenesisFS / Virtual VFS (Inodes & Superblock)"
        )

        Text(
            text = "DIAGRAM ARCHITEKTURY SYSTEMU OPERACYJNEGO",
            color = CyberCyan,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "Wizualizacja warstw abstrakcji procesora, pamięci i przestrzeni użytkownika",
            color = TextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )

        // Ring 3 - Userland
        ArchitectureLayerCard(
            layerName = "PRZESTRZEŃ UŻYTKOWNIKA (RING 3 - USER MODE)",
            subtitle = "Aplikacje, Menedżer Okien GUI, Shell, Narzędzia POSIX",
            icon = Icons.Default.Widgets,
            accentColor = CyberCyan,
            isUnlocked = completedQuestIds.contains("Q7_1_SYSCALLS_AND_SHELL"),
            modules = listOf(
                "Genesis GUI Desktop (VBE)" to completedQuestIds.contains("Q8_1_GUI_FRAMEBUFFER"),
                "Shell /bin/gsh" to completedQuestIds.contains("Q7_1_SYSCALLS_AND_SHELL"),
                "HTTP Server & Sockets" to completedQuestIds.contains("Q9_1_NET_STACK"),
                "TCC C Compiler" to completedQuestIds.contains("Q10_1_OS_RELEASE")
            )
        )

        // Gateway - Syscalls
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF131D31),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A5F))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = CyberPurple, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "BRAMA WYWOŁAŃ SYSTEMOWYCH: INT 0x80 / SYSENTER (Ring 3 ➔ Ring 0)",
                    color = CyberPurple,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Ring 0 - Kernel Land
        ArchitectureLayerCard(
            layerName = "JĄDRO SYSTEMU (RING 0 - KERNEL SPACE)",
            subtitle = "Pamięć wirtualna MMU, Planista zadań, VFS, Sterowniki, IDT",
            icon = Icons.Default.Memory,
            accentColor = CyberGreen,
            isUnlocked = completedQuestIds.contains("Q2_1_VGA_TERMINAL"),
            modules = listOf(
                "Stronicowanie MMU (CR3)" to completedQuestIds.contains("Q3_1_PAGING_MMU"),
                "Round-Robin Scheduler" to completedQuestIds.contains("Q4_1_ROUND_ROBIN_SCHEDULER"),
                "VFS Inode & Ramdisk" to completedQuestIds.contains("Q5_1_VFS_INODES"),
                "Tablica Przerwań IDT & PIC" to completedQuestIds.contains("Q2_2_IDT_INTERRUPTS"),
                "Sterownik VGA 80x25" to completedQuestIds.contains("Q2_1_VGA_TERMINAL"),
                "Klawiatura PS/2 & UART" to completedQuestIds.contains("Q6_1_PS2_KEYBOARD")
            )
        )

        // Bootloader & Hardware Layer
        ArchitectureLayerCard(
            layerName = "SPRZĘT & SEKTOR ROZRUCHOWY (BARE METAL)",
            subtitle = "BIOS POST, MBR 0x7C00 (0xAA55), 32-bit GDT, CPU x86 Registers",
            icon = Icons.Default.Storage,
            accentColor = CyberAmber,
            isUnlocked = completedQuestIds.contains("Q1_1_MBR_MAGIC"),
            modules = listOf(
                "MBR 512B (0xAA55)" to completedQuestIds.contains("Q1_1_MBR_MAGIC"),
                "GDT & CR0.PE (32-bit)" to completedQuestIds.contains("Q1_2_GDT_PROTECTED_MODE"),
                "Kontroler Przerwań PIC 8259" to completedQuestIds.contains("Q2_2_IDT_INTERRUPTS"),
                "Zegar Sprzętowy PIT 8254" to completedQuestIds.contains("Q4_1_ROUND_ROBIN_SCHEDULER")
            )
        )

        // CPU Registers Live Monitor Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, ConsoleBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "STATUS REJESTRÓW PROCESORA x86 (IA-32)",
                    color = CyberCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))

                val regList = listOf(
                    "EAX" to cpuRegisters.eax,
                    "EBX" to cpuRegisters.ebx,
                    "ECX" to cpuRegisters.ecx,
                    "EDX" to cpuRegisters.edx,
                    "ESP" to cpuRegisters.esp,
                    "EBP" to cpuRegisters.ebp,
                    "EIP" to cpuRegisters.eip,
                    "CR0" to cpuRegisters.cr0,
                    "CR3" to cpuRegisters.cr3,
                    "EFLAGS" to cpuRegisters.eflags
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        regList.take(5).forEach { (reg, valStr) ->
                            Text(
                                text = "$reg: $valStr",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        regList.drop(5).forEach { (reg, valStr) ->
                            Text(
                                text = "$reg: $valStr",
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

@Composable
fun ArchitectureLayerCard(
    layerName: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    isUnlocked: Boolean,
    modules: List<Pair<String, Boolean>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101626)),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isUnlocked) accentColor else Color(0xFF1F293D)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = layerName,
                        color = accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (isUnlocked) {
                    Text("ODBLOKOWANE", color = CyberGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ZABLOKOWANE", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)

            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                modules.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { (modName, isDone) ->
                            Surface(
                                modifier = Modifier.weight(1f),
                                color = if (isDone) Color(0xFF132338) else Color(0xFF090E1A),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, if (isDone) CyberGreen else Color(0xFF1F293D))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(if (isDone) CyberGreen else Color.Gray, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = modName,
                                        color = if (isDone) TextPrimary else TextMuted,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        if (row.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
