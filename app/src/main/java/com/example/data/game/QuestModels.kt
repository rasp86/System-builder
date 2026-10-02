package com.example.data.game

data class TestCase(
    val description: String,
    val check: (code: String) -> Pair<Boolean, String>
)

data class Quest(
    val id: String,
    val phase: Int,
    val phaseName: String,
    val title: String,
    val category: String,
    val storyPrompt: String,
    val conceptExplanation: String,
    val taskInstructions: String,
    val filePath: String,
    val defaultCode: String,
    val referenceSolution: String,
    val testCases: List<TestCase>,
    val xpReward: Int,
    val bitsReward: Int,
    val unlockedPerk: String
)

enum class OSBootStatus {
    POWERED_OFF,
    BIOS_POST,
    BOOTLOADER_RUNNING,
    KERNEL_BOOTING,
    RUNNING_CLI,
    RUNNING_GUI,
    KERNEL_PANIC
}

data class CpuRegisters(
    val eax: String = "0x00000000",
    val ebx: String = "0x00000000",
    val ecx: String = "0x00000000",
    val edx: String = "0x00000000",
    val esp: String = "0x00090000",
    val ebp: String = "0x00090000",
    val eip: String = "0x00100000",
    val cr0: String = "0x80000011", // PG, PE
    val cr3: String = "0x0009C000", // Page Directory
    val eflags: String = "0x00000202", // IF set
    val cs: String = "0x08",
    val ds: String = "0x10"
)

data class OSProcess(
    val pid: Int,
    val name: String,
    val state: String, // "RUNNING", "SLEEPING", "READY"
    val memoryKb: Int,
    val priority: Int,
    val ring: Int // 0 (Kernel) or 3 (User)
)
