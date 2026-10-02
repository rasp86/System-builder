package com.example.data.game

data class TerminalLine(
    val text: String,
    val type: TerminalLineType = TerminalLineType.OUTPUT
)

enum class TerminalLineType {
    INPUT,
    OUTPUT,
    SUCCESS,
    ERROR,
    WARNING,
    HEADER,
    MATRIX,
    SYSTEM
}

object OsSimulationEngine {

    fun generateBootSequence(
        osName: String,
        completedQuestIds: Set<String>
    ): List<String> {
        val logs = mutableListOf<String>()

        logs.add("[   0.000000] SeaBIOS (version 1.15.0-x86_64)")
        logs.add("[   0.000100] Machine: QEMU Standard PC (i440FX + PIIX, 1996)")
        logs.add("[   0.000200] CPU: GenuineIntel 4-Core x86_64 Virtual CPU @ 3.40GHz")
        logs.add("[   0.000450] Scanning PCI buses... Found 00:01.0 IDE, 00:02.0 VGA, 00:03.0 RTL8139")

        if (!completedQuestIds.contains("Q1_1_MBR_MAGIC")) {
            logs.add("[   0.001000] Booting from Hard Disk...")
            logs.add("[   FATAL ERROR ] No bootable device: 0xAA55 magic signature not found in sector 0!")
            logs.add("[   SYSTEM HALTED ] Execution stopped at 0x0000:0x7C00")
            return logs
        }

        logs.add("[   0.001100] Booting from Hard Disk: Valid MBR found at 0x7C00 (Signature: 0xAA55)")
        logs.add("[   0.002300] >>> $osName Stage 1 Bootloader executing in 16-bit Real Mode...")

        if (!completedQuestIds.contains("Q1_2_GDT_PROTECTED_MODE")) {
            logs.add("[   0.003000] Loading GDT... FAILED! No 32-bit GDT table defined.")
            logs.add("[   PANIC ] Unable to enter 32-bit Protected Mode (CR0 PE unset). CPU Halted.")
            return logs
        }

        logs.add("[   0.003100] Loading GDT (Null, Kernel Code 0x08, Kernel Data 0x10)... OK")
        logs.add("[   0.003500] Setting CR0.PE=1 -> Switched to 32-bit Protected Mode!")
        logs.add("[   0.004000] Far jump to 0x08:0x00100000 -> Jumping to 32-bit C Kernel Entry...")

        if (!completedQuestIds.contains("Q2_1_VGA_TERMINAL")) {
            logs.add("[   0.005000] Initializing VGA text buffer (0xB8000)... FAILED!")
            logs.add("[   PANIC ] No video output device available. Console redirected to null.")
            return logs
        }

        logs.add("[   0.005100] Video: VGA 80x25 Color Console initialized at physical 0xB8000")
        logs.add("[   0.006200] ==================================================")
        logs.add("[   0.006300]           WELCOME TO $osName KERNEL v0.9-LTS      ")
        logs.add("[   0.006400] ==================================================")

        if (completedQuestIds.contains("Q2_2_IDT_INTERRUPTS")) {
            logs.add("[   0.007000] Remapping PIC 8259A (Master=0x20..0x27, Slave=0x28..0x2F)... OK")
            logs.add("[   0.007500] Loading IDTR: 256 Interrupt gates initialized in IDT")
            logs.add("[   0.008000] Enabling interrupts (STI)... Timer IRQ0 active at 100Hz")
        } else {
            logs.add("[   WARNING ] IDT not configured! Interrupts disabled (CLI active).")
        }

        if (completedQuestIds.contains("Q3_1_PAGING_MMU")) {
            logs.add("[   0.009000] MMU: Identity mapping lower 4MB RAM (Page Table at 0x0009C000)")
            logs.add("[   0.009500] Enabling Paging: CR3=0x0009C000, CR0.PG=1 -> Virtual memory ONLINE")
            logs.add("[   0.010000] Kernel Heap Allocator (kmalloc): 32MB physical pool available")
        }

        if (completedQuestIds.contains("Q4_1_ROUND_ROBIN_SCHEDULER")) {
            logs.add("[   0.011000] Task Scheduler: Preemptive Round-Robin Multi-threading initialized")
            logs.add("[   0.011500] Spawning PID 0 (kernel_idle), PID 1 (init_task), PID 2 (klogd)")
        }

        if (completedQuestIds.contains("Q5_1_VFS_INODES")) {
            logs.add("[   0.012000] VFS: Mounting root filesystem '/' (Ramdisk 16MB) ... OK")
            logs.add("[   0.012500] Creating special nodes: /dev/null, /dev/zero, /dev/tty0, /etc")
        }

        if (completedQuestIds.contains("Q6_1_PS2_KEYBOARD")) {
            logs.add("[   0.013000] PS/2 Controller: Keyboard driver bound to IRQ 1 (Scancode Set 1)")
        }

        if (completedQuestIds.contains("Q7_1_SYSCALLS_AND_SHELL")) {
            logs.add("[   0.014000] Registering POSIX Syscall Gateway (INT 0x80) -> Ring 3 enabled")
            logs.add("[   0.014500] Executing /bin/sh -> Shell prompt ready on /dev/tty0")
        }

        if (completedQuestIds.contains("Q8_1_GUI_FRAMEBUFFER")) {
            logs.add("[   0.015000] VESA/VBE Graphics: 800x600x32bpp Linear Framebuffer at 0xE0000000")
            logs.add("[   0.015500] Starting GenesisOS Window Manager (Desktop Compositor)... OK")
        }

        if (completedQuestIds.contains("Q9_1_NET_STACK")) {
            logs.add("[   0.016000] RTL8139: MAC 52:54:00:12:34:56, IP 192.168.1.42/24 assigned")
            logs.add("[   0.016500] TCP/IP Stack & Genesis HTTP Daemon running on port 80")
        }

        logs.add("[   0.020000] System startup completed successfully. Type 'help' for commands.")
        return logs
    }

