package com.example.data.game.engine

import com.example.data.game.SystemProcess
import com.example.data.game.TerminalLine
import com.example.data.game.TerminalLineType
import java.util.Locale

/**
 * Manages the simulated multitasking process table and signal dispatching.
 */
class ProcessScheduler(
    private val dmesg: DmesgRingBuffer
) {

    val backgroundProcesses = mutableListOf(
        SystemProcess(1, "root", "S", 0.0f, 0.4f, 4096, "0:02", "/sbin/init [systemd-genesis]"),
        SystemProcess(2, "root", "S", 0.0f, 0.0f, 0, "0:00", "[kthreadd]"),
        SystemProcess(3, "root", "I", 0.1f, 0.0f, 0, "0:01", "[ksoftirqd/0]"),
        SystemProcess(4, "root", "I", 0.0f, 0.0f, 0, "0:00", "[kworker/0:0-events]"),
        SystemProcess(10, "root", "S", 0.0f, 0.0f, 0, "0:00", "[rcu_sched]"),
        SystemProcess(42, "root", "S", 0.2f, 2.1f, 8192, "0:05", "/usr/bin/genesis-compiler --daemon"),
        SystemProcess(108, "dev", "S", 1.2f, 8.4f, 32768, "0:12", "/usr/bin/qemu-system-i386 -fda boot.bin"),
        SystemProcess(1337, "dev", "R", 0.8f, 1.2f, 4096, "0:01", "/bin/gsh (Lead Architect Host Shell)"),
        SystemProcess(2048, "root", "S", 0.1f, 4.2f, 16384, "0:03", "/usr/bin/ada-mentor-daemon")
    )

    fun listProcesses(): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        lines.add(TerminalLine("╔═══════════════════════════════════════════════════════════════════════════════╗", TerminalLineType.HEADER))
        lines.add(TerminalLine("║              GENESIS OS PROCESS TABLE & SCHEDULER (ps aux)                   ║", TerminalLineType.HEADER))
        lines.add(TerminalLine("╚═══════════════════════════════════════════════════════════════════════════════╝", TerminalLineType.HEADER))
        lines.add(TerminalLine("  PID USER       STAT   %CPU  %MEM    RSS/SZ     TIME COMMAND", TerminalLineType.SYSTEM))
        backgroundProcesses.forEach { p ->
            val pidStr = p.pid.toString().padStart(5)
            val userStr = p.user.padEnd(10)
            val statStr = p.stat.padEnd(4)
            val cpuStr = String.format(Locale.US, "%5.1f", p.cpuPercent)
            val memStr = String.format(Locale.US, "%5.1f", p.memPercent)
            val memKbStr = "${p.rssKb} KB".padStart(10)
            val timeStr = p.time.padStart(7)
            val lineType = when (p.stat) {
                "R" -> TerminalLineType.SUCCESS
                "D" -> TerminalLineType.WARNING
                "Z" -> TerminalLineType.ERROR
                else -> TerminalLineType.OUTPUT
            }
            lines.add(TerminalLine("$pidStr $userStr $statStr $cpuStr $memStr $memKbStr $timeStr ${p.command}", lineType))
        }
        val runningCount = backgroundProcesses.count { it.stat == "R" }
        val totalMemKb = backgroundProcesses.sumOf { it.rssKb }
        lines.add(TerminalLine(""))
        lines.add(TerminalLine("• Procesy: ${backgroundProcesses.size} łącznie (Aktywne: $runningCount, Uśpione: ${backgroundProcesses.size - runningCount}) | Pamięć procesów: ${totalMemKb / 1024} MB", TerminalLineType.SYSTEM))
        lines.add(TerminalLine("• Planista zadań: Round-Robin Preemptive Multi-Tasking (Ring 0 / Ring 3)", TerminalLineType.OUTPUT))
        return lines
    }

    fun killProcess(args: List<String>, isByName: Boolean = false): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        if (args.isEmpty()) {
            lines.add(TerminalLine("╔══════════════════════════════════════════════════════════════════╗", TerminalLineType.HEADER))
            lines.add(TerminalLine("║         GENESIS OS PROCESS TERMINATION UTILITY (kill)            ║", TerminalLineType.HEADER))
            lines.add(TerminalLine("╚══════════════════════════════════════════════════════════════════╝", TerminalLineType.HEADER))
            lines.add(TerminalLine("SKŁADNIA: kill [-SYGNAŁ] <PID...> | killall <NAZWA_PROCESU>", TerminalLineType.SYSTEM))
            lines.add(TerminalLine("OBSŁUGIWANE SYGNAŁY:", TerminalLineType.OUTPUT))
            lines.add(TerminalLine("  -9,  -SIGKILL   ➔ Natychmiastowe bezwarunkowe ubicie procesu (Ring 0 force terminate)", TerminalLineType.OUTPUT))
            lines.add(TerminalLine("  -15, -SIGTERM   ➔ Standardowe żądanie zakończenia (graceful shutdown, domyślny)", TerminalLineType.OUTPUT))
            lines.add(TerminalLine("  -l,  -list      ➔ Wyświetl listę wszystkich dostępnych sygnałów POSIX", TerminalLineType.OUTPUT))
            lines.add(TerminalLine(""))
            lines.add(TerminalLine("AKTYWNE PROCESY W SYSTEMIE (Wpisz 'ps' aby zobaczyć szczegóły):", TerminalLineType.SYSTEM))
            backgroundProcesses.forEach { p ->
                val desc = if (p.pid in 1..2) "[Chroniony przez jądro Ring 0]" else "[Można zakończyć]"
                val type = if (p.pid in 1..2) TerminalLineType.SYSTEM else TerminalLineType.OUTPUT
                lines.add(TerminalLine("  • PID ${p.pid.toString().padStart(4)}: ${p.command.padEnd(36)} (RAM: ${p.rssKb} KB) $desc", type))
            }
            return lines
        }

        if (args[0] == "-l" || args[0] == "-list" || args[0] == "--list") {
            lines.add(TerminalLine("=== LISTA SYGNAŁÓW POSIX / GENESIS KERNEL ===", TerminalLineType.HEADER))
            lines.add(TerminalLine(" 1) SIGHUP       2) SIGINT       3) SIGQUIT      4) SIGILL", TerminalLineType.OUTPUT))
            lines.add(TerminalLine(" 6) SIGABRT      8) SIGFPE       9) SIGKILL     11) SIGSEGV", TerminalLineType.OUTPUT))
            lines.add(TerminalLine("13) SIGPIPE     14) SIGALRM     15) SIGTERM     17) SIGCHLD", TerminalLineType.OUTPUT))
            lines.add(TerminalLine("18) SIGCONT     19) SIGSTOP     20) SIGTSTP     28) SIGWINCH", TerminalLineType.OUTPUT))
            return lines
        }

        var signalNum = 15
        var signalName = "SIGTERM"
        val targetTokens = mutableListOf<String>()

        for (token in args) {
            when {
                token == "-9" || token.equals("-SIGKILL", ignoreCase = true) || token.equals("-KILL", ignoreCase = true) -> {
                    signalNum = 9
                    signalName = "SIGKILL"
                }
                token == "-15" || token.equals("-SIGTERM", ignoreCase = true) || token.equals("-TERM", ignoreCase = true) -> {
                    signalNum = 15
                    signalName = "SIGTERM"
                }
                token == "-2" || token.equals("-SIGINT", ignoreCase = true) || token.equals("-INT", ignoreCase = true) -> {
                    signalNum = 2
                    signalName = "SIGINT"
                }
                token == "-1" || token.equals("-SIGHUP", ignoreCase = true) || token.equals("-HUP", ignoreCase = true) -> {
                    signalNum = 1
                    signalName = "SIGHUP"
                }
                token.startsWith("-") && token.drop(1).toIntOrNull() != null -> {
                    val s = token.drop(1).toInt()
                    signalNum = s
                    signalName = "SIG#$s"
                }
                else -> {
                    targetTokens.add(token)
                }
            }
        }

        if (targetTokens.isEmpty()) {
            lines.add(TerminalLine("kill: Wymagany identyfikator PID lub nazwa procesu. Użyj 'kill <PID>' lub 'ps'.", TerminalLineType.WARNING))
            return lines
        }

        targetTokens.forEach { token ->
            val pidCandidate = token.toIntOrNull()
            val targetProcs = if (!isByName && pidCandidate != null) {
                backgroundProcesses.filter { it.pid == pidCandidate }
            } else {
                backgroundProcesses.filter { it.command.contains(token, ignoreCase = true) || (pidCandidate != null && it.pid == pidCandidate) }
            }

            if (targetProcs.isEmpty()) {
                lines.add(TerminalLine("kill: ($token) - Brak takiego procesu. Wpisz 'ps' aby wyświetlić aktywne procesy.", TerminalLineType.ERROR))
            } else {
                targetProcs.toList().forEach { proc ->
                    if (proc.pid == 1 || proc.pid == 2) {
                        lines.add(TerminalLine("kill: (PID ${proc.pid} '${proc.command}') - Operacja zabroniona: Ochrona jądra Ring 0 uniemożliwia zatrzymanie procesu init/kthreadd.", TerminalLineType.ERROR))
                    } else {
                        backgroundProcesses.remove(proc)
                        dmesg.log("scheduler/kill", "WARN", "Process PID ${proc.pid} (${proc.command}) terminated by signal $signalName ($signalNum)")
                        lines.add(TerminalLine("✔ [$signalName (Sygnał $signalNum)] Proces PID ${proc.pid} ('${proc.command}') został pomyślnie zakończony.", TerminalLineType.SUCCESS))
                        lines.add(TerminalLine("  • Zwolniono zasoby: ${proc.rssKb} KB RAM (STAT=${proc.stat}, CPU=${proc.cpuPercent}%)", TerminalLineType.OUTPUT))
                        lines.add(TerminalLine("  • Zaktualizowano tabelę procesów planisty. Pozostało procesów: ${backgroundProcesses.size}. (Wpisz 'ps' aby sprawdzić)", TerminalLineType.SYSTEM))
                    }
                }
            }
        }

        return lines
    }
}
