package com.example.game.terminal.commands

import com.example.data.game.TerminalLine
import com.example.data.game.TerminalLineType
import com.example.game.terminal.CommandContext
import com.example.game.terminal.TerminalCommand
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HelpCommand : TerminalCommand {
    override val name = "help"
    override val aliases = listOf("?")
    override val description = "Wyświetla listę dostępnych komend i narzędzi"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        lines.add(TerminalLine("╔════════════════════════════════════════════════════════════════╗", TerminalLineType.HEADER))
        lines.add(TerminalLine("║      GENESIS OS ARCHITECT – POMOC SYSTEMU HOST (/bin/sh)      ║", TerminalLineType.HEADER))
        lines.add(TerminalLine("╚════════════════════════════════════════════════════════════════╝", TerminalLineType.HEADER))
        lines.add(TerminalLine("─── KERNEL & MISJE ──────────────────────────────────────────────", TerminalLineType.SYSTEM))
        lines.add(TerminalLine("  quests               - Lista wszystkich faz i zadań systemu operacyjnego", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  quest <id>           - Wybór i otwarcie zadania (np. quest Q1_1)", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  build                - Kompilacja aktywnego modułu jądra", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  test                 - Uruchomienie testów akceptacyjnych", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  boot / run           - Uruchomienie maszyny wirtualnej QEMU", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  hint                 - Wskazówka techniczna do bieżącego zadania", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  ai <pytanie>         - Konsultacja z mentorką Adą (AI Kernel Architect)", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("─── PLIKI & VFS ─────────────────────────────────────────────────", TerminalLineType.SYSTEM))
        lines.add(TerminalLine("  ls [-l] [-a] [path]  - Lista plików i katalogów w wirtualnym VFS", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  cat <path>           - Wyświetlenie zawartości pliku", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  touch <path>         - Utworzenie pustego pliku", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  mkdir <path>         - Utworzenie nowego katalogu", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  rm [-r] <path>       - Usunięcie pliku lub katalogu", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  chmod <octal> <path> - Zmiana uprawnień (np. chmod 755 /bin/sh)", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  df [-h]              - Statystyki pamięci dyskowej VFS", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  tree [path]          - Drzewiasta struktura katalogów VFS", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  grep <wzorzec> <plik>- Wyszukiwanie tekstu w pliku", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("─── HARDWARE & PROCESY ──────────────────────────────────────────", TerminalLineType.SYSTEM))
        lines.add(TerminalLine("  ps                   - Lista procesów w schedulerze jądra", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  kill <PID>           - Wysłanie sygnału SIGKILL do procesu", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  reg                  - Odczyt rejestrów procesora x86 (EAX, EBX, CR0...)", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  mem <hex_addr>       - Zrzut pamięci RAM (hexdump)", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  asm <instrukcja>     - Asemblacja instrukcji x86 w locie", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  dmesg [-c] [-l lvl]  - Bufor komunikatów jądra", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  uptime               - Czas działania systemu i obciążenie CPU", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("─── POWŁOKA & PERSONALIZACJA ────────────────────────────────────", TerminalLineType.SYSTEM))
        lines.add(TerminalLine("  alias [k=w]          - Definiowanie i przeglądanie aliasów", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  unalias <name>       - Usuwanie zdefiniowanego aliasu", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  history              - Historia wykonanych poleceń", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  theme <nazwa>        - Zmiana schematu kolorów (amber, green, cyan...)", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  clear / cls          - Wyczyszczenie ekranu terminala", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  snapshot save/list   - Zarządzanie migawkami stanu systemu", TerminalLineType.OUTPUT))
        return lines
    }
}

class UptimeCommand : TerminalCommand {
    override val name = "uptime"
    override val description = "Podaje czas działania systemu i wskaźniki obciążenia"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
        val uptimeSeconds = (System.currentTimeMillis() - context.getBootTimeMillis()) / 1000
        val hours = uptimeSeconds / 3600
        val mins = (uptimeSeconds % 3600) / 60
        val secs = uptimeSeconds % 60
        val timeStr = String.format(Locale.US, "%02d:%02d:%02d", hours, mins, secs)
        val processesCount = context.processScheduler.backgroundProcesses.size

        return listOf(
            TerminalLine(
                " $timeStr up $hours:$mins, $processesCount processes, load average: 0.12, 0.08, 0.04",
                TerminalLineType.OUTPUT
            )
        )
    }
}

class WhoamiCommand : TerminalCommand {
    override val name = "whoami"
    override val description = "Wyświetla aktualnego użytkownika"
    override fun execute(args: List<String>, context: CommandContext) =
        listOf(TerminalLine("root (Architect of GenesisOS)", TerminalLineType.SUCCESS))
}

class ArchCommand : TerminalCommand {
    override val name = "arch"
    override val aliases = listOf("specs", "uname")
    override val description = "Architektura procesora i specyfikacja emulowanej maszyny"

    override fun execute(args: List<String>, context: CommandContext) = listOf(
        TerminalLine("CPU: Genesis Virtual x86-32 (Protected Mode / Long Mode Capable)", TerminalLineType.OUTPUT),
        TerminalLine("Paging: Two-level 4KB pages / PAE 36-bit ready", TerminalLineType.OUTPUT),
        TerminalLine("Memory: 64 MB Identity-Mapped Physical RAM (0x00000000 - 0x03FFFFFF)", TerminalLineType.OUTPUT),
        TerminalLine("PIC: Dual 8259A Cascaded (IRQ 0-15 remapped to INT 0x20-0x2F)", TerminalLineType.OUTPUT),
        TerminalLine("Timer: 8254 PIT @ 1193182 Hz (Channel 0 IRQ0)", TerminalLineType.OUTPUT),
        TerminalLine("Display: Standard VGA 80x25 Text Mode (0xB8000) / Framebuffer (0xFD000000)", TerminalLineType.OUTPUT),
        TerminalLine("Storage: Primary IDE Master IDE-0 (LBA28 PIO Mode, 128 MB Virtual Drive)", TerminalLineType.OUTPUT)
    )
}

class VersionCommand : TerminalCommand {
    override val name = "version"
    override val description = "Wersja jądra i kompilatora"
    override fun execute(args: List<String>, context: CommandContext) = listOf(
        TerminalLine("Genesis OS Kernel Architect Engine v1.0.0-RELEASE", TerminalLineType.HEADER),
        TerminalLine("Host Platform: Android Jetpack Compose runtime", TerminalLineType.OUTPUT),
        TerminalLine("Virtual Toolchain: i686-elf-gcc (Genesis Project Toolchain) 13.2.0", TerminalLineType.OUTPUT),
        TerminalLine("Target Architecture: x86-32 / IA-32 Bare Metal Kernel", TerminalLineType.OUTPUT)
    )
}

class DateCommand : TerminalCommand {
    override val name = "date"
    override val description = "Bieżąca data i czas zegara CMOS RTC"
    override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
        val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US)
        return listOf(TerminalLine(sdf.format(Date()), TerminalLineType.OUTPUT))
    }
}

class PwdCommand : TerminalCommand {
    override val name = "pwd"
    override val description = "Bieżący katalog roboczy"
    override fun execute(args: List<String>, context: CommandContext) =
        listOf(TerminalLine("/usr/src/genesis-kernel/v1.0", TerminalLineType.OUTPUT))
}

class EchoCommand : TerminalCommand {
    override val name = "echo"
    override val description = "Wypisuje podany tekst na ekranie"
    override fun execute(args: List<String>, context: CommandContext) =
        listOf(TerminalLine(args.joinToString(" "), TerminalLineType.OUTPUT))
}

class ClearCommand : TerminalCommand {
    override val name = "clear"
    override val aliases = listOf("cls")
    override val description = "Czyści ekran terminala"
    override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
        // Clear is handled by TerminalEngine / ViewModel
        return emptyList()
    }
}

class HistoryCommand : TerminalCommand {
    override val name = "history"
    override val description = "Wyświetla historię wpisanych poleceń"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
        val history = context.getHistory()
        val lines = mutableListOf<TerminalLine>()
        lines.add(TerminalLine("╔════════════════════════════════════════════════════════════════╗", TerminalLineType.HEADER))
        lines.add(TerminalLine("║                  HISTORIA POLECEŃ POWŁOKI                      ║", TerminalLineType.HEADER))
        lines.add(TerminalLine("╚════════════════════════════════════════════════════════════════╝", TerminalLineType.HEADER))

        if (history.isEmpty()) {
            lines.add(TerminalLine("  (Brak poleceń w historii sesji)", TerminalLineType.OUTPUT))
        } else {
            val limit = args.firstOrNull()?.toIntOrNull() ?: 25
            val slice = history.takeLast(limit)
            val startIndex = maxOf(1, history.size - slice.size + 1)
            slice.forEachIndexed { index, cmd ->
                val lineNum = String.format(Locale.US, "%4d  ", startIndex + index)
                lines.add(TerminalLine("$lineNum$cmd", TerminalLineType.OUTPUT))
            }
        }
        return lines
    }
}