    fun executeGuestOsCommand(command: String, osName: String, completedQuestIds: Set<String>): List<String> {
        val trimmed = command.trim()
        val parts = trimmed.split("\\s+".toRegex())
        val cmd = parts.getOrNull(0)?.lowercase() ?: ""
        val args = parts.drop(1)

        val out = mutableListOf<String>()

        when (cmd) {
            "help" -> {
                out.add("=== $osName Built-in Guest Commands ===")
                out.add("  help              - Wyświetla listę poleceń")
                out.add("  uname -a          - Informacje o architekturze jądra")
                out.add("  sysinfo / fetch   - Informacje o systemie i banner ASCII")
                out.add("  ls [dir]          - Wylistuj pliki w wirtualnym systemie plików")
                out.add("  cat <file>        - Wyświetl zawartość pliku")
                out.add("  ps / top          - Lista aktywnych procesów w Round-Robin")
                out.add("  mem               - Użycie pamięci RAM i stronicowania MMU")
                out.add("  calc <wyrażenie>  - Prosty kalkulator jądra")
                out.add("  matrix            - Efekt deszczu kodu Matrix")
                out.add("  clear             - Czyści ekran wirtualnego monitora")
                out.add("  reboot / poweroff - Restart lub wyłączenie maszyny wirtualnej")
                if (completedQuestIds.contains("Q8_1_GUI_FRAMEBUFFER")) {
                    out.add("  gui               - Uruchom graficzny menedżer okien VBE")
                }
            }
            "uname" -> {
                out.add("$osName 1.0.0-LTS #42 SMP PREEMPT i386-pc-elf GNU/Genesis")
            }
            "sysinfo", "fetch", "neofetch" -> {
                out.add("     ______                 _      ")
                out.add("    / ____/__  ____  ___   (_)____ ")
                out.add("   / / __/ _ \\/ __ \\/ _ \\ / / ___/ ")
                out.add("  / /_/ /  __/ / / /  __// (__  )  ")
                out.add("  \\____/\\___/_/ /_/\\___//_/____/   ")
                out.add("------------------------------------")
                out.add("OS: $osName 1.0 x86 Protected Mode")
                out.add("Kernel: Custom Monolithic Micro-hybrid")
                out.add("Uptime: 0h 42m 13s")
                out.add("Memory: 14.8 MB / 64.0 MB (Paging 4KB)")
                out.add("Shell: /bin/gsh (Genesis Shell)")
                out.add("Display: VGA 80x25 / VBE 800x600 32bpp")
                out.add("Arch: x86 (IA-32) Ring 0/Ring 3")
            }
            "ls" -> {
                val dir = args.getOrNull(0) ?: "/"
                out.add("Zawartość katalogu $dir:")
                out.add("  drwxr-xr-x  2 root root  4096 /bin")
                out.add("  drwxr-xr-x  2 root root  4096 /boot")
                out.add("  drwxr-xr-x  2 root root  4096 /dev")
                out.add("  drwxr-xr-x  2 root root  4096 /etc")
                out.add("  drwxr-xr-x  2 root root  4096 /home")
                out.add("  -rwxr-xr-x  1 root root 28416 /kernel.bin")
                out.add("  -rw-r--r--  1 root root   342 /etc/os-release")
            }
            "cat" -> {
                val file = args.getOrNull(0) ?: ""
                when {
                    file.contains("os-release") -> {
                        out.add("NAME=\"$osName\"")
                        out.add("VERSION=\"1.0.0-LTS\"")
                        out.add("ID=genesisos")
                        out.add("PRETTY_NAME=\"$osName 1.0 x86 Kernel\"")
                    }
                    file.contains("kernel") -> {
                        out.add("[BINARNY PLIK ELF: Header 0x7F 'E' 'L' 'F', Entry 0x00100000]")
                    }
                    file.isBlank() -> out.add("Użycie: cat <ścieżka do pliku>")
                    else -> out.add("Plik '$file' (rozmiar: 128 B, Inode: 42, VFS Mounted)")
                }
            }
            "ps", "top" -> {
                out.add("PID   USER    STATE    RING   MEM(KB)   COMMAND")
                out.add("  0   root    RUNNING    0        128   [kidle]")
                out.add("  1   root    READY      0        512   [init]")
                out.add("  2   root    READY      0        256   [klogd]")
                out.add("  3   user    RUNNING    3       1024   /bin/sh")
                if (completedQuestIds.contains("Q8_1_GUI_FRAMEBUFFER")) {
                    out.add("  4   user    SLEEPING   3       4096   /bin/wm_compositor")
                }
                if (completedQuestIds.contains("Q9_1_NET_STACK")) {
                    out.add("  5   root    READY      0        512   [http_daemon]")
                }
            }
            "mem" -> {
                out.add("=== GenesisOS MMU Memory Statistics ===")
                out.add("Paging Status: ACTIVE (CR0.PG=1, CR3=0x0009C000)")
                out.add("Page Directories: 1 (1024 Entries)")
                out.add("Page Tables: 4 (Identity mapped lower 16MB)")
                out.add("Total Physical RAM: 65,536 KB (64 MB)")
                out.add("Used Memory: 15,240 KB (Kernel + Buffers)")
                out.add("Free Memory: 50,296 KB")
            }
            "calc" -> {
                val expr = args.joinToString(" ")
                if (expr.isBlank()) {
                    out.add("Użycie: calc <liczba> <op> <liczba> (np. calc 42 * 8)")
                } else {
                    try {
                        val sanitized = expr.replace(" ", "")
                        val res = when {
                            sanitized.contains("+") -> {
                                val s = sanitized.split("+"); s[0].toLong() + s[1].toLong()
                            }
                            sanitized.contains("-") -> {
                                val s = sanitized.split("-"); s[0].toLong() - s[1].toLong()
                            }
                            sanitized.contains("*") -> {
                                val s = sanitized.split("*"); s[0].toLong() * s[1].toLong()
                            }
                            sanitized.contains("/") -> {
                                val s = sanitized.split("/"); s[0].toLong() / s[1].toLong()
                            }
                            else -> 42L
                        }
                        out.add("Wynik: $res (Hex: 0x${java.lang.Long.toHexString(res).uppercase()})")
                    } catch (e: Exception) {
                        out.add("Błąd składni kalkulatora.")
                    }
                }
            }
            "matrix" -> {
                out.add("01001111 01010011 00100000 01000001 01110010 01100011 01101000")
                out.add("10101010 11001100 00110011 11110000 00001111 10100101 01011010")
                out.add("SYSTEM_BREACH_DETECTED... OK: Root access granted to Kernel Ring 0.")
            }
            "clear" -> {
                // Triggered in UI
            }
            "" -> {
                // Empty
            }
            else -> {
                out.add("gsh: nieznane polecenie '$cmd'. Wpisz 'help' aby zobaczyć listę komend.")
            }
        }

        return out
    }
}
