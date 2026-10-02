package com.example.data.game.engine

import com.example.data.game.FsNodeType
import com.example.data.game.Quest
import com.example.data.game.QuestsData
import com.example.data.game.TerminalLine
import com.example.data.game.TerminalLineType
import com.example.data.game.VirtualFsNode
import com.example.data.local.OsSnapshotEntity
import com.example.data.local.OsSnapshotRepository
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dispatches terminal commands to dedicated subsystem handlers.
 */
class TerminalCommandDispatcher(
    private val vfsManager: VirtualFileSystemManager,
    private val cpuSim: HardwareCpuSimulator,
    private val processScheduler: ProcessScheduler,
    private val dmesgBuffer: DmesgRingBuffer,
    private val getBootTimeMillis: () -> Long,
    private val getThemeId: () -> String,
    private val setThemeId: (String) -> Unit,
    private val getSnapshotRepo: () -> OsSnapshotRepository?,
    private val getHistory: () -> List<String> = { emptyList() }
) {

    fun execute(
        commandStr: String,
        currentQuest: Quest?,
        userCode: String,
        completedQuestIds: Set<String>,
        onSelectQuest: (String) -> Unit,
        onRunTests: () -> Unit,
        onBootVm: () -> Unit,
        onAskAi: (String) -> Unit
    ): List<TerminalLine> {
        val trimmed = commandStr.trim()
        val rawParts = trimmed.split("\\s+".toRegex())
        val firstToken = rawParts.getOrNull(0)?.lowercase() ?: ""

        var effectiveCommand = trimmed
        if (vfsManager.customAliases.containsKey(firstToken)) {
            val target = vfsManager.customAliases[firstToken] ?: ""
            val rest = rawParts.drop(1).joinToString(" ")
            effectiveCommand = if (rest.isNotBlank()) "$target $rest" else target
        }

        val parts = effectiveCommand.split("\\s+".toRegex())
        val cmd = parts.getOrNull(0)?.lowercase() ?: ""
        val args = parts.drop(1)

        val lines = mutableListOf<TerminalLine>()
        lines.add(TerminalLine("> $commandStr", TerminalLineType.INPUT))

        when (cmd) {
            "help", "?" -> lines.addAll(handleHelp())
            "uptime" -> lines.addAll(handleUptime())
            "chmod" -> lines.addAll(handleChmod(args))
            "df" -> lines.addAll(handleDf(args))
            "ls" -> lines.addAll(handleLs(args))
            "cat" -> lines.addAll(handleCat(args))
            "alias" -> lines.addAll(handleAlias(args))
            "unalias" -> lines.addAll(handleUnalias(args))
            "kill", "pkill", "killall" -> lines.addAll(processScheduler.killProcess(args, cmd == "pkill" || cmd == "killall"))
            "ps" -> lines.addAll(processScheduler.listProcesses())
            "dmesg" -> lines.addAll(handleDmesg(args))
            "theme", "colorscheme", "scheme" -> lines.addAll(handleTheme(args))
            "man", "manual" -> lines.addAll(ManualPagesProvider.getManualPage(args.getOrNull(0) ?: "man"))
            "int", "interrupt" -> lines.addAll(handleInterrupt(args))
            "asm", "assemble" -> lines.addAll(handleAsm(args))
            "mem", "memview", "memory" -> lines.addAll(handleMem(args, currentQuest, userCode, completedQuestIds, onSelectQuest, onRunTests, onBootVm, onAskAi))
            "reg", "registers" -> lines.addAll(handleReg(args, currentQuest, userCode, completedQuestIds, onSelectQuest, onRunTests, onBootVm, onAskAi))
            "whoami" -> lines.addAll(handleWhoami())
            "date" -> lines.addAll(handleDate())
            "pwd" -> lines.add(TerminalLine("/usr/src/genesis-kernel/v1.0", TerminalLineType.OUTPUT))
            "echo" -> lines.add(TerminalLine(args.joinToString(" "), TerminalLineType.OUTPUT))
            "version" -> lines.addAll(handleVersion())
            "quests", "tasks" -> lines.addAll(handleQuests(completedQuestIds))
            "quest", "open" -> lines.addAll(handleQuestOpen(args, onSelectQuest))
            "build", "compile" -> lines.addAll(handleBuild(currentQuest))
            "test" -> {
                if (currentQuest == null) {
                    lines.add(TerminalLine("Wybierz najpierw zadanie (wpisz 'quests').", TerminalLineType.WARNING))
                } else {
                    lines.add(TerminalLine("Uruchamianie zestawu testów akceptacyjnych dla ${currentQuest.id}...", TerminalLineType.SYSTEM))
                    onRunTests()
                }
            }
            "boot", "run", "runos" -> {
                lines.add(TerminalLine("Inicjalizacja emulatora QEMU x86...", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("$ qemu-system-i386 -fda /build/boot.bin -m 64M -vga std", TerminalLineType.OUTPUT))
                onBootVm()
            }
            "hint" -> lines.addAll(handleHint(currentQuest))
            "ai", "ask" -> lines.addAll(handleAi(args, onAskAi))
            "arch", "specs" -> lines.addAll(handleArch())
            "status", "stats" -> lines.addAll(handleStatus(currentQuest, completedQuestIds))
            "history" -> lines.addAll(handleHistory())
            "touch" -> lines.addAll(handleTouch(args))
            "mkdir" -> lines.addAll(handleMkdir(args))
            "rm" -> lines.addAll(handleRm(args))
            "tree" -> lines.addAll(handleTree(args))
            "grep" -> lines.addAll(handleGrep(args))
            "save" -> lines.addAll(handleSave(args))
            "load" -> lines.addAll(handleLoad(args))
            "benchmark" -> lines.addAll(handleBenchmark())
            "clear", "cls" -> return emptyList()
            "" -> {}
            else -> {
                lines.add(
                    TerminalLine(
                        "Nieznane polecenie: '$cmd'. Wpisz 'help', 'ls', 'cat', 'chmod', 'df', 'uptime', 'grep', 'save', 'load' lub 'man' aby wyświetlić pomoc.",
                        TerminalLineType.ERROR
                    )
                )
            }
        }
        return lines
    }

    private fun handleHelp(): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        lines.add(TerminalLine("╔══════════════════════════════════════════════════════════════════╗", TerminalLineType.HEADER))
        lines.add(TerminalLine("║        GENESIS OS ARCHITECT - HOST DEVELOPER TOOLCHAIN           ║", TerminalLineType.HEADER))
        lines.add(TerminalLine("╚══════════════════════════════════════════════════════════════════╝", TerminalLineType.HEADER))
        lines.add(TerminalLine("SYSTEM PLIKÓW & DOKUMENTACJA:", TerminalLineType.SYSTEM))
        lines.add(TerminalLine("  ls [-l] [ścieżka]  - Wylistuj zawartość wirtualnego katalogu VFS", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  cat <plik>         - Wyświetl zawartość pliku (np. 'cat /etc/os-release')", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  chmod <mod> <plik> - Zmień uprawnienia pliku (np. 'chmod 755 /boot/boot.asm')", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  df [-h]            - Statystyki wykorzystania przestrzeni dyskowej VFS", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  grep <wzorzec> [p] - Wyszukaj wzorce tekstowe w plikach", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  touch <plik>       - Utwórz pusty plik lub odśwież znacznik czasu", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  mkdir [-p] <kat>   - Utwórz nowy katalog w VFS (np. 'mkdir /src')", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  rm [-r] <plik/kat> - Usuń plik lub katalog z VFS (np. 'rm /boot/test.asm')", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  tree [ścieżka]     - Wizualne drzewo struktury wirtualnego systemu plików", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  history            - Wyświetl ostatnie 20 wprowadzonych poleceń", TerminalLineType.OUTPUT))
        lines.add(TerminalLine(""))
        lines.add(TerminalLine("SYSTEM & MONITORING:", TerminalLineType.SYSTEM))
        lines.add(TerminalLine("  uptime             - Czas pracy systemu od rozruchu i obciążenie CPU", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  save [slot]        - Zapisz stan VFS i pamięci RAM do lokalnej bazy Room", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  load [slot]        - Załaduj stan VFS i pamięci RAM z bazy Room", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  benchmark          - Uruchom testy wydajnościowe ALU, MMU, IDT i VGA", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  alias [n='cmd']    - Twórz własne skróty poleceń (np. 'alias c=\"clear\"')", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  clear / cls        - Wyczyść ekran i bufor linii terminala", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  kill [-9] <PID>    - Zakończ działanie procesu systemowego (np. 'kill 108')", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  int <wektor_hex>   - Wywołaj przerwanie programowe (np. 'int 0x80')", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  ps [aux]           - Lista aktywnych procesów systemowych w tle", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  dmesg              - Zrzut logów bufora cyklicznego zdarzeń jądra", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  man [polecenie]    - Wyświetl szczegółowy podręcznik systemowy Unix", TerminalLineType.OUTPUT))
        lines.add(TerminalLine(""))
        lines.add(TerminalLine("PAMIĘĆ, ASEMBLER I REJESTRY:", TerminalLineType.SYSTEM))
        lines.add(TerminalLine("  mem                - Wizualny zrzut pamięci RAM i rejestrów CPU", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  mem set <a hex...> - Wpisz bajty pod adres pamięci (np. 'mem set 0x7C00 55 AA')", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  mem reg <r> <val>  - Ustaw rejestr x86 (eax, ebx, esp, cr0, cr3)", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  asm <addr> <inst>  - Asembluj mnemoniki x86 i symuluj cykle zegara CPU", TerminalLineType.OUTPUT))
        lines.add(TerminalLine(""))
        lines.add(TerminalLine("KOMPILACJA I TESTOWANIE JĄDRA:", TerminalLineType.SYSTEM))
        lines.add(TerminalLine("  build / compile    - Kompiluj bieżący moduł (NASM / GCC)", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  test               - Uruchom zautomatyzowane testy jednostkowe modułu", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("  boot / run         - Uruchom system operacyjny w maszynie wirtualnej QEMU", TerminalLineType.OUTPUT))
        return lines
    }

    private fun handleUptime(): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        val now = System.currentTimeMillis()
        val elapsedSeconds = ((now - getBootTimeMillis()) / 1000).coerceAtLeast(0)
        val hours = elapsedSeconds / 3600
        val minutes = (elapsedSeconds % 3600) / 60
        val seconds = elapsedSeconds % 60
        val timeFormatted = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(now))
        val bootTimeFormatted = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(getBootTimeMillis()))
        val upStr = if (hours > 0) {
            "${hours}h ${minutes}m ${seconds}s"
        } else if (minutes > 0) {
            "${minutes}m ${seconds}s"
        } else {
            "${seconds}s"
        }
        lines.add(TerminalLine("$timeFormatted  up $upStr (Booted at $bootTimeFormatted),  1 user,  load average: 0.12, 0.08, 0.04", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("Hardware: x86 IA-32 @ ${cpuSim.cpuHardwareState.clockspeedGhz} GHz | Total CPU Cycles: ${cpuSim.cpuHardwareState.totalCycles}", TerminalLineType.SYSTEM))
        return lines
    }

    private fun handleChmod(args: List<String>): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        if (args.size < 2) {
            lines.add(TerminalLine("Użycie: chmod <uprawnienia_octal_lub_symboliczne> <ścieżka_pliku>", TerminalLineType.WARNING))
            lines.add(TerminalLine("Przykład: chmod 755 /boot/boot.asm, chmod 644 /etc/os-release, chmod +x /kernel/vga.c", TerminalLineType.OUTPUT))
        } else {
            val modeStr = args[0]
            val rawPath = args[1]
            val normalized = if (rawPath.startsWith("/")) rawPath else "/$rawPath"
            val node = vfsManager.virtualFileSystem[normalized] ?: vfsManager.virtualFileSystem.values.find { it.name.equals(rawPath, ignoreCase = true) }

            if (node == null) {
                lines.add(TerminalLine("chmod: nie można uzyskać dostępu do '$rawPath': Nie ma takiego pliku ani katalogu", TerminalLineType.ERROR))
            } else {
                val oldPerms = node.permissions
                val prefix = if (node.type == FsNodeType.DIRECTORY) "d" else if (node.type == FsNodeType.DEVICE) "c" else "-"
                val newPerms: String = when {
                    modeStr.length == 3 && modeStr.all { it in '0'..'7' } -> {
                        val u = modeStr[0].digitToInt()
                        val g = modeStr[1].digitToInt()
                        val o = modeStr[2].digitToInt()
                        prefix + octalToRwx(u) + octalToRwx(g) + octalToRwx(o)
                    }
                    modeStr == "+x" -> {
                        val body = oldPerms.drop(1).toCharArray()
                        if (body.size >= 9) { body[2] = 'x'; body[5] = 'x'; body[8] = 'x' }
                        prefix + String(body)
                    }
                    modeStr == "-x" -> {
                        val body = oldPerms.drop(1).toCharArray()
                        if (body.size >= 9) { body[2] = '-'; body[5] = '-'; body[8] = '-' }
                        prefix + String(body)
                    }
                    modeStr == "+w" -> {
                        val body = oldPerms.drop(1).toCharArray()
                        if (body.size >= 9) { body[1] = 'w'; body[4] = 'w'; body[7] = 'w' }
                        prefix + String(body)
                    }
                    modeStr == "+r" -> {
                        val body = oldPerms.drop(1).toCharArray()
                        if (body.size >= 9) { body[0] = 'r'; body[3] = 'r'; body[6] = 'r' }
                        prefix + String(body)
                    }
                    else -> "$prefix$modeStr"
                }

                vfsManager.virtualFileSystem[node.path] = node.copy(permissions = newPerms)
                dmesgBuffer.log("vfs/chmod", "OK", "chmod: Permissions for ${node.path} modified: $oldPerms -> $newPerms")
                lines.add(TerminalLine("✔ Zmieniono uprawnienia węzła VFS '${node.path}'", TerminalLineType.SUCCESS))
                lines.add(TerminalLine("  Poprzednie uprawnienia: $oldPerms", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("  Nowe uprawnienia:       $newPerms", TerminalLineType.SYSTEM))
            }
        }
        return lines
    }

    private fun handleDf(args: List<String>): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        val totalRamDiskBytes = 67108864L // 64 MB
        val usedBytes = vfsManager.virtualFileSystem.values.sumOf { it.sizeBytes }
        val freeBytes = (totalRamDiskBytes - usedBytes).coerceAtLeast(0)
        val usePercent = ((usedBytes * 100) / totalRamDiskBytes).toInt()

        lines.add(TerminalLine("╔══════════════════════════════════════════════════════════════════╗", TerminalLineType.HEADER))
        lines.add(TerminalLine("║           GENESIS VFS DISK & INODE USAGE REPORT (df)             ║", TerminalLineType.HEADER))
        lines.add(TerminalLine("╚══════════════════════════════════════════════════════════════════╝", TerminalLineType.HEADER))
        lines.add(TerminalLine("System plików     Rozmiar   Użyte   Dostępne  Użycie%  Zamontowano na", TerminalLineType.SYSTEM))

        val isHuman = args.contains("-h")
        if (isHuman) {
            val totalStr = "64.0M"
            val usedStr = String.format(Locale.US, "%.1fM", usedBytes / (1024.0 * 1024.0))
            val freeStr = String.format(Locale.US, "%.1fM", freeBytes / (1024.0 * 1024.0))
            lines.add(TerminalLine("/dev/ram0         ${totalStr.padStart(7)}  ${usedStr.padStart(6)}   ${freeStr.padStart(7)}   ${usePercent.toString().padStart(3)}%    /", TerminalLineType.OUTPUT))
            lines.add(TerminalLine("devtmpfs           4.0M     0.0M      4.0M     0%    /dev", TerminalLineType.OUTPUT))
            lines.add(TerminalLine("proc               0.0M     0.0M      0.0M     0%    /proc", TerminalLineType.OUTPUT))
            lines.add(TerminalLine("tmpfs             16.0M     0.5M     15.5M     3%    /tmp", TerminalLineType.OUTPUT))
        } else {
            val usedKb = (usedBytes / 1024).coerceAtLeast(1)
            val freeMb = 64 - (usedBytes / (1024 * 1024))
            lines.add(TerminalLine("/dev/ram0          64M      ${usedKb}K      ${freeMb}M   ${usePercent}%    /", TerminalLineType.OUTPUT))
            lines.add(TerminalLine("devtmpfs            4M        0K        4M     0%    /dev", TerminalLineType.OUTPUT))
            lines.add(TerminalLine("proc                0M        0K        0M     0%    /proc", TerminalLineType.OUTPUT))
            lines.add(TerminalLine("tmpfs              16M      512K       15M     3%    /tmp", TerminalLineType.OUTPUT))
        }

        val inodeCount = vfsManager.virtualFileSystem.size
        lines.add(TerminalLine(""))
        lines.add(TerminalLine("• Wirtualny system plików: $inodeCount i-węzłów aktywnych (Limit i-węzłów: 1024)", TerminalLineType.SYSTEM))
        lines.add(TerminalLine("• Domyślny blok alokacji VFS: 4096 bajtów (Ext2/GenesisFS compatible)", TerminalLineType.OUTPUT))
        return lines
    }

    private fun handleLs(args: List<String>): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        val isDetailed = args.contains("-l") || args.contains("-la") || args.contains("-al")
        val targetPath = args.find { !it.startsWith("-") } ?: "/"
        val cleanTarget = if (targetPath == "/") "/" else targetPath.trimEnd('/')

        val entries = if (cleanTarget == "/") {
            vfsManager.virtualFileSystem.filter { (path, _) ->
                path != "/" && path.count { it == '/' } == 1
            }
        } else {
            vfsManager.virtualFileSystem.filter { (path, _) ->
                path.startsWith("$cleanTarget/") && path.substring(cleanTarget.length + 1).count { it == '/' } == 0
            }
        }

        if (entries.isEmpty() && !vfsManager.virtualFileSystem.containsKey(cleanTarget)) {
            lines.add(TerminalLine("ls: nie można uzyskać dostępu do '$targetPath': Nie ma takiego pliku ani katalogu", TerminalLineType.ERROR))
        } else {
            lines.add(TerminalLine("Katalog: $cleanTarget", TerminalLineType.HEADER))
            if (isDetailed) {
                lines.add(TerminalLine("UPRAWNIENIA  WŁAŚCICIEL  GRUPA       ROZMIAR  NAZWA", TerminalLineType.SYSTEM))
                entries.values.sortedBy { it.name }.forEach { node ->
                    val typeColor = when (node.type) {
                        FsNodeType.DIRECTORY -> TerminalLineType.SUCCESS
                        FsNodeType.DEVICE -> TerminalLineType.WARNING
                        FsNodeType.SYMLINK -> TerminalLineType.WARNING
                        FsNodeType.FILE -> TerminalLineType.OUTPUT
                    }
                    val icon = if (node.type == FsNodeType.DIRECTORY) "📁 " else if (node.type == FsNodeType.DEVICE) "🔌 " else if (node.type == FsNodeType.SYMLINK) "🔗 " else "📄 "
                    val sizeStr = node.sizeBytes.toString().padStart(8)
                    lines.add(
                        TerminalLine(
                            "${node.permissions}  ${node.owner.padEnd(10)}  ${node.group.padEnd(10)} $sizeStr  $icon${node.name}",
                            typeColor
                        )
                    )
                }
            } else {
                val formattedItems = entries.values.sortedBy { it.name }.map { node ->
                    if (node.type == FsNodeType.DIRECTORY) "[${node.name}/]" else node.name
                }
                formattedItems.chunked(4).forEach { row ->
                    lines.add(TerminalLine(row.joinToString("   "), TerminalLineType.SUCCESS))
                }
            }
        }
        return lines
    }

    private fun handleCat(args: List<String>): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        val target = args.getOrNull(0) ?: ""
        if (target.isBlank()) {
            lines.add(TerminalLine("Użycie: cat <ścieżka_pliku>", TerminalLineType.WARNING))
            lines.add(TerminalLine("Przykład: cat /boot/boot.asm, cat /etc/os-release, cat /proc/cpuinfo", TerminalLineType.OUTPUT))
        } else {
            val normalized = if (target.startsWith("/")) target else "/$target"
            val fileNode = vfsManager.virtualFileSystem[normalized] ?: vfsManager.virtualFileSystem.values.find { it.name.equals(target, ignoreCase = true) }

            if (fileNode == null) {
                lines.add(TerminalLine("cat: $target: Nie ma takiego pliku ani katalogu", TerminalLineType.ERROR))
            } else if (fileNode.type == FsNodeType.DIRECTORY) {
                lines.add(TerminalLine("cat: $target: Jest katalogiem (użyj 'ls $target')", TerminalLineType.WARNING))
            } else {
                lines.add(TerminalLine("╔══════════════════════════════════════════════════════════════════╗", TerminalLineType.HEADER))
                lines.add(TerminalLine("║ PLIK: ${fileNode.path.padEnd(58)} ║", TerminalLineType.HEADER))
                lines.add(TerminalLine("╚══════════════════════════════════════════════════════════════════╝", TerminalLineType.HEADER))
                if (fileNode.content.isEmpty()) {
                    lines.add(TerminalLine("(Plik jest pusty - 0 bajtów)", TerminalLineType.OUTPUT))
                } else {
                    fileNode.content.lines().forEachIndexed { index, line ->
                        val lineNum = (index + 1).toString().padStart(3)
                        lines.add(TerminalLine("$lineNum | $line", TerminalLineType.OUTPUT))
                    }
                }
            }
        }
        return lines
    }

    private fun handleAlias(args: List<String>): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        if (args.isEmpty()) {
            lines.add(TerminalLine("╔══════════════════════════════════════════════════════════════════╗", TerminalLineType.HEADER))
            lines.add(TerminalLine("║      AKTYWNE ALIASY SKRÓTÓW TERMINALA (/etc/aliases.cfg)        ║", TerminalLineType.HEADER))
            lines.add(TerminalLine("╚══════════════════════════════════════════════════════════════════╝", TerminalLineType.HEADER))
            if (vfsManager.customAliases.isEmpty()) {
                lines.add(TerminalLine("(Brak zdefiniowanych aliasów w /etc/aliases.cfg)", TerminalLineType.OUTPUT))
            } else {
                vfsManager.customAliases.toSortedMap().forEach { (k, v) ->
                    lines.add(TerminalLine("  alias $k='$v'", TerminalLineType.OUTPUT))
                }
            }
            lines.add(TerminalLine(""))
            lines.add(TerminalLine("• Łącznie aktywnych skrótów: ${vfsManager.customAliases.size} | Plik konfiguracyjny VFS: /etc/aliases.cfg", TerminalLineType.SYSTEM))
            lines.add(TerminalLine("• Użycie: alias <nazwa>='<polecenie>' | alias -r (przeładuj z VFS) | unalias <nazwa>", TerminalLineType.SYSTEM))
        } else if (args[0] == "-r" || args[0] == "--reload") {
            vfsManager.loadAliasesFromVfs()
            lines.add(TerminalLine("✔ Pomyślnie przeładowano definicje aliasów z wirtualnego pliku konfiguracyjnego /etc/aliases.cfg (${vfsManager.customAliases.size} aliasów).", TerminalLineType.SUCCESS))
        } else if (args[0] == "-d" || args[0] == "--delete") {
            val targetName = args.getOrNull(1)?.lowercase() ?: ""
            if (vfsManager.customAliases.containsKey(targetName)) {
                vfsManager.customAliases.remove(targetName)
                vfsManager.syncAliasesToVfs()
                lines.add(TerminalLine("✔ Usunięto alias '$targetName' oraz zaktualizowano /etc/aliases.cfg.", TerminalLineType.SUCCESS))
            } else {
                lines.add(TerminalLine("alias: $targetName: Nie znaleziono takiego aliasu.", TerminalLineType.ERROR))
            }
        } else {
            val fullArg = args.joinToString(" ")
            if (fullArg.contains("=")) {
                val name = fullArg.substringBefore("=").trim().lowercase()
                var target = fullArg.substringAfter("=").trim().removeSurrounding("'").removeSurrounding("\"")
                if (name.isNotBlank() && target.isNotBlank()) {
                    vfsManager.customAliases[name] = target
                    vfsManager.syncAliasesToVfs()
                    lines.add(TerminalLine("✔ Zdefiniowano nowy alias: $name ➔ '$target'", TerminalLineType.SUCCESS))
                    lines.add(TerminalLine("• Zapisano konfigurację do wirtualnego systemu plików: /etc/aliases.cfg", TerminalLineType.SYSTEM))
                } else {
                    lines.add(TerminalLine("Błąd formatu aliasu. Użyj: alias <nazwa>='<polecenie>'", TerminalLineType.ERROR))
                }
            } else if (args.size >= 2) {
                val name = args[0].lowercase()
                val target = args.drop(1).joinToString(" ").removeSurrounding("'").removeSurrounding("\"")
                vfsManager.customAliases[name] = target
                vfsManager.syncAliasesToVfs()
                lines.add(TerminalLine("✔ Zdefiniowano nowy alias: $name ➔ '$target'", TerminalLineType.SUCCESS))
                lines.add(TerminalLine("• Zapisano konfigurację do wirtualnego systemu plików: /etc/aliases.cfg", TerminalLineType.SYSTEM))
            } else {
                val name = args[0].lowercase()
                if (vfsManager.customAliases.containsKey(name)) {
                    lines.add(TerminalLine("alias $name='${vfsManager.customAliases[name]}'", TerminalLineType.OUTPUT))
                } else {
                    lines.add(TerminalLine("alias: $name: Nie znaleziono takiego aliasu.", TerminalLineType.ERROR))
                }
            }
        }
        return lines
    }

    private fun handleUnalias(args: List<String>): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        if (args.isEmpty()) {
            lines.add(TerminalLine("Użycie: unalias <nazwa_aliasu> | unalias -a (usuń wszystkie)", TerminalLineType.WARNING))
        } else if (args[0] == "-a" || args[0] == "--all") {
            vfsManager.customAliases.clear()
            vfsManager.syncAliasesToVfs()
            lines.add(TerminalLine("✔ Usunięto wszystkie aliasy użytkownika. Zaktualizowano /etc/aliases.cfg.", TerminalLineType.SUCCESS))
        } else {
            val name = args[0].lowercase()
            if (vfsManager.customAliases.containsKey(name)) {
                vfsManager.customAliases.remove(name)
                vfsManager.syncAliasesToVfs()
                lines.add(TerminalLine("✔ Usunięto alias '$name' oraz zaktualizowano plik konfiguracji /etc/aliases.cfg.", TerminalLineType.SUCCESS))
            } else {
                lines.add(TerminalLine("unalias: $name: Nie znaleziono takiego aliasu.", TerminalLineType.ERROR))
            }
        }
        return lines
    }

    private fun handleDmesg(args: List<String>): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        val isClear = args.contains("-c")
        val isKernelOnly = args.contains("-k")
        val isHuman = args.contains("-H") || args.contains("-T")

        lines.add(TerminalLine("╔══════════════════════════════════════════════════════════════════╗", TerminalLineType.HEADER))
        lines.add(TerminalLine("║           GENESIS KERNEL RING BUFFER (dmesg log)                 ║", TerminalLineType.HEADER))
        lines.add(TerminalLine("╚══════════════════════════════════════════════════════════════════╝", TerminalLineType.HEADER))

        val filtered = dmesgBuffer.getFiltered(isKernelOnly)
        filtered.forEach { entry ->
            val lineType = when (entry.level) {
                "OK" -> TerminalLineType.SUCCESS
                "ERR" -> TerminalLineType.ERROR
                "WARN" -> TerminalLineType.WARNING
                else -> TerminalLineType.OUTPUT
            }
            val formatted = if (isHuman) {
                val sec = String.format(Locale.US, "%.3fs", entry.timestampSeconds)
                "[$sec] <${entry.subsystem}> [${entry.level}]: ${entry.message}"
            } else {
                entry.formatFormattedLine()
            }
            lines.add(TerminalLine(formatted, lineType))
        }

        lines.add(TerminalLine(""))
        lines.add(TerminalLine("• Łącznie wpisów w buforze kołowym jądra: ${filtered.size} (Rozmiar bufora: 64 KB)", TerminalLineType.SYSTEM))
        lines.add(TerminalLine("• Narzędzie dmesg: zrzut bufora /dev/kmsg oraz zdarzeń ACPI/x86 boot sequence.", TerminalLineType.OUTPUT))

        if (isClear) {
            dmesgBuffer.clear()
            lines.add(TerminalLine("✔ Bufor ring jądra dmesg został wyczyszczony.", TerminalLineType.SUCCESS))
        }
        return lines
    }

    private fun handleTheme(args: List<String>): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        if (args.isEmpty()) {
            lines.add(TerminalLine("╔══════════════════════════════════════════════════════════════════╗", TerminalLineType.HEADER))
            lines.add(TerminalLine("║            SCHEMATY KOLORYSTYCZNE TERMINALA (THEMES)             ║", TerminalLineType.HEADER))
            lines.add(TerminalLine("╚══════════════════════════════════════════════════════════════════╝", TerminalLineType.HEADER))
            lines.add(TerminalLine("Aktualny motyw: ${getThemeId()}", TerminalLineType.SYSTEM))
            lines.add(TerminalLine("Dostępne palety kolorów terminala:", TerminalLineType.OUTPUT))
            lines.add(TerminalLine("  1. 'green' / 'retro'     ➔ Retro Green (CRT P1 Phosphor)", TerminalLineType.SUCCESS))
            lines.add(TerminalLine("  2. 'amber' / 'vt220'     ➔ Classic Amber (VT220 Phosphor)", TerminalLineType.WARNING))
            lines.add(TerminalLine("  3. 'mono' / 'white'      ➔ Monochrome (VT100 Minimal)", TerminalLineType.OUTPUT))
            lines.add(TerminalLine("  4. 'matrix' / 'cyber'    ➔ Cyberpunk Matrix (Cyan/Purple/Green)", TerminalLineType.HEADER))
            lines.add(TerminalLine("  5. 'solarized'           ➔ Solarized Dark (Ethan Schoonover)", TerminalLineType.SYSTEM))
            lines.add(TerminalLine(""))
            lines.add(TerminalLine("Użycie: theme <nazwa> (np. 'theme green', 'theme amber', 'theme mono')", TerminalLineType.SYSTEM))
            lines.add(TerminalLine("Wskazówka: Możesz także kliknąć przycisk [THEME] na górnym pasku terminala.", TerminalLineType.OUTPUT))
        } else {
            val choice = args[0].lowercase()
            val targetTheme = when {
                choice.contains("green") || choice.contains("retro") || choice == "1" -> "RETRO_GREEN"
                choice.contains("amber") || choice.contains("vt220") || choice == "2" -> "CLASSIC_AMBER"
                choice.contains("mono") || choice.contains("white") || choice == "3" -> "MONOCHROME"
                choice.contains("cyber") || choice.contains("matrix") || choice == "4" -> "CYBER_MATRIX"
                choice.contains("solar") || choice == "5" -> "SOLARIZED_DARK"
                else -> null
            }

            if (targetTheme != null) {
                setThemeId(targetTheme)
                lines.add(TerminalLine("✔ Zmieniono schemat kolorów terminala na: '$targetTheme'", TerminalLineType.SUCCESS))
            } else {
                lines.add(TerminalLine("Nieznany schemat '$choice'. Dostępne: green, amber, mono, cyber, solarized", TerminalLineType.ERROR))
            }
        }
        return lines
    }

    private fun handleInterrupt(args: List<String>): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        val vectorStr = args.getOrNull(0) ?: "0x80"
        val vectorInt = try {
            java.lang.Long.decode(vectorStr).toInt()
        } catch (e: Exception) {
            0x80
        }
        val vectorHex = Integer.toHexString(vectorInt).uppercase().padStart(2, '0')

        val (desc, penaltyCycles, category) = when (vectorInt) {
            0x00 -> Triple("Divide by Zero Exception (#DE)", 42, "EXCEPTION")
            0x0E -> Triple("Page Fault Exception (#PF) - CR2=0x00105000", 55, "FAULT")
            0x0D -> Triple("General Protection Fault (#GP) - Ring 0 Privilege Violation", 60, "FAULT")
            0x10 -> Triple("BIOS Video Display Services (VGA 0x10)", 35, "SOFTWARE INT")
            0x20 -> Triple("PIT 8254 Hardware Timer Tick (IRQ 0)", 38, "HARDWARE IRQ")
            0x21 -> Triple("PS/2 Keyboard Controller Event (IRQ 1)", 36, "HARDWARE IRQ")
            0x80 -> Triple("POSIX Linux/GenesisOS System Call Dispatcher", 35, "SYSCALL")
            else -> Triple("User Defined Interrupt Vector 0x$vectorHex", 35, "SOFTWARE INT")
        }

        val intLoadIntensity = (15f + (penaltyCycles * 1.5f)).coerceIn(18f, 100f)
        cpuSim.cpuHardwareState = cpuSim.cpuHardwareState.withLoadSample(
            newLoad = intLoadIntensity,
            opMnemonic = "INT 0x$vectorHex",
            opCycles = penaltyCycles,
            addedCycles = penaltyCycles
        )

        dmesgBuffer.log(
            "x86/idt",
            if (category == "FAULT") "ERR" else "OK",
            "System Interrupt Vector [0x$vectorHex]: $desc (EFLAGS=0x202, CS=0x08, EIP=0x100000)"
        )

        lines.add(TerminalLine("╔══════════════════════════════════════════════════════════════════╗", TerminalLineType.HEADER))
        lines.add(TerminalLine("║      SYSTEM INTERRUPT VECTOR DISPATCH (INT 0x$vectorHex)               ║", TerminalLineType.HEADER))
        lines.add(TerminalLine("╚══════════════════════════════════════════════════════════════════╝", TerminalLineType.HEADER))
        lines.add(TerminalLine("• Wektor IDT: 0x$vectorHex (Slot #$vectorInt) [$category]", TerminalLineType.SYSTEM))
        lines.add(TerminalLine("• Opis: $desc", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("• Zapisany kontekst stosu: [EFLAGS: 0x00000202 | CS: 0x0008 | EIP: 0x00100000]", TerminalLineType.OUTPUT))
        lines.add(TerminalLine("• Zużycie cykli CPU: +$penaltyCycles cykli zegara (Taktowanie: ${cpuSim.cpuHardwareState.clockspeedGhz} GHz)", TerminalLineType.SYSTEM))
        lines.add(TerminalLine("• Zdarzenie zarejestrowane w dmesg: 'System Interrupt Vector [0x$vectorHex]'", TerminalLineType.SUCCESS))
        lines.add(TerminalLine("• Status: Procedura obsługi przerwania (ISR) zakończona instrukcją IRET.", TerminalLineType.SUCCESS))
        return lines
    }

    private fun handleAsm(args: List<String>): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        if (args.isEmpty()) {
            lines.add(TerminalLine("╔══════════════════════════════════════════════════════════════════╗", TerminalLineType.HEADER))
            lines.add(TerminalLine("║           GENESIS REAL-TIME x86 ASSEMBLER ENGINE                 ║", TerminalLineType.HEADER))
            lines.add(TerminalLine("╚══════════════════════════════════════════════════════════════════╝", TerminalLineType.HEADER))
            lines.add(TerminalLine("SKŁADNIA: asm <adres_hex> <instrukcje...>", TerminalLineType.SYSTEM))
            lines.add(TerminalLine("PRZYKŁADY:", TerminalLineType.OUTPUT))
            lines.add(TerminalLine("  asm 0x7C00 xor eax, eax; mov esp, 0x90000; dw 0xAA55", TerminalLineType.OUTPUT))
            lines.add(TerminalLine("  asm 0x100000 mov eax, 0x42; cli; hlt", TerminalLineType.OUTPUT))
            lines.add(TerminalLine("  asm 0x9C000 mov cr3, eax; mov cr0, eax", TerminalLineType.OUTPUT))
            lines.add(TerminalLine("Wpisz 'man asm' aby zobaczyć pełną listę obsługiwanych mnemoników.", TerminalLineType.SYSTEM))
        } else {
            val addrStr = args[0]
            val codeStr = args.drop(1).joinToString(" ")
            if (codeStr.isBlank()) {
                lines.add(TerminalLine("Błąd: brak instrukcji do asemblacji. Użyj np. 'asm 0x7C00 nop; hlt'", TerminalLineType.ERROR))
            } else {
                try {
                    val addr = java.lang.Long.decode(addrStr)
                    val instructions = codeStr.split(";").map { it.trim() }.filter { it.isNotEmpty() }

                    var currentTarget = cpuSim.memoryMap.getOrPut(addr) { ByteArray(16) { 0 } }
                    var writeOffset = 0
                    var totalCyclesAdded = 0
                    var lastOpName = "NOP"
                    var lastCycles = 1

                    lines.add(TerminalLine("=== ASEMPLACJA INSTRUKCJI POD ADRES 0x${java.lang.Long.toHexString(addr).uppercase()} ===", TerminalLineType.HEADER))

                    instructions.forEach { instr ->
                        val (opcodes, desc, cycles) = cpuSim.assembleInstruction(instr)
                        totalCyclesAdded += cycles
                        lastOpName = instr
                        lastCycles = cycles

                        for (b in opcodes) {
                            if (writeOffset < currentTarget.size) {
                                currentTarget[writeOffset] = b
                                writeOffset++
                            }
                        }
                        val hexRep = opcodes.joinToString(" ") { String.format("%02X", it) }
                        lines.add(TerminalLine("  [+$writeOffset] $hexRep  ➔  $instr ($desc) [$cycles cykli]", TerminalLineType.SUCCESS))
                    }

                    cpuSim.memoryMap[addr] = currentTarget
                    val loadIntensity = (14f + (totalCyclesAdded * 2.2f)).coerceIn(12f, 98.5f)
                    cpuSim.cpuHardwareState = cpuSim.cpuHardwareState.withLoadSample(
                        newLoad = loadIntensity,
                        opMnemonic = lastOpName,
                        opCycles = lastCycles,
                        addedCycles = totalCyclesAdded
                    )

                    dmesgBuffer.log("asm/cpu", "OK", "Assembled ${instructions.size} opcodes into 0x${java.lang.Long.toHexString(addr).uppercase()} (+$totalCyclesAdded CPU clocks)")

                    val finalDump = currentTarget.joinToString(" ") { String.format("%02X", it) }
                    lines.add(TerminalLine("✔ Zapisano pomyślnie do pamięci RAM (0x${java.lang.Long.toHexString(addr).uppercase()}):", TerminalLineType.SYSTEM))
                    lines.add(TerminalLine("  0x${java.lang.Long.toHexString(addr).uppercase().padStart(8, '0')}: $finalDump", TerminalLineType.OUTPUT))
                    lines.add(TerminalLine("⏱ Zużyto $totalCyclesAdded cykli procesora (Zegar: ${cpuSim.cpuHardwareState.clockspeedGhz} GHz)", TerminalLineType.SYSTEM))
                } catch (e: Exception) {
                    dmesgBuffer.log("asm/cpu", "ERR", "Assembly translation failure: ${e.message}")
                    lines.add(TerminalLine("Błąd asemblacji: ${e.message}", TerminalLineType.ERROR))
                }
            }
        }
        return lines
    }

    private fun handleMem(
        args: List<String>,
        currentQuest: Quest?,
        userCode: String,
        completedQuestIds: Set<String>,
        onSelectQuest: (String) -> Unit,
        onRunTests: () -> Unit,
        onBootVm: () -> Unit,
        onAskAi: (String) -> Unit
    ): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        if (args.isEmpty()) {
            lines.add(TerminalLine("╔══════════════════════════════════════════════════════════════════════════════════╗", TerminalLineType.HEADER))
            lines.add(TerminalLine("║                   GENESIS OS VIRTUAL MEMORY & REGISTER MAP                      ║", TerminalLineType.HEADER))
            lines.add(TerminalLine("╠══════════════════════════════════════════════════════════════════════════════════╣", TerminalLineType.HEADER))
            lines.add(TerminalLine("║ CPU REGISTERS (IA-32):                                                           ║", TerminalLineType.SYSTEM))
            lines.add(TerminalLine("║   EAX: ${cpuSim.cpuRegisters.eax.padEnd(10)} EBX: ${cpuSim.cpuRegisters.ebx.padEnd(10)} ECX: ${cpuSim.cpuRegisters.ecx.padEnd(10)} EDX: ${cpuSim.cpuRegisters.edx.padEnd(10)}║", TerminalLineType.OUTPUT))
            lines.add(TerminalLine("║   ESP: ${cpuSim.cpuRegisters.esp.padEnd(10)} EBP: ${cpuSim.cpuRegisters.ebp.padEnd(10)} EIP: ${cpuSim.cpuRegisters.eip.padEnd(10)} EFLAGS: ${cpuSim.cpuRegisters.eflags} (IF=1)  ║", TerminalLineType.OUTPUT))
            lines.add(TerminalLine("║   CR0: ${cpuSim.cpuRegisters.cr0.padEnd(12)} (PG=1, PE=1)    CR3: ${cpuSim.cpuRegisters.cr3.padEnd(12)} (Page Directory Table)   ║", TerminalLineType.OUTPUT))
            lines.add(TerminalLine("║   CS:  ${cpuSim.cpuRegisters.cs.padEnd(6)} (Kernel Code)       DS:  ${cpuSim.cpuRegisters.ds.padEnd(6)} (Kernel Data)              ║", TerminalLineType.OUTPUT))
            lines.add(TerminalLine("╠══════════════════════════════════════════════════════════════════════════════════╣", TerminalLineType.HEADER))
            lines.add(TerminalLine("║ PHYSICAL RAM DUMP (HEX & ASCII):                                                 ║", TerminalLineType.SYSTEM))

            cpuSim.memoryMap.forEach { (addr, bytes) ->
                val hexStr = bytes.joinToString(" ") { String.format("%02X", it) }
                val asciiStr = bytes.map { if (it in 32..126) it.toInt().toChar() else '.' }.joinToString("")
                val label = when (addr) {
                    0x7C00L -> "[MBR BOOT SECTOR]"
                    0x9C000L -> "[PAGE DIRECTORY]"
                    0xB8000L -> "[VGA 80x25 BUFFER]"
                    0x100000L -> "[ELF KERNEL IMAGE]"
                    else -> "[USER REGION]"
                }
                lines.add(TerminalLine("║ 0x${java.lang.Long.toHexString(addr).uppercase().padStart(8, '0')}: ${hexStr.padEnd(48)} |$asciiStr| $label", TerminalLineType.OUTPUT))
            }
            lines.add(TerminalLine("╚══════════════════════════════════════════════════════════════════════════════════╝", TerminalLineType.HEADER))
            lines.add(TerminalLine("Wskazówka: Użyj przycisku [MEM] na górnym pasku lub poleceń 'mem set', 'asm'.", TerminalLineType.SYSTEM))
        } else {
            val sub = args[0].lowercase()
            when (sub) {
                "set", "write" -> {
                    val addrStr = args.getOrNull(1) ?: ""
                    val hexBytes = args.drop(2)
                    if (addrStr.isBlank() || hexBytes.isEmpty()) {
                        lines.add(TerminalLine("Użycie: mem set <adres_hex> <bajt1_hex> [bajt2_hex ...]", TerminalLineType.WARNING))
                        lines.add(TerminalLine("Przykład: mem set 0x7C00 55 AA 90 90", TerminalLineType.OUTPUT))
                    } else {
                        try {
                            val addr = java.lang.Long.decode(addrStr)
                            val parsedBytes = hexBytes.map { it.toInt(16).toByte() }.toByteArray()
                            val existing = cpuSim.memoryMap[addr] ?: ByteArray(16) { 0 }
                            for (i in parsedBytes.indices) {
                                if (i < existing.size) {
                                    existing[i] = parsedBytes[i]
                                }
                            }
                            cpuSim.memoryMap[addr] = existing
                            val hexResult = existing.joinToString(" ") { String.format("%02X", it) }

                            dmesgBuffer.log("mem/write", "OK", "Hex write ${parsedBytes.size} bytes to 0x${java.lang.Long.toHexString(addr).uppercase()}")

                            lines.add(TerminalLine("✔ Zapisano pomyślnie pod adres 0x${java.lang.Long.toHexString(addr).uppercase()}:", TerminalLineType.SUCCESS))
                            lines.add(TerminalLine("  0x${java.lang.Long.toHexString(addr).uppercase().padStart(8, '0')}: $hexResult", TerminalLineType.OUTPUT))
                        } catch (e: Exception) {
                            lines.add(TerminalLine("Błąd formatu hex: ${e.message}. Użyj np. 'mem set 0x7C00 AA 55'", TerminalLineType.ERROR))
                        }
                    }
                }

                "reg", "register" -> {
                    val regName = args.getOrNull(1)?.lowercase() ?: ""
                    val regVal = args.getOrNull(2) ?: ""
                    if (regName.isBlank() || regVal.isBlank()) {
                        lines.add(TerminalLine("Użycie: mem reg <rejestr> <wartość_hex>", TerminalLineType.WARNING))
                        lines.add(TerminalLine("Przykład: mem reg eax 0x00001337 | mem reg cr0 0x80000011", TerminalLineType.OUTPUT))
                    } else {
                        try {
                            val formattedVal = if (regVal.startsWith("0x", ignoreCase = true)) regVal else "0x$regVal"
                            cpuSim.cpuRegisters = when (regName) {
                                "eax" -> cpuSim.cpuRegisters.copy(eax = formattedVal)
                                "ebx" -> cpuSim.cpuRegisters.copy(ebx = formattedVal)
                                "ecx" -> cpuSim.cpuRegisters.copy(ecx = formattedVal)
                                "edx" -> cpuSim.cpuRegisters.copy(edx = formattedVal)
                                "esp" -> cpuSim.cpuRegisters.copy(esp = formattedVal)
                                "ebp" -> cpuSim.cpuRegisters.copy(ebp = formattedVal)
                                "eip" -> cpuSim.cpuRegisters.copy(eip = formattedVal)
                                "cr0" -> cpuSim.cpuRegisters.copy(cr0 = formattedVal)
                                "cr3" -> cpuSim.cpuRegisters.copy(cr3 = formattedVal)
                                "eflags" -> cpuSim.cpuRegisters.copy(eflags = formattedVal)
                                "cs" -> cpuSim.cpuRegisters.copy(cs = formattedVal)
                                "ds" -> cpuSim.cpuRegisters.copy(ds = formattedVal)
                                else -> cpuSim.cpuRegisters
                            }
                            lines.add(TerminalLine("✔ Rejestr CPU ${regName.uppercase()} zaktualizowany na $formattedVal", TerminalLineType.SUCCESS))
                        } catch (e: Exception) {
                            lines.add(TerminalLine("Błąd modyfikacji rejestru: ${e.message}", TerminalLineType.ERROR))
                        }
                    }
                }

                "dump" -> {
                    val addrStr = args.getOrNull(1) ?: "0x7C00"
                    try {
                        val addr = java.lang.Long.decode(addrStr)
                        val bytes = cpuSim.memoryMap[addr] ?: ByteArray(16) { 0 }
                        val hexStr = bytes.joinToString(" ") { String.format("%02X", it) }
                        val asciiStr = bytes.map { if (it in 32..126) it.toInt().toChar() else '.' }.joinToString("")
                        lines.add(TerminalLine("=== ZRZUT PAMIĘCI RAM OD ADRESU 0x${java.lang.Long.toHexString(addr).uppercase()} ===", TerminalLineType.HEADER))
                        lines.add(TerminalLine("0x${java.lang.Long.toHexString(addr).uppercase().padStart(8, '0')}: $hexStr |$asciiStr|", TerminalLineType.OUTPUT))
                    } catch (e: Exception) {
                        lines.add(TerminalLine("Błąd adresu hex: ${e.message}", TerminalLineType.ERROR))
                    }
                }

                "reset" -> {
                    cpuSim.cpuRegisters = com.example.data.game.CpuRegisters()
                    lines.add(TerminalLine("✔ Rejestry procesora i pamięć wirtualna zostały zresetowane do wartości domyślnych.", TerminalLineType.SUCCESS))
                }

                else -> {
                    lines.add(TerminalLine("Nieznana opcja 'mem $sub'. Użyj: 'mem set', 'mem reg', 'mem dump', 'mem reset'", TerminalLineType.WARNING))
                }
            }
        }
        return lines
    }

    private fun handleReg(
        args: List<String>,
        currentQuest: Quest?,
        userCode: String,
        completedQuestIds: Set<String>,
        onSelectQuest: (String) -> Unit,
        onRunTests: () -> Unit,
        onBootVm: () -> Unit,
        onAskAi: (String) -> Unit
    ): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        if (args.isEmpty()) {
            lines.add(TerminalLine("=== REJESTRY I ARCHITEKTURA CPU x86 (IA-32) ===", TerminalLineType.HEADER))
            lines.add(TerminalLine("EAX: ${cpuSim.cpuRegisters.eax} | EBX: ${cpuSim.cpuRegisters.ebx} | ECX: ${cpuSim.cpuRegisters.ecx} | EDX: ${cpuSim.cpuRegisters.edx}", TerminalLineType.OUTPUT))
            lines.add(TerminalLine("ESP: ${cpuSim.cpuRegisters.esp} | EBP: ${cpuSim.cpuRegisters.ebp} | EIP: ${cpuSim.cpuRegisters.eip} | EFLAGS: ${cpuSim.cpuRegisters.eflags}", TerminalLineType.OUTPUT))
            lines.add(TerminalLine("CR0: ${cpuSim.cpuRegisters.cr0} (PG=1, PE=1) | CR3: ${cpuSim.cpuRegisters.cr3} (Page Directory)", TerminalLineType.OUTPUT))
            lines.add(TerminalLine("CS:  ${cpuSim.cpuRegisters.cs} (Kernel Code)   | DS:  ${cpuSim.cpuRegisters.ds} (Kernel Data)", TerminalLineType.OUTPUT))
            lines.add(TerminalLine("Aby zmodyfikować rejestr: 'mem reg <nazwa> <wartość_hex>' (np. 'mem reg eax 0x1234')", TerminalLineType.SYSTEM))
        } else {
            val r = args.getOrNull(0) ?: ""
            val v = args.getOrNull(1) ?: ""
            lines.addAll(execute("mem reg $r $v", currentQuest, userCode, completedQuestIds, onSelectQuest, onRunTests, onBootVm, onAskAi))
        }
        return lines
    }

    private fun handleWhoami(): List<TerminalLine> {
        return listOf(
            TerminalLine("uid=0(root) gid=0(root) groups=0(root),1(kernel-dev),42(os-architect)", TerminalLineType.SYSTEM),
            TerminalLine("USER: Lead OS Architect & Kernel Engineer (Ring 0 Superuser)", TerminalLineType.SUCCESS),
            TerminalLine("HOST: Genesis Workstation x86_64 [Security Sandbox: Active]", TerminalLineType.OUTPUT)
        )
    }

    private fun handleDate(): List<TerminalLine> {
        val formatter = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US)
        return listOf(TerminalLine("System Clock: ${formatter.format(Date())}", TerminalLineType.OUTPUT))
    }

    private fun handleVersion(): List<TerminalLine> {
        return listOf(
            TerminalLine("Genesis Toolchain Suite v1.0.0-LTS", TerminalLineType.HEADER),
            TerminalLine("• Target Architecture: i386-pc-elf (32-bit x86 Protected Mode)", TerminalLineType.OUTPUT),
            TerminalLine("• Cross-Compiler: GNU GCC 13.2.0 (freestanding)", TerminalLineType.OUTPUT),
            TerminalLine("• Assembler: Netwide Assembler (NASM 2.16.01) + Live CPU Cycle Simulator", TerminalLineType.OUTPUT),
            TerminalLine("• Emulator Engine: QEMU PC i440FX + SeaBIOS 1.15.0", TerminalLineType.OUTPUT),
            TerminalLine("• Persistence: Room SQLite Database Local Inode Storage", TerminalLineType.SUCCESS),
            TerminalLine("• AI Kernel Mentor: Gemini 3.1 Pro Preview (High Thinking Level)", TerminalLineType.SUCCESS)
        )
    }

    private fun handleQuests(completedQuestIds: Set<String>): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        lines.add(TerminalLine("=== DRZEWO PROJEKTOWE: GENESIS OS ===", TerminalLineType.HEADER))
        QuestsData.allQuests.forEachIndexed { idx, q ->
            val isDone = completedQuestIds.contains(q.id)
            val statusIcon = if (isDone) "✔ [UKOŃCZONE]" else "● [OTWARTE]"
            val lineType = if (isDone) TerminalLineType.SUCCESS else TerminalLineType.OUTPUT
            lines.add(TerminalLine("${idx + 1}. [${q.id}] ${q.title} ($statusIcon)", lineType))
        }
        lines.add(TerminalLine("Wpisz 'quest <id>' (np. 'quest Q1_1_MBR_MAGIC') aby otworzyć zadanie.", TerminalLineType.SYSTEM))
        return lines
    }

    private fun handleQuestOpen(args: List<String>, onSelectQuest: (String) -> Unit): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        val targetId = args.getOrNull(0)?.uppercase() ?: ""
        val found = QuestsData.allQuests.find { it.id.equals(targetId, ignoreCase = true) }
        if (found != null) {
            onSelectQuest(found.id)
            lines.add(TerminalLine("Załadowano zadanie: ${found.title}", TerminalLineType.SUCCESS))
            lines.add(TerminalLine("Plik: ${found.filePath} (${found.category})", TerminalLineType.OUTPUT))
            lines.add(TerminalLine("Cel: ${found.taskInstructions}", TerminalLineType.SYSTEM))
        } else {
            lines.add(TerminalLine("Nie znaleziono zadania '$targetId'. Wpisz 'quests' aby zobaczyć listę.", TerminalLineType.ERROR))
        }
        return lines
    }

    private fun handleBuild(currentQuest: Quest?): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        if (currentQuest == null) {
            lines.add(TerminalLine("Brak aktywnego zadania. Wybierz zadanie poleceniem 'quests'.", TerminalLineType.WARNING))
        } else {
            lines.add(TerminalLine("Inicjalizacja łańcucha kompilacji dla ${currentQuest.filePath}...", TerminalLineType.SYSTEM))
            if (currentQuest.filePath.endsWith(".asm")) {
                lines.add(TerminalLine("$ nasm -f bin ${currentQuest.filePath} -o /build/boot.bin", TerminalLineType.OUTPUT))
            } else {
                lines.add(TerminalLine("$ i386-elf-gcc -m32 -ffreestanding -O2 -c ${currentQuest.filePath} -o /build/kernel.o", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("$ i386-elf-ld -T /boot/linker.ld -o /build/genesis_kernel.elf /build/kernel.o", TerminalLineType.OUTPUT))
            }
            lines.add(TerminalLine("Kompilacja i linkowanie zakończone sukcesem (Exit code: 0).", TerminalLineType.SUCCESS))
            lines.add(TerminalLine("Wpisz 'test' aby zweryfikować moduł lub 'boot' aby odpalić OS!", TerminalLineType.SYSTEM))
        }
        return lines
    }

    private fun handleHint(currentQuest: Quest?): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        if (currentQuest == null) {
            lines.add(TerminalLine("Wybierz najpierw zadanie wpisując 'quests'.", TerminalLineType.WARNING))
        } else {
            lines.add(TerminalLine("💡 PODPOWIEDŹ ARCHITEKTONICZNA:", TerminalLineType.HEADER))
            lines.add(TerminalLine(currentQuest.conceptExplanation, TerminalLineType.OUTPUT))
            lines.add(TerminalLine("Instrukcja: " + currentQuest.taskInstructions, TerminalLineType.SYSTEM))
        }
        return lines
    }

    private fun handleAi(args: List<String>, onAskAi: (String) -> Unit): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        val prompt = args.joinToString(" ")
        if (prompt.isBlank()) {
            lines.add(TerminalLine("Użycie: ai <twoje pytanie dotyczące jądra>", TerminalLineType.WARNING))
        } else {
            lines.add(TerminalLine("Wysyłanie zapytania do mentorki Ady (Gemini 3.1 Pro High Thinking)...", TerminalLineType.SYSTEM))
            onAskAi(prompt)
        }
        return lines
    }

    private fun handleArch(): List<TerminalLine> {
        return listOf(
            TerminalLine("=== REJESTRY I ARCHITEKTURA CPU x86 (IA-32) ===", TerminalLineType.HEADER),
            TerminalLine("EAX: ${cpuSim.cpuRegisters.eax} | EBX: ${cpuSim.cpuRegisters.ebx} | ECX: ${cpuSim.cpuRegisters.ecx} | EDX: ${cpuSim.cpuRegisters.edx}", TerminalLineType.OUTPUT),
            TerminalLine("ESP: ${cpuSim.cpuRegisters.esp} | EBP: ${cpuSim.cpuRegisters.ebp} | EIP: ${cpuSim.cpuRegisters.eip} | EFLAGS: ${cpuSim.cpuRegisters.eflags}", TerminalLineType.OUTPUT),
            TerminalLine("CR0: ${cpuSim.cpuRegisters.cr0} (PG=1, PE=1) | CR3: ${cpuSim.cpuRegisters.cr3} (Page Directory)", TerminalLineType.OUTPUT),
            TerminalLine("CS:  ${cpuSim.cpuRegisters.cs} (Kernel Code)   | DS:  ${cpuSim.cpuRegisters.ds} (Kernel Data)", TerminalLineType.OUTPUT)
        )
    }

    private fun handleStatus(currentQuest: Quest?, completedQuestIds: Set<String>): List<TerminalLine> {
        val total = QuestsData.allQuests.size
        val done = completedQuestIds.size
        return listOf(
            TerminalLine("=== STATYSTYKI ARCHITEKTA SYSTEMU ===", TerminalLineType.HEADER),
            TerminalLine("Ukończone moduły: $done / $total (${(done * 100) / total}%)", TerminalLineType.SUCCESS),
            TerminalLine("Aktywny moduł: ${currentQuest?.title ?: "Brak"}", TerminalLineType.OUTPUT)
        )
    }

    private fun handleTouch(args: List<String>): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        val raw = args.getOrNull(0) ?: ""
        if (raw.isBlank()) {
            lines.add(TerminalLine("Użycie: touch <ścieżka_pliku>", TerminalLineType.WARNING))
        } else {
            val path = if (raw.startsWith("/")) raw else "/$raw"
            val name = path.substringAfterLast('/')
            val parent = path.substringBeforeLast('/').ifEmpty { "/" }

            if (!vfsManager.virtualFileSystem.containsKey(parent) && parent != "/") {
                lines.add(TerminalLine("touch: nie można utworzyć '$path': Katalog nadrzędny nie istnieje", TerminalLineType.ERROR))
            } else {
                val existing = vfsManager.virtualFileSystem[path]
                if (existing != null) {
                    lines.add(TerminalLine("Zaktualizowano znacznik czasu dla '$path'.", TerminalLineType.SUCCESS))
                } else {
                    vfsManager.virtualFileSystem[path] = VirtualFsNode(
                        path = path,
                        name = name,
                        type = FsNodeType.FILE,
                        permissions = "-rw-r--r--",
                        owner = "dev",
                        group = "dev",
                        sizeBytes = 0,
                        content = ""
                    )
                    dmesgBuffer.log("vfs/create", "OK", "touch: Created empty inode $path")
                    lines.add(TerminalLine("Utworzono pusty plik '$path' (0 bajtów).", TerminalLineType.SUCCESS))
                }
            }
        }
        return lines
    }

    private fun handleMkdir(args: List<String>): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        val raw = args.find { !it.startsWith("-") } ?: ""
        if (raw.isBlank()) {
            lines.add(TerminalLine("Użycie: mkdir [-p] <ścieżka_katalogu>", TerminalLineType.WARNING))
        } else {
            val path = if (raw.startsWith("/")) raw.trimEnd('/') else "/${raw.trimEnd('/')}"
            val name = path.substringAfterLast('/')
            if (vfsManager.virtualFileSystem.containsKey(path)) {
                lines.add(TerminalLine("mkdir: nie można utworzyć katalogu '$path': Plik lub katalog istnieje", TerminalLineType.ERROR))
            } else {
                vfsManager.virtualFileSystem[path] = VirtualFsNode(
                    path = path,
                    name = name,
                    type = FsNodeType.DIRECTORY,
                    permissions = "drwxr-xr-x",
                    owner = "dev",
                    group = "dev",
                    sizeBytes = 4096
                )
                dmesgBuffer.log("vfs/mkdir", "OK", "mkdir: Directory $path created")
                lines.add(TerminalLine("Utworzono katalog '$path'.", TerminalLineType.SUCCESS))
            }
        }
        return lines
    }

    private fun handleRm(args: List<String>): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        val isRecursive = args.contains("-r") || args.contains("-rf") || args.contains("-R")
        val raw = args.find { !it.startsWith("-") } ?: ""
        if (raw.isBlank()) {
            lines.add(TerminalLine("Użycie: rm [-r] <ścieżka>", TerminalLineType.WARNING))
        } else {
            val path = if (raw.startsWith("/")) raw.trimEnd('/') else "/${raw.trimEnd('/')}"
            val node = vfsManager.virtualFileSystem[path]

            if (node == null) {
                lines.add(TerminalLine("rm: nie można usunąć '$path': Nie ma takiego pliku ani katalogu", TerminalLineType.ERROR))
            } else if (node.type == FsNodeType.DIRECTORY && !isRecursive) {
                lines.add(TerminalLine("rm: nie można usunąć '$path': Jest katalogiem (użyj 'rm -r $path')", TerminalLineType.WARNING))
            } else {
                if (node.type == FsNodeType.DIRECTORY) {
                    val toDelete = vfsManager.virtualFileSystem.keys.filter { it == path || it.startsWith("$path/") }
                    toDelete.forEach { vfsManager.virtualFileSystem.remove(it) }
                    dmesgBuffer.log("vfs/rm", "WARN", "rm: Recursively removed directory tree $path (${toDelete.size} nodes)")
                    lines.add(TerminalLine("Usunięto katalog '$path' wraz z zawartością (${toDelete.size} węzłów).", TerminalLineType.SUCCESS))
                } else {
                    vfsManager.virtualFileSystem.remove(path)
                    dmesgBuffer.log("vfs/rm", "OK", "rm: File $path unlinked")
                    lines.add(TerminalLine("Usunięto plik '$path'.", TerminalLineType.SUCCESS))
                }
            }
        }
        return lines
    }

    private fun handleTree(args: List<String>): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        val rootPath = args.getOrNull(0) ?: "/"
        lines.add(TerminalLine("DRZEWO STRUKTURY WIRTUALNEGO SYSTEMU PLIKÓW ($rootPath):", TerminalLineType.HEADER))

        val sortedPaths = vfsManager.virtualFileSystem.keys.sorted()
        var dirCount = 0
        var fileCount = 0

        sortedPaths.forEach { p ->
            if (p == "/") return@forEach
            val depth = p.count { it == '/' }
            val indent = "│   ".repeat((depth - 1).coerceAtLeast(0))
            val node = vfsManager.virtualFileSystem[p]
            if (node != null) {
                val icon = when (node.type) {
                    FsNodeType.DIRECTORY -> {
                        dirCount++
                        "├── 📁 [${node.name}/]"
                    }
                    FsNodeType.DEVICE -> {
                        fileCount++
                        "├── 🔌 ${node.name}"
                    }
                    FsNodeType.SYMLINK -> {
                        fileCount++
                        "├── 🔗 ${node.name} -> ${node.content}"
                    }
                    FsNodeType.FILE -> {
                        fileCount++
                        "├── 📄 ${node.name} (${node.sizeBytes} B)"
                    }
                }
                val color = if (node.type == FsNodeType.DIRECTORY) TerminalLineType.SUCCESS else TerminalLineType.OUTPUT
                lines.add(TerminalLine("$indent$icon", color))
            }
        }
        lines.add(TerminalLine(""))
        lines.add(TerminalLine("• Łącznie: $dirCount katalogów, $fileCount plików w strukturze VFS", TerminalLineType.SYSTEM))
        return lines
    }

    private fun handleGrep(args: List<String>): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        if (args.isEmpty()) {
            lines.add(TerminalLine("Użycie: grep [-i] <wzorzec> [ścieżka_pliku]", TerminalLineType.WARNING))
            lines.add(TerminalLine("Przykład: grep 'start' /boot/boot.asm, grep 'Genesis' /etc/os-release", TerminalLineType.OUTPUT))
        } else {
            val ignoreCase = args.contains("-i")
            val cleanArgs = args.filter { !it.startsWith("-") }
            val pattern = cleanArgs.getOrNull(0) ?: ""
            val targetPath = cleanArgs.getOrNull(1)

            if (pattern.isBlank()) {
                lines.add(TerminalLine("Błąd: brak wzorca wyszukiwania.", TerminalLineType.ERROR))
            } else if (targetPath != null) {
                val normalized = if (targetPath.startsWith("/")) targetPath else "/$targetPath"
                val fileNode = vfsManager.virtualFileSystem[normalized]
                if (fileNode == null) {
                    lines.add(TerminalLine("grep: $targetPath: Nie ma takiego pliku", TerminalLineType.ERROR))
                } else if (fileNode.type == FsNodeType.DIRECTORY) {
                    lines.add(TerminalLine("grep: $targetPath: Jest katalogiem", TerminalLineType.WARNING))
                } else {
                    var matchCount = 0
                    fileNode.content.lines().forEachIndexed { idx, line ->
                        if (line.contains(pattern, ignoreCase = ignoreCase)) {
                            matchCount++
                            lines.add(TerminalLine("${fileNode.name}:${idx + 1}: $line", TerminalLineType.OUTPUT))
                        }
                    }
                    lines.add(TerminalLine("Znaleziono $matchCount dopasowań dla '$pattern' w pliku ${fileNode.path}.", TerminalLineType.SYSTEM))
                }
            } else {
                var totalMatches = 0
                vfsManager.virtualFileSystem.values.filter { it.type == FsNodeType.FILE }.forEach { file ->
                    file.content.lines().forEachIndexed { idx, line ->
                        if (line.contains(pattern, ignoreCase = ignoreCase)) {
                            totalMatches++
                            lines.add(TerminalLine("${file.path}:${idx + 1}: $line", TerminalLineType.OUTPUT))
                        }
                    }
                }
                lines.add(TerminalLine("Łącznie znaleziono $totalMatches dopasowań w całym systemie plików.", TerminalLineType.SYSTEM))
            }
        }
        return lines
    }

    private fun handleSave(args: List<String>): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        val slot = args.getOrNull(0)?.toIntOrNull() ?: 1
        val repo = getSnapshotRepo()
        if (repo == null) {
            lines.add(TerminalLine("Błąd: repozytorium Room nie jest zainicjalizowane.", TerminalLineType.ERROR))
        } else {
            val result = runBlocking {
                repo.saveState(
                    slotName = "slot_$slot",
                    vfs = vfsManager.virtualFileSystem,
                    memoryMap = cpuSim.memoryMap,
                    registers = cpuSim.cpuRegisters,
                    totalCycles = cpuSim.cpuHardwareState.totalCycles,
                    lastOpMnemonic = cpuSim.cpuHardwareState.lastOpMnemonic
                )
            }
            if (result.isSuccess) {
                dmesgBuffer.log("db/room", "OK", "Room SQLite snapshot saved to slot #$slot")
                lines.add(TerminalLine("✔ Zapisano migawkę stanu VFS i pamięci RAM do lokalnej bazy Room (Slot #$slot).", TerminalLineType.SUCCESS))
                lines.add(TerminalLine("• Zachowano stan ${vfsManager.virtualFileSystem.size} i-węzłów oraz ${cpuSim.memoryMap.size} bloków pamięci RAM.", TerminalLineType.SYSTEM))
            } else {
                lines.add(TerminalLine("Błąd zapisu migawki: ${result.exceptionOrNull()?.message}", TerminalLineType.ERROR))
            }
        }
        return lines
    }

    private fun handleLoad(args: List<String>): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        val slot = args.getOrNull(0)?.toIntOrNull() ?: 1
        val repo = getSnapshotRepo()
        if (repo == null) {
            lines.add(TerminalLine("Błąd: repozytorium Room nie jest zainicjalizowane.", TerminalLineType.ERROR))
        } else {
            val result = runBlocking { repo.loadState("slot_$slot") }
            if (result.isFailure) {
                lines.add(TerminalLine("Brak zapisanej migawki w slocie #$slot lub błąd odczytu.", TerminalLineType.WARNING))
            } else {
                val (restoredVfs, memRegs) = result.getOrThrow()
                val (restoredMem, regsCycles) = memRegs
                val (restoredRegs, _) = regsCycles

                vfsManager.virtualFileSystem.clear()
                vfsManager.virtualFileSystem.putAll(restoredVfs)

                cpuSim.memoryMap.clear()
                cpuSim.memoryMap.putAll(restoredMem)

                cpuSim.cpuRegisters = restoredRegs

                dmesgBuffer.log("db/room", "OK", "Room SQLite snapshot loaded from slot #$slot")
                lines.add(TerminalLine("✔ Przywrócono stan systemu z bazy Room (Slot #$slot).", TerminalLineType.SUCCESS))
                lines.add(TerminalLine("• Przywrócono ${restoredVfs.size} plików/katalogów VFS oraz ${restoredMem.size} bloków pamięci.", TerminalLineType.SYSTEM))
            }
        }
        return lines
    }

    private fun handleBenchmark(): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        lines.add(TerminalLine("╔══════════════════════════════════════════════════════════════════╗", TerminalLineType.HEADER))
        lines.add(TerminalLine("║           GENESIS HARDWARE PERFORMANCE BENCHMARK (MIPS)          ║", TerminalLineType.HEADER))
        lines.add(TerminalLine("╚══════════════════════════════════════════════════════════════════╝", TerminalLineType.HEADER))
        lines.add(TerminalLine("• Test 1: Integer ALU & Shift Register Matrix   ➔ 4120 GENESIS-MIPS [OK]", TerminalLineType.SUCCESS))
        lines.add(TerminalLine("• Test 2: MMU Two-Level TLB Translation Speed   ➔ 128 MB/s Throughput [OK]", TerminalLineType.SUCCESS))
        lines.add(TerminalLine("• Test 3: IDT Hardware Interrupt Latency       ➔ 14 ns Response Time [OK]", TerminalLineType.SUCCESS))
        lines.add(TerminalLine("• Test 4: VGA Framebuffer Raster Operations     ➔ 60.0 FPS @ 80x25 [OK]", TerminalLineType.SUCCESS))
        lines.add(TerminalLine("• Wskaźnik ogólny systemu: 98.4 / 100 (Optimal Bare-Metal Performance)", TerminalLineType.SYSTEM))

        cpuSim.cpuHardwareState = cpuSim.cpuHardwareState.withLoadSample(
            newLoad = 85f,
            opMnemonic = "BENCHMARK_FULL_SUITE",
            opCycles = 250,
            addedCycles = 500
        )
        dmesgBuffer.log("cpu/bench", "OK", "Hardware benchmark completed successfully with 4120 GENESIS-MIPS")
        return lines
    }

    private fun handleHistory(): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        lines.add(TerminalLine("╔══════════════════════════════════════════════════════════════════╗", TerminalLineType.HEADER))
        lines.add(TerminalLine("║              HISTORIA OSTATNICH 20 POLECEŃ (history)             ║", TerminalLineType.HEADER))
        lines.add(TerminalLine("╚══════════════════════════════════════════════════════════════════╝", TerminalLineType.HEADER))
        val history = getHistory().takeLast(20)
        if (history.isEmpty()) {
            lines.add(TerminalLine("Historia poleceń jest pusta.", TerminalLineType.OUTPUT))
        } else {
            history.forEachIndexed { index, cmd ->
                val num = (index + 1).toString().padStart(3)
                lines.add(TerminalLine("  $num  $cmd", TerminalLineType.OUTPUT))
            }
        }
        return lines
    }

    private fun octalToRwx(digit: Int): String {
        val r = if ((digit and 4) != 0) "r" else "-"
        val w = if ((digit and 2) != 0) "w" else "-"
        val x = if ((digit and 1) != 0) "x" else "-"
        return "$r$w$x"
    }
}
