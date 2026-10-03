package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.game.CpuHardwareState
import com.example.ui.theme.ConsoleBorder
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberPink
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * Visual Dashboard component displaying simulated CPU, RAM, and Disk I/O usage
 * for the user's custom Operating System with animated progress bars and telemetry.
 */
@Composable
fun OsResourceDashboard(
    modifier: Modifier = Modifier,
    cpuState: CpuHardwareState = CpuHardwareState(),
    simulatedRamMb: Float = 64f,
    simulatedRamUsedMb: Float = 18.4f,
    diskReadKbps: Float = 1420f,
    diskWriteKbps: Float = 680f,
    title: String = "OS SYSTEM TELEMETRY & RESOURCE DASHBOARD"
) {
    // Disk activity LED pulse animation
    var diskLedOn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            diskLedOn = !diskLedOn
            delay((200..600).random().toLong())
        }
    }

    val cpuPercent = (cpuState.currentLoadPercent / 100f).coerceIn(0f, 1f)
    val ramPercent = (simulatedRamUsedMb / simulatedRamMb).coerceIn(0f, 1f)
    val diskIoPercent = ((diskReadKbps + diskWriteKbps) / 4000f).coerceIn(0.05f, 1f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("os_resource_dashboard"),
        color = Color(0xFF0A0F1D),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ConsoleBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Dashboard Title Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = "Telemetry",
                        tint = CyberCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Disk I/O Activity LED
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (diskLedOn) CyberGreen else Color(0xFF1E3A20),
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = "DISK I/O ACTIVE",
                        color = if (diskLedOn) CyberGreen else TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // 1. CPU USAGE BAR
            ResourceMeterRow(
                icon = Icons.Default.Speed,
                iconTint = if (cpuPercent > 0.7f) CyberPink else CyberCyan,
                label = "CPU USAGE",
                detail = String.format(Locale.US, "%.1f%% @ %.1f GHz (temp: %d°C)", cpuState.currentLoadPercent, cpuState.clockspeedGhz, cpuState.cpuTemperatureC),
                progress = cpuPercent,
                progressColor = if (cpuPercent > 0.7f) CyberPink else if (cpuPercent > 0.4f) CyberAmber else CyberCyan,
                testTag = "cpu_usage_meter"
            )

            // 2. RAM USAGE BAR
            ResourceMeterRow(
                icon = Icons.Default.Memory,
                iconTint = CyberAmber,
                label = "RAM (PHYSICAL / VFS)",
                detail = String.format(Locale.US, "%.1f MB / %.0f MB (%.0f%%)", simulatedRamUsedMb, simulatedRamMb, ramPercent * 100f),
                progress = ramPercent,
                progressColor = CyberAmber,
                testTag = "ram_usage_meter"
            )

            // 3. DISK I/O USAGE BAR
            ResourceMeterRow(
                icon = Icons.Default.Storage,
                iconTint = CyberGreen,
                label = "DISK I/O (ATA / AHCI)",
                detail = String.format(Locale.US, "R: %.1f MB/s | W: %.1f MB/s", diskReadKbps / 1024f, diskWriteKbps / 1024f),
                progress = diskIoPercent,
                progressColor = CyberGreen,
                testTag = "disk_io_meter"
            )
        }
    }
}

@Composable
private fun ResourceMeterRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    label: String,
    detail: String,
    progress: Float,
    progressColor: Color,
    testTag: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    color = TextPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = detail,
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = progressColor,
            trackColor = Color(0xFF131A2B),
            strokeCap = StrokeCap.Round
        )
    }
}
