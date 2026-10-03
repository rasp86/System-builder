package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ConsoleBorder
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberPink
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * 'OS Info' panel that displays virtual CPU architecture, memory capacity,
 * and storage filesystem type for the OS being built.
 */
@Composable
fun OsInfoPanel(
    modifier: Modifier = Modifier,
    osName: String = "GenesisOS",
    currentPhase: Int = 1,
    cpuArch: String = "x86_64 / IA-32 (Protected Mode)",
    memoryCapacity: String = "64 MB Physical RAM (4KB 2-Level Paging)",
    filesystemType: String = "GenesisFS / Virtual VFS (Inodes & Superblock)"
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("os_info_panel"),
        color = Color(0xFF090E1B),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ConsoleBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "OS Info",
                        tint = CyberCyan,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "OS ARCHITECTURE & SYSTEM SPECIFICATION",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Surface(
                    color = Color(0xFF132238),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, CyberCyan)
                ) {
                    Text(
                        text = "$osName (Faza $currentPhase/10)",
                        color = CyberCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // 1. Virtual CPU Architecture Section
            InfoSectionCard(
                icon = Icons.Default.DeveloperBoard,
                iconTint = CyberPink,
                title = "VIRTUAL CPU ARCHITECTURE",
                testTag = "os_info_cpu_arch"
            ) {
                InfoPropertyRow("Architecture", cpuArch, CyberPink)
                InfoPropertyRow("Privilege Levels", "Ring 0 (Supervisor) / Ring 3 (User)", TextPrimary)
                InfoPropertyRow("MMU & Protection", "GDT Segmentation + IDT Interrupts + CR0.PE/PG", TextSecondary)
                InfoPropertyRow("Timers & Hardware", "Intel 8254 PIT (100Hz) & 8259 PIC Cascade", TextMuted)
            }

            // 2. Memory Capacity Section
            InfoSectionCard(
                icon = Icons.Default.Memory,
                iconTint = CyberAmber,
                title = "MEMORY CAPACITY & MMU",
                testTag = "os_info_memory"
            ) {
                InfoPropertyRow("Total Memory", memoryCapacity, CyberAmber)
                InfoPropertyRow("Paging Mode", "2-Level Hierarchical Paging (CR3 Page Directory)", TextPrimary)
                InfoPropertyRow("Kernel Base Address", "0x00100000 (1 MB physical entry)", CyberCyan)
                InfoPropertyRow("Identity Mapping", "0x00000000 - 0x00400000 (4 MB identity-mapped)", TextMuted)
            }

            // 3. Storage & Filesystem Type Section
            InfoSectionCard(
                icon = Icons.Default.Storage,
                iconTint = CyberGreen,
                title = "STORAGE & FILESYSTEM TYPE",
                testTag = "os_info_filesystem"
            ) {
                InfoPropertyRow("Filesystem Type", filesystemType, CyberGreen)
                InfoPropertyRow("Partition Table", "MBR Partition Table (0xAA55 Magic Signature)", TextPrimary)
                InfoPropertyRow("Storage Controller", "IDE / ATA PIO Mode (Drive 0x80 Primary Master)", TextSecondary)
                InfoPropertyRow("Mounted Subsystems", "/boot, /dev, /etc, /bin, /home (Virtual Inodes)", CyberCyan)
            }
        }
    }
}

@Composable
private fun InfoSectionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    testTag: String,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        color = Color(0xFF0C1322),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF1E293B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = title,
                    color = iconTint,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            content()
        }
    }
}

@Composable
private fun InfoPropertyRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
    }
}
