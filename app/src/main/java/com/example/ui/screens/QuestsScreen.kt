package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.data.db.QuestProgressEntity
import com.example.data.game.Quest
import com.example.data.game.QuestsData
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
fun QuestsScreen(
    modifier: Modifier = Modifier,
    questsProgress: List<QuestProgressEntity>,
    activeQuestId: String,
    onSelectQuest: (String) -> Unit,
    onNavigateToEditor: () -> Unit
) {
    val progressMap = questsProgress.associateBy { it.questId }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ConsoleBackground)
            .padding(12.dp)
    ) {
        // Roadmap Title
        Text(
            text = "PLAN BUDOWY SYSTEMU OPERACYJNEGO",
            color = CyberCyan,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "10 Faz Architektonicznych od Bootloadera do Suwerennego OS",
            color = TextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(QuestsData.allQuests) { quest ->
                val progress = progressMap[quest.id]
                val isUnlocked = progress?.isUnlocked ?: (quest.phase == 1)
                val isCompleted = progress?.isCompleted ?: false
                val isCurrent = quest.id == activeQuestId

                QuestCard(
                    quest = quest,
                    isUnlocked = isUnlocked,
                    isCompleted = isCompleted,
                    isCurrent = isCurrent,
                    onSelect = {
                        onSelectQuest(quest.id)
                        onNavigateToEditor()
                    }
                )
            }
        }
    }
}

@Composable
fun QuestCard(
    quest: Quest,
    isUnlocked: Boolean,
    isCompleted: Boolean,
    isCurrent: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = when {
        isCurrent -> CyberCyan
        isCompleted -> CyberGreen
        isUnlocked -> ConsoleBorder
        else -> Color(0xFF1F293D)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isUnlocked) { onSelect() }
            .testTag("quest_card_${quest.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) Color(0xFF131F33) else Color(0xFF0F172A)
        ),
        border = androidx.compose.foundation.BorderStroke(if (isCurrent) 1.5.dp else 1.dp, borderColor),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Phase Badge
                Surface(
                    color = when (quest.phase) {
                        1 -> Color(0xFF3B1E1E)
                        2 -> Color(0xFF3B2E1E)
                        3 -> Color(0xFF383B1E)
                        4 -> Color(0xFF1E3B24)
                        5 -> Color(0xFF1E383B)
                        6 -> Color(0xFF1E283B)
                        7 -> Color(0xFF281E3B)
                        8 -> Color(0xFF3B1E38)
                        9 -> Color(0xFF1E3B39)
                        else -> Color(0xFF353B1E)
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = quest.phaseName,
                        color = CyberAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Completion status
                if (isCompleted) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CyberGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("UKOŃCZONE", color = CyberGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                } else if (!isUnlocked) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ZABLOKOWANE", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                } else {
                    Text("DO ZROBIENIA", color = CyberCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = quest.title,
                color = if (isUnlocked) TextPrimary else TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = quest.storyPrompt,
                color = if (isUnlocked) TextSecondary else Color.DarkGray,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 15.sp,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Plik: ${quest.filePath}", color = CyberCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("+${quest.xpReward} XP", color = CyberGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                if (isUnlocked) {
                    Button(
                        onClick = onSelect,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCurrent) CyberCyan else Color(0xFF1E293B)
                        ),
                        modifier = Modifier.height(28.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                    ) {
                        Text(
                            text = if (isCurrent) "W Edytorze" else "Otwórz Zadanie",
                            color = if (isCurrent) Color.Black else TextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
