package com.example.data.game

enum class TerminalLineType {
    COMMAND,
    INPUT,
    OUTPUT,
    SYSTEM,
    ERROR,
    SUCCESS,
    WARNING,
    HEADER,
    MATRIX,
    DIFF
}

data class TerminalLine(
    val text: String,
    val type: TerminalLineType = TerminalLineType.OUTPUT
)

enum class FsNodeType {
    FILE,
    DIRECTORY,
    DEVICE,
    SYMLINK
}

data class VirtualFsNode(
    val path: String,
    val name: String,
    val type: FsNodeType,
    val permissions: String = "rw-r--r--",
    val owner: String = "root",
    val group: String = "root",
    val sizeBytes: Long = 0L,
    val content: String = "",
    val isExecutable: Boolean = false,
    val modifiedTimestampMillis: Long = System.currentTimeMillis()
)

data class SystemProcess(
    val pid: Int,
    val user: String = "root",
    val stat: String = "S",
    val cpuPercent: Float = 0.0f,
    val memPercent: Float = 0.1f,
    val rssKb: Long = 512L,
    val time: String = "0:00.01",
    val command: String = "init"
)
