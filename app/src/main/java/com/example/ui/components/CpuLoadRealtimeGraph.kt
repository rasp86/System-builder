package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.game.CpuHardwareState
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberPink
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

/**
 * Real-time CPU Load Graph component visualizing CPU utilization percentage over time,
 * dynamically responding to instruction cycle intensity from 'asm' and 'int' commands.
 */
@Composable
fun CpuLoadRealtimeGraph(
    cpuState: CpuHardwareState,
    modifier: Modifier = Modifier
) {
    val history = cpuState.cpuLoadHistory
    val currentLoad = cpuState.currentLoadPercent
    val peakLoad = history.maxOrNull() ?: currentLoad
    val avgLoad = if (history.isNotEmpty()) history.average().toFloat() else currentLoad

    // Smooth pulse animation on CPU load spikes
    val pulseAnim = remember { Animatable(1f) }
    LaunchedEffect(currentLoad) {
        if (currentLoad > 40f) {
            pulseAnim.animateTo(1.15f, animationSpec = tween(120))
            pulseAnim.animateTo(1.0f, animationSpec = tween(200))
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cpu_load_realtime_graph"),
        color = Color(0xFF090E1D),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1B2A4A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            // Header stats bar (Recharts-style legend & telemetry badges)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoGraph,
                        contentDescription = "Wykres obciążenia CPU",
                        tint = if (currentLoad > 60f) CyberPink else if (currentLoad > 35f) CyberAmber else CyberCyan,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "CPU LOAD REAL-TIME TELEMETRY",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Current Load badge
                    Surface(
                        color = if (currentLoad > 60f) Color(0xFF381523) else Color(0xFF132238),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            0.5.dp,
                            if (currentLoad > 60f) CyberPink else CyberCyan
                        )
                    ) {
                        Text(
                            text = String.format(Locale.US, "NOW: %.1f%%", currentLoad),
                            color = if (currentLoad > 60f) CyberPink else CyberCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }

                    // Peak Load badge
                    Text(
                        text = String.format(Locale.US, "PEAK: %.1f%%", peakLoad),
                        color = if (peakLoad > 70f) CyberPink else CyberAmber,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Real-Time Canvas Area Chart (Recharts style smooth bezier & glowing area gradient)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .background(Color(0xFF050813), RoundedCornerShape(4.dp))
                    .border(0.5.dp, Color(0xFF131D31), RoundedCornerShape(4.dp))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(65.dp)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val points = history

                    if (points.size < 2) return@Canvas

                    // Draw subtle horizontal grid lines (0%, 25%, 50%, 75%, 100%)
                    val gridSteps = 4
                    for (i in 0..gridSteps) {
                        val y = height * (i.toFloat() / gridSteps)
                        drawLine(
                            color = Color(0x18FFFFFF),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 0.8f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                        )
                    }

                    // Calculate X & Y coordinates for points
                    val stepX = width / (points.size - 1).coerceAtLeast(1)
                    val coords = points.mapIndexed { index, value ->
                        val normalizedY = (value.coerceIn(0f, 100f) / 100f)
                        val y = height - (normalizedY * height)
                        val x = index * stepX
                        Offset(x, y)
                    }

                    // Build smooth cubic Bezier path for chart line and filled area
                    val linePath = Path().apply {
                        moveTo(coords.first().x, coords.first().y)
                        for (i in 0 until coords.size - 1) {
                            val p0 = coords[i]
                            val p1 = coords[i + 1]
                            val controlX = (p0.x + p1.x) / 2f
                            cubicTo(
                                x1 = controlX, y1 = p0.y,
                                x2 = controlX, y2 = p1.y,
                                x3 = p1.x, y3 = p1.y
                            )
                        }
                    }

                    val fillPath = Path().apply {
                        addPath(linePath)
                        lineTo(coords.last().x, height)
                        lineTo(coords.first().x, height)
                        close()
                    }

                    // Draw gradient area under the curve
                    val areaGradient = Brush.verticalGradient(
                        colors = if (currentLoad > 60f) {
                            listOf(CyberPink.copy(alpha = 0.45f), CyberPink.copy(alpha = 0.05f))
                        } else if (currentLoad > 35f) {
                            listOf(CyberAmber.copy(alpha = 0.40f), CyberAmber.copy(alpha = 0.03f))
                        } else {
                            listOf(CyberCyan.copy(alpha = 0.35f), CyberCyan.copy(alpha = 0.02f))
                        },
                        startY = 0f,
                        endY = height
                    )
                    drawPath(path = fillPath, brush = areaGradient)

                    // Draw the smooth line on top
                    val lineColor = if (currentLoad > 60f) CyberPink else if (currentLoad > 35f) CyberAmber else CyberCyan
                    drawPath(
                        path = linePath,
                        color = lineColor,
                        style = Stroke(width = 2.2f, cap = StrokeCap.Round)
                    )

                    // Highlight the last real-time active point with pulsing glowing circle
                    val lastPoint = coords.last()
                    drawCircle(
                        color = lineColor.copy(alpha = 0.3f),
                        radius = 6f * pulseAnim.value,
                        center = lastPoint
                    )
                    drawCircle(
                        color = lineColor,
                        radius = 3.5f,
                        center = lastPoint
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Footer Subsystem Load & Opcode Intensity info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Bolt, contentDescription = "Spike", tint = CyberAmber, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Last Opcode: ${cpuState.lastOpMnemonic} (+${cpuState.lastOpCycles} cycles)",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = "Load Model: Ring 0 Real-Time Clock",
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
