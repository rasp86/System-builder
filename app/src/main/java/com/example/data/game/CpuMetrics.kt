package com.example.data.game

data class DmesgEntry(
    val timestampSeconds: Double,
    val subsystem: String,
    val level: String, // "INFO", "OK", "WARN", "ERR"
    val message: String
) {
    fun formatFormattedLine(): String {
        val timeFormatted = String.format(java.util.Locale.US, "[%10.6f]", timestampSeconds)
        val tag = "[${subsystem.padEnd(9)}]"
        return "$timeFormatted $tag $message"
    }
}

data class CpuHardwareState(
    val clockspeedGhz: Double = 3.40,
    val totalCycles: Long = 1420500L,
    val lastOpCycles: Int = 1,
    val lastOpMnemonic: String = "NOP",
    val cpuTemperatureC: Int = 42,
    val instructionsPerCycle: Double = 2.4
)
