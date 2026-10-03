package com.example.game.vm

/**
 * Data-driven boot sequence generator for GenesisOS / custom guest OS.
 */
class BootSequenceGenerator(
    private val stages: List<BootStage> = DEFAULT_BOOT_STAGES
) {

    companion object {
        val DEFAULT_BOOT_STAGES = listOf(
            BootStage(
                id = "bios",
                title = "SeaBIOS POST",
                logMessage = "[   0.000000] SeaBIOS (version 1.15.0-rel-0-genesis-x86_64)",
                requiredQuestId = null
            ),
            BootStage(
                id = "machine",
                title = "Hardware Machine Spec",
                logMessage = "[   0.000120] Machine: i440FX + PIIX, 64 MB Identity Mapped RAM, Dual PIC 8259A",
                requiredQuestId = null
            ),
            BootStage(
                id = "mbr",
                title = "MBR Bootsector 0xAA55",
                logMessage = "[   0.001100] Booting from Hard Disk (0x80)... Validating MBR Magic 0xAA55",
                requiredQuestId = "Q1_1_MBR_MAGIC",
                isCriticalFailure = true,
                failureMessage = "[   0.001200] [FATAL BOOT ERROR] Bootsector missing 0xAA55 magic signature! System halted."
            ),
            BootStage(
                id = "gdt",
                title = "GDT & Protected Mode",
                logMessage = "[   0.003100] Switching CPU to 32-bit Protected Mode (CR0.PE=1)... Loading GDT descriptor",
                requiredQuestId = "Q1_2_GDT_PROTECTED_MODE",
                isCriticalFailure = true,
                failureMessage = "[   0.003200] [PANIC] Invalid GDT segment limits! CPU triple fault in Real Mode."
            ),
            BootStage(
                id = "kernel_entry",
                title = "Kernel Entry Point",
                logMessage = "[   0.005000] Jumping to Kernel Entry Point (0x100000)... Hello {osName}!",
                requiredQuestId = null
            ),
            BootStage(
                id = "vga",
                title = "VGA Text Mode Driver",
                logMessage = "[   0.008900] Initializing VGA Text Mode Buffer at 0xB8000 (80x25 characters, 16 colors)",
                requiredQuestId = "Q4_1_VGA_DRIVER",
                isCriticalFailure = false,
                warningMessage = "[   0.009000] [WARN] VGA Driver missing. Falling back to headless serial output (COM1)."
            ),
            BootStage(
                id = "idt",
                title = "IDT Interrupt Descriptor Table",
                logMessage = "[   0.012400] Loading IDT Table (256 gates)... Remapping Master/Slave PIC 8259A to 0x20-0x2F",
                requiredQuestId = "Q3_1_IDT_SETUP",
                isCriticalFailure = true,
                failureMessage = "[   0.012500] [PANIC] IDT not loaded! Unhandled Double Fault (Exception 0x08)."
            ),
            BootStage(
                id = "paging",
                title = "Paging MMU Initialization",
                logMessage = "[   0.018000] Initializing 2-level paging MMU (Page Directory at 0x9C000, CR0.PG=1)...",
                requiredQuestId = "Q2_1_PAGING_INIT",
                isCriticalFailure = true,
                failureMessage = "[   0.018100] [PANIC] Page Fault (CR2=0xC0000000) with Paging disabled. Kernel halted."
            ),
            BootStage(
                id = "pit_timer",
                title = "PIT Timer 100Hz",
                logMessage = "[   0.024000] Configuring 8254 PIT Timer to 100Hz (Channel 0, IRQ0, divisor=11932)...",
                requiredQuestId = "Q4_2_PIT_TIMER",
                isCriticalFailure = false,
                warningMessage = "[   0.024100] [WARN] PIT Timer unconfigured. System clock tick will be unsynchronized."
            ),
            BootStage(
                id = "keyboard",
                title = "PS/2 Keyboard Driver",
                logMessage = "[   0.031000] Initializing PS/2 Keyboard Controller on Port 0x60/0x64 (IRQ1)...",
                requiredQuestId = "Q4_3_KEYBOARD_DRIVER",
                isCriticalFailure = false,
                warningMessage = "[   0.031100] [WARN] PS/2 keyboard driver not found. Interactive input limited."
            ),
            BootStage(
                id = "scheduler",
                title = "Multitasking & Process Scheduler",
                logMessage = "[   0.038000] Starting Preemptive Round-Robin Process Scheduler (context switch on IRQ0)...",
                requiredQuestId = "Q5_1_CONTEXT_SWITCH",
                isCriticalFailure = false,
                warningMessage = "[   0.038100] [WARN] Context switching disabled. Single-tasking mode active."
            ),
            BootStage(
                id = "vfs",
                title = "Virtual File System",
                logMessage = "[   0.045000] Mounting root Virtual File System (VFS) on / (devfs, procfs, sysfs)...",
                requiredQuestId = "Q6_1_VFS_INODE",
                isCriticalFailure = false,
                warningMessage = "[   0.045100] [WARN] VFS not mounted. Disk storage operations unavailable."
            ),
            BootStage(
                id = "syscalls",
                title = "System Call Interface",
                logMessage = "[   0.052000] Registering System Call handler on INT 0x80 (sys_read, sys_write, sys_exit)...",
                requiredQuestId = "Q6_2_SYSCALL_INT80",
                isCriticalFailure = false,
                warningMessage = "[   0.052100] [WARN] System calls not registered. Userland applications will fail."
            ),
            BootStage(
                id = "userland_init",
                title = "Userland Shell Init",
                logMessage = "[   0.060000] Spawning PID 1 /bin/init and launching /bin/gsh (Genesis Shell)...",
                requiredQuestId = null
            )
        )
    }

    fun generateBootSequence(osName: String, completedQuestIds: Set<String>): BootSequenceResult {
        val logs = mutableListOf<String>()
        var hasPanic = false
        var failedStage: BootStage? = null

        for (stage in stages) {
            val formattedMsg = stage.logMessage.replace("{osName}", osName)
            logs.add(formattedMsg)

            if (stage.requiredQuestId != null && !completedQuestIds.contains(stage.requiredQuestId)) {
                if (stage.isCriticalFailure) {
                    val failure = stage.failureMessage?.replace("{osName}", osName)
                        ?: "[   CRITICAL FAILURE ] Stage '${stage.title}' failed!"
                    logs.add(failure)
                    hasPanic = true
                    failedStage = stage
                    break
                } else if (stage.warningMessage != null) {
                    val warn = stage.warningMessage.replace("{osName}", osName)
                    logs.add(warn)
                }
            }
        }

        if (!hasPanic) {
            logs.add("═══════════════════════════════════════════════════════════════")
            logs.add("   $osName Bare-Metal Kernel v1.0 Booted Successfully!         ")
            logs.add("═══════════════════════════════════════════════════════════════")
        }

        return BootSequenceResult(logs = logs, hasPanic = hasPanic, failedStage = failedStage)
    }
}
