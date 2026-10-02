package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.game.QuestsData
import com.example.ui.components.ArchitectureDiagramView
import com.example.ui.components.CodeEditorView
import com.example.ui.components.AiMentorView
import com.example.ui.components.TerminalView
import com.example.ui.components.VirtualMachineView
import com.example.ui.theme.ConsoleBackground
import com.example.ui.theme.ConsoleBorder
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberPink
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.TerminalCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.GameViewModel

@Composable
fun MainGameScreen(
    viewModel: GameViewModel
) {
    val context = LocalContext.current
    val gameSave by viewModel.gameSave.collectAsStateWithLifecycle()
    val allQuestsProgress by viewModel.allQuestsProgress.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val activeQuestId by viewModel.activeQuestId.collectAsStateWithLifecycle()
    val editorCode by viewModel.editorCode.collectAsStateWithLifecycle()
    val terminalLines by viewModel.terminalLines.collectAsStateWithLifecycle()
    val guestTerminalLines by viewModel.guestTerminalLines.collectAsStateWithLifecycle()
    val vmStatus by viewModel.vmStatus.collectAsStateWithLifecycle()
    val vmLogs by viewModel.vmLogs.collectAsStateWithLifecycle()
    val testResults by viewModel.testResults.collectAsStateWithLifecycle()
    val isCompiling by viewModel.isCompiling.collectAsStateWithLifecycle()
    val isAiThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()
    val aiMessages by viewModel.aiMessages.collectAsStateWithLifecycle()
    val cpuRegisters by viewModel.cpuRegisters.collectAsStateWithLifecycle()

    val currentQuest = QuestsData.allQuests.find { it.id == activeQuestId } ?: QuestsData.allQuests.first()
    val completedIds = allQuestsProgress.filter { it.isCompleted }.map { it.questId }.toSet()

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    // Handle back button to return to Terminal tab if on other subscreen
    BackHandler(enabled = selectedTab != AppTab.TERMINAL) {
        viewModel.selectTab(AppTab.TERMINAL)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        containerColor = ConsoleBackground,
        topBar = {
            // Top HUD Bar: Level, XP, Bits, Active Module, CRT toggle
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF0B101D),
                border = androidx.compose.foundation.BorderStroke(1.dp, ConsoleBorder)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Player Level & OS Name
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(CyberCyan, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${gameSave?.level ?: 1}",
                                    color = Color.Black,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = gameSave?.osName ?: "GenesisOS",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Faza ${gameSave?.currentPhase ?: 1}/10",
                                    color = CyberAmber,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Currency Bits & CRT Toggle
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color(0xFF1B2616),
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, CyberGreen)
                            ) {
                                Text(
                                    text = "🪙 ${gameSave?.bits ?: 100} BITS",
                                    color = CyberGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { viewModel.toggleScanlines() },
                                modifier = Modifier.size(28.dp).testTag("crt_toggle_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tv,
                                    contentDescription = "CRT Scanlines",
                                    tint = if (gameSave?.crtScanlinesEnabled == true) CyberCyan else TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // XP Progress bar
                    val xp = gameSave?.xp ?: 0
                    val xpInLevel = xp % 300
                    val progressFloat = (xpInLevel / 300f).coerceIn(0f, 1f)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = { progressFloat },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp),
                            color = CyberCyan,
                            trackColor = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$xp XP",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Bottom M3 Navigation Bar with 6 HUD tabs
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = Color(0xFF090E1A),
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == AppTab.TERMINAL,
                    onClick = { viewModel.selectTab(AppTab.TERMINAL) },
                    icon = { Icon(Icons.Default.Terminal, contentDescription = "Terminal") },
                    label = { Text("Terminal", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberCyan,
                        selectedTextColor = CyberCyan,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = Color(0xFF132238)
                    ),
                    modifier = Modifier.testTag("tab_terminal")
                )

                NavigationBarItem(
                    selected = selectedTab == AppTab.QUESTS,
                    onClick = { viewModel.selectTab(AppTab.QUESTS) },
                    icon = {
                        BadgedBox(
                            badge = {
                                val done = completedIds.size
                                Badge(containerColor = CyberAmber) {
                                    Text("$done/10", fontSize = 9.sp, color = Color.Black)
                                }
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = "Zadania")
                        }
                    },
                    label = { Text("Zadania", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberAmber,
                        selectedTextColor = CyberAmber,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = Color(0xFF2C2213)
                    ),
                    modifier = Modifier.testTag("tab_quests")
                )

                NavigationBarItem(
                    selected = selectedTab == AppTab.EDITOR,
                    onClick = { viewModel.selectTab(AppTab.EDITOR) },
                    icon = { Icon(Icons.Default.Code, contentDescription = "Kod") },
                    label = { Text("Edytor", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberGreen,
                        selectedTextColor = CyberGreen,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = Color(0xFF132C1E)
                    ),
                    modifier = Modifier.testTag("tab_editor")
                )

                NavigationBarItem(
                    selected = selectedTab == AppTab.VIRTUAL_MACHINE,
                    onClick = { viewModel.selectTab(AppTab.VIRTUAL_MACHINE) },
                    icon = { Icon(Icons.Default.Computer, contentDescription = "Maszyna VM") },
                    label = { Text("PC QEMU", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberCyan,
                        selectedTextColor = CyberCyan,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = Color(0xFF132238)
                    ),
                    modifier = Modifier.testTag("tab_vm")
                )

                NavigationBarItem(
                    selected = selectedTab == AppTab.ARCHITECTURE,
                    onClick = { viewModel.selectTab(AppTab.ARCHITECTURE) },
                    icon = { Icon(Icons.Default.Memory, contentDescription = "Architektura") },
                    label = { Text("Architektura", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberPurple,
                        selectedTextColor = CyberPurple,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = Color(0xFF221338)
                    ),
                    modifier = Modifier.testTag("tab_architecture")
                )

                NavigationBarItem(
                    selected = selectedTab == AppTab.AI_MENTOR,
                    onClick = { viewModel.selectTab(AppTab.AI_MENTOR) },
                    icon = {
                        Icon(Icons.Default.SmartToy, contentDescription = "Ada AI")
                    },
                    label = { Text("Ada AI", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberPurple,
                        selectedTextColor = CyberPurple,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = Color(0xFF221338)
                    ),
                    modifier = Modifier.testTag("tab_ai_mentor")
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                AppTab.TERMINAL -> {
                    TerminalScreen(
                        lines = terminalLines,
                        scanlinesEnabled = gameSave?.crtScanlinesEnabled ?: true,
                        onExecuteCommand = { viewModel.executeTerminalCommand(it) }
                    )
                }

                AppTab.QUESTS -> {
                    QuestsScreen(
                        questsProgress = allQuestsProgress,
                        activeQuestId = activeQuestId,
                        onSelectQuest = { viewModel.selectQuest(it) },
                        onNavigateToEditor = { viewModel.selectTab(AppTab.EDITOR) }
                    )
                }

                AppTab.EDITOR -> {
                    CodeEditorView(
                        quest = currentQuest,
                        code = editorCode,
                        testResults = testResults,
                        isCompiling = isCompiling,
                        onCodeChange = { viewModel.updateEditorCode(it) },
                        onCompile = { viewModel.compileCurrentCode() },
                        onRunTests = { viewModel.runTests() },
                        onResetTemplate = { viewModel.resetToDefaultCode() },
                        onLoadSolution = { viewModel.resetToReferenceSolution() },
                        onAskAiHint = {
                            viewModel.selectTab(AppTab.AI_MENTOR)
                        }
                    )
                }

                AppTab.VIRTUAL_MACHINE -> {
                    VirtualMachineView(
                        osName = gameSave?.osName ?: "GenesisOS",
                        status = vmStatus,
                        bootLogs = vmLogs,
                        guestTerminalLines = guestTerminalLines,
                        onBoot = { viewModel.bootVirtualMachine() },
                        onPowerOff = { viewModel.powerOffVm() },
                        onExecuteGuestCommand = { viewModel.executeGuestOsCommand(it) },
                        onSwitchToCli = { viewModel.switchGuestToCli() },
                        onSwitchToGui = { viewModel.switchGuestToGui() }
                    )
                }

                AppTab.ARCHITECTURE -> {
                    ArchitectureDiagramView(
                        completedQuestIds = completedIds,
                        cpuRegisters = cpuRegisters
                    )
                }

                AppTab.AI_MENTOR -> {
                    AiMentorView(
                        messages = aiMessages,
                        isThinking = isAiThinking,
                        currentQuestTitle = currentQuest.title,
                        onSendMessage = { viewModel.askAiMentor(it) },
                        onClearChat = { viewModel.clearAiChat() }
                    )
                }
            }
        }
    }
}
