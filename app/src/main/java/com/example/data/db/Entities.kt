package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_save")
data class GameSaveEntity(
    @PrimaryKey val id: Int = 1,
    val osName: String = "GenesisOS",
    val playerNick: String = "JuniorKernelDev",
    val level: Int = 1,
    val xp: Int = 0,
    val bits: Int = 100,
    val currentPhase: Int = 1,
    val crtScanlinesEnabled: Boolean = true,
    val soundHapticsEnabled: Boolean = true,
    val bootCount: Int = 0,
    val bugsFixed: Int = 0,
    val totalLinesWritten: Int = 42,
    val unlockedAppsJson: String = "[\"terminal\",\"memview\",\"calc\",\"sysinfo\"]"
)

@Entity(tableName = "quest_progress")
data class QuestProgressEntity(
    @PrimaryKey val questId: String,
    val phase: Int,
    val isUnlocked: Boolean,
    val isCompleted: Boolean,
    val userCode: String,
    val stars: Int = 0,
    val completedAt: Long = 0L
)

@Entity(tableName = "virtual_files")
data class VirtualFileEntity(
    @PrimaryKey val path: String,
    val fileName: String,
    val content: String,
    val language: String, // "asm", "c", "h", "txt", "sh"
    val isKernelCore: Boolean = false,
    val lastModified: Long = System.currentTimeMillis()
)

@Entity(tableName = "ai_messages")
data class AiMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String, // "user", "model"
    val prompt: String,
    val content: String,
    val reasoning: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val questId: String = ""
)
