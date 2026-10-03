package com.example.domain.model

data class GameState(
    val id: Int = 1,
    val osName: String = "GenesisOS",
    val playerNick: String = "KernelArchitect",
    val level: Int = 1,
    val xp: Int = 0,
    val bits: Int = 100,
    val currentPhase: Int = 1,
    val crtScanlinesEnabled: Boolean = true,
    val terminalColorScheme: String = "CYBER_MATRIX",
    val bootCount: Int = 0,
    val bugsFixed: Int = 0,
    val totalLinesWritten: Int = 0
)

data class QuestProgress(
    val questId: String,
    val phase: Int,
    val isUnlocked: Boolean,
    val isCompleted: Boolean,
    val userCode: String,
    val stars: Int = 0,
    val completedAt: Long = 0L
)

sealed class GameResult<out T> {
    data class Success<out T>(val data: T) : GameResult<T>()
    data class Error(val exception: Throwable, val message: String = exception.message ?: "Unknown error") : GameResult<Nothing>()
    object Loading : GameResult<Nothing>()
}
