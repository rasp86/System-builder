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
    val instructionsPerCycle: Double = 2.4,
    val currentLoadPercent: Float = 14.0f,
    val cpuLoadHistory: List<Float> = listOf(
        12f, 14f, 11f, 15f, 18f, 14f, 12f, 16f, 22f, 18f, 15f, 12f, 14f, 16f, 15f, 19f, 14f, 12f
    )
) {
    fun withLoadSample(newLoad: Float, opMnemonic: String, opCycles: Int, addedCycles: Int): CpuHardwareState {
        val updatedHistory = (cpuLoadHistory + newLoad).takeLast(32)
        val newTemp = (40 + (newLoad * 0.35f)).toInt().coerceIn(38, 85)
        return copy(
            currentLoadPercent = newLoad,
            cpuLoadHistory = updatedHistory,
            lastOpMnemonic = opMnemonic,
            lastOpCycles = opCycles,
            totalCycles = totalCycles + addedCycles,
            cpuTemperatureC = newTemp
        )
    }
}
