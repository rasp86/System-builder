package com.example.game.vm

/**
 * Represents a single stage in the OS boot sequence.
 */
data class BootStage(
    val id: String,
    val title: String,
    val logMessage: String,
    val requiredQuestId: String? = null,
    val isCriticalFailure: Boolean = false,
    val warningMessage: String? = null,
    val failureMessage: String? = null
)

/**
 * Result of a generated boot sequence.
 */
data class BootSequenceResult(
    val logs: List<String>,
    val hasPanic: Boolean,
    val failedStage: BootStage? = null
)
