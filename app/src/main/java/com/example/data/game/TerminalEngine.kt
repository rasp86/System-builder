package com.example.data.game

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TerminalEngine {

    private val executionHistory = mutableListOf<String>()

    // Kernel Dmesg Ring Buffer
    val dmesgEntries = mutableListOf<DmesgEntry>().apply {
        add(DmesgEntry(0.000000, "kernel", "INFO", "Linux / GenesisOS Kernel 1.0.0-SMP (x86_32-pc-elf)"))
        add(DmesgEntry(0.001420, "x86/cpu", "OK", "Intel(R) Core(TM) i486/Pentium compatible CPU found @ 3.40 GHz"))
        add(DmesgEntry(0.003180, "bios/e820", "INFO", "BIOS-e820: [mem 0x0000000000000000-0x000000000009fbff] usable"))
        add(DmesgEntry(0.004920, "bios/e820", "INFO", "BIOS-e820: [mem 0x000000000009fc00-0x000000000009ffff] reserved"))
        add(DmesgEntry(0.006500, "bios/e820", "INFO", "BIOS-e820: [mem 0x00000000000b8000-0x00000000000c0000] VGA text video buffer"))
        add(DmesgEntry(0.008910, "x86/gdt", "OK", "Global Descriptor Table (GDT) loaded at physical 0x00000800 [3 entries]"))
        add(DmesgEntry(0.012400, "x86/idt", "OK", "Interrupt Descriptor Table (IDT) loaded with 256 vector slots"))
        add(DmesgEntry(0.016830, "x86/mmu", "OK", "Page Directory initialized @ 0x0009C000. 4MB Identity Paging active (CR0.PG=1)"))
        add(DmesgEntry(0.021050, "pci/bus", "INFO", "PCI host bridge initialized. 00:01.0 VGA Display Controller"))
        add(DmesgEntry(0.025400, "vfs/root", "OK", "Mounted root VFS from initial ramdisk (/dev/ram0) on /"))
        add(DmesgEntry(0.031200, "scheduler", "OK", "Round-Robin Preemptive Task Scheduler online. PIT Timer frequency set to 100Hz"))
    }

    var cpuHardwareState = CpuHardwareState()

    // Memory simulation map (address -> byte array)
    private val memoryMap = mutableMapOf<Long, ByteArray>().apply {
        // MBR 0x7C00 (512B bootloader sector snippet)
        put(0x7C00L, byteArrayOf(0x31.toByte(), 0xC0.toByte(), 0x8E.toByte(), 0xD8.toByte(), 0x8E.toByte(), 0xC0.toByte(), 0x8E.toByte(), 0xD0.toByte(), 0xBC.toByte(), 0x00.toByte(), 0x7C.toByte(), 0x00.toByte(), 0x55.toByte(), 0xAA.toByte(), 0x90.toByte(), 0x90.toByte()))
        // Page Directory 0x9C000
        put(0x9C000L, byteArrayOf(0x00.toByte(), 0x10.toByte(), 0x09.toByte(), 0x00.toByte(), 0x03.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x02.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte()))
        // VGA Text Buffer 0xB8000 ("GenesisOS")
        put(0xB8000L, byteArrayOf(0x47.toByte(), 0x0A.toByte(), 0x65.toByte(), 0x0A.toByte(), 0x6E.toByte(), 0x0A.toByte(), 0x65.toByte(), 0x0A.toByte(), 0x73.toByte(), 0x0A.toByte(), 0x69.toByte(), 0x0A.toByte(), 0x73.toByte(), 0x0A.toByte(), 0x4F.toByte(), 0x0F.toByte()))
        // Kernel Entry 0x100000 (ELF Magic & entry code)
        put(0x100000L, byteArrayOf(0x7F.toByte(), 0x45.toByte(), 0x4C.toByte(), 0x46.toByte(), 0x01.toByte(), 0x01.toByte(), 0x01.toByte(), 0x00.toByte(), 0xB8.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x90.toByte(), 0x90.toByte(), 0x90.toByte()))
    }

    var cpuRegisters = CpuRegisters()

    fun getMemoryMap(): Map<Long, ByteArray> = memoryMap

    fun setMemoryByte(address: Long, byteOffset: Int, value: Byte) {
        val bytes = memoryMap.getOrPut(address) { ByteArray(16) { 0 } }
        if (byteOffset in bytes.indices) {
            bytes[byteOffset] = value
            val timestamp = (System.currentTimeMillis() % 100000) / 1000.0
            dmesgEntries.add(
                DmesgEntry(
                    timestampSeconds = timestamp,
                    subsystem = "mem/alloc",
                    level = "OK",
                    message = "Byte modified at 0x${java.lang.Long.toHexString(address).uppercase()}[+$byteOffset] -> 0x${String.format("%02X", value)}"
                )
            )
        }
    }

    val allAvailableCommands: List<String> = listOf(
        "help",
        "man",
        "dmesg",
        "asm",
        "mem",
        "reg",
        "clear",
        "whoami",
        "date",
        "uptime",
        "pwd",
        "echo",
        "version",
        "history",
        "quests",
        "quest",
        "build",
        "compile",
        "test",
        "boot",
        "run",
        "arch",
        "specs",
        "registers",
        "hint",
        "ls",
        "files",
        "cat",
        "ai",
        "status",
        "stats"
    )

    val allKnownFilePaths: List<String> = listOf(
        "/boot/boot.asm",
        "/boot/gdt.asm",
        "/kernel/vga.c",
        "/kernel/idt.c",
        "/mm/paging.c",
        "/kernel/scheduler.c",
        "/fs/vfs.c",
        "/drivers/keyboard.c",
        "/kernel/syscall.c",
        "/gui/wm.c",
        "/net/ipv4.c",
        "/etc/os-release"
    )

    fun getTabCompletions(currentText: String): List<String> {
        val trimmed = currentText.trimStart()
        if (trimmed.isEmpty()) return listOf("help", "dmesg", "man", "asm", "mem", "quests", "build", "test", "boot")

        val parts = trimmed.split("\\s+".toRegex())
        val lastToken = parts.last()

        if (parts.size <= 1) {
            val matches = allAvailableCommands.filter { it.startsWith(lastToken, ignoreCase = true) }
            return if (matches.isNotEmpty()) matches else allAvailableCommands.take(6)
        } else {
            val cmd = parts[0].lowercase()
            if (cmd == "man") {
                return allAvailableCommands.filter { it.contains(lastToken, ignoreCase = true) }
            }
            if (cmd == "asm") {
                val asmKeywords = listOf("0x7C00", "0x9C000", "0xB8000", "0x100000", "mov", "xor", "cli", "sti", "hlt", "nop", "int", "dw", "jmp")
                return asmKeywords.filter { it.contains(lastToken, ignoreCase = true) }
            }
            if (cmd == "mem" || cmd == "reg") {
                val memKeywords = listOf("set", "dump", "reg", "reset", "0x7C00", "0x9C000", "0xB8000", "0x100000", "eax", "ebx", "ecx", "edx", "cr0", "cr3")
                return memKeywords.filter { it.contains(lastToken, ignoreCase = true) }
            }
            if (cmd == "quest" || cmd == "open") {
                val questIds = QuestsData.allQuests.map { it.id }
                return questIds.filter { it.contains(lastToken, ignoreCase = true) }
            }
            if (cmd in listOf("cat", "edit", "build", "compile", "ls")) {
                val matches = allKnownFilePaths.filter { it.contains(lastToken, ignoreCase = true) }
                return if (matches.isNotEmpty()) matches else allKnownFilePaths.take(5)
            }
            val allCandidates = allAvailableCommands + allKnownFilePaths
            return allCandidates.filter { it.contains(lastToken, ignoreCase = true) }.take(5)
        }
    }

    /**
     * Translates a single x86 assembly mnemonic into machine opcodes and calculates hardware CPU clock cycles.
     */
    fun assembleInstruction(mnemonic: String): Triple<ByteArray, String, Int> {
        val clean = mnemonic.trim().lowercase()
        return when {
            clean == "nop" -> Triple(byteArrayOf(0x90.toByte()), "NOP (No Operation)", 1)
            clean == "cli" -> Triple(byteArrayOf(0xFA.toByte()), "CLI (Clear Interrupt Flag)", 4)
            clean == "sti" -> Triple(byteArrayOf(0xFB.toByte()), "STI (Set Interrupt Flag)", 4)
            clean == "hlt" -> Triple(byteArrayOf(0xF4.toByte()), "HLT (Halt CPU)", 20)
            clean == "cld" -> Triple(byteArrayOf(0xFC.toByte()), "CLD (Clear Direction Flag)", 2)
            clean == "std" -> Triple(byteArrayOf(0xFD.toByte()), "STD (Set Direction Flag)", 2)
            clean == "pushad" -> Triple(byteArrayOf(0x60.toByte()), "PUSHAD (Push All General Registers)", 18)
            clean == "popad" -> Triple(byteArrayOf(0x61.toByte()), "POPAD (Pop All General Registers)", 18)
            clean == "pushfd" -> Triple(byteArrayOf(0x9C.toByte()), "PUSHFD (Push EFLAGS)", 4)
            clean == "popfd" -> Triple(byteArrayOf(0x9D.toByte()), "POPFD (Pop EFLAGS)", 4)
            clean == "iret" -> Triple(byteArrayOf(0xCF.toByte()), "IRET (Interrupt Return)", 24)
            clean == "ret" -> Triple(byteArrayOf(0xC3.toByte()), "RET (Near Return)", 4)
            clean == "xor eax, eax" || clean == "xor eax,eax" -> Triple(byteArrayOf(0x31.toByte(), 0xC0.toByte()), "XOR EAX, EAX (Zero EAX)", 1)
            clean == "xor ebx, ebx" || clean == "xor ebx,ebx" -> Triple(byteArrayOf(0x31.toByte(), 0xDB.toByte()), "XOR EBX, EBX (Zero EBX)", 1)
            clean == "xor ecx, ecx" || clean == "xor ecx,ecx" -> Triple(byteArrayOf(0x31.toByte(), 0xC9.toByte()), "XOR ECX, ECX (Zero ECX)", 1)
            clean == "xor edx, edx" || clean == "xor edx,edx" -> Triple(byteArrayOf(0x31.toByte(), 0xD2.toByte()), "XOR EDX, EDX (Zero EDX)", 1)
            clean == "mov cr0, eax" || clean == "mov cr0,eax" -> Triple(byteArrayOf(0x0F.toByte(), 0x22.toByte(), 0xC0.toByte()), "MOV CR0, EAX (Update Control Register 0)", 16)
            clean == "mov eax, cr0" || clean == "mov eax,cr0" -> Triple(byteArrayOf(0x0F.toByte(), 0x20.toByte(), 0xC0.toByte()), "MOV EAX, CR0 (Read Control Register 0)", 4)
            clean == "mov cr3, eax" || clean == "mov cr3,eax" -> Triple(byteArrayOf(0x0F.toByte(), 0x22.toByte(), 0xD8.toByte()), "MOV CR3, EAX (Set Page Directory Pointer)", 22)
            clean == "mov eax, cr3" || clean == "mov eax,cr3" -> Triple(byteArrayOf(0x0F.toByte(), 0x20.toByte(), 0xD8.toByte()), "MOV EAX, CR3 (Read Page Directory Pointer)", 4)
            clean == "jmp $" -> Triple(byteArrayOf(0xEB.toByte(), 0xFE.toByte()), "JMP $ (Infinite Hang Loop)", 3)

            clean.startsWith("int ") -> {
                val numStr = clean.removePrefix("int ").trim()
                val num = try { java.lang.Long.decode(numStr).toInt() } catch (e: Exception) { 0x10 }
                Triple(byteArrayOf(0xCD.toByte(), num.toByte()), "INT 0x${Integer.toHexString(num).uppercase()} (Software Interrupt)", 35)
            }

            clean.startsWith("dw ") -> {
                val valStr = clean.removePrefix("dw ").trim()
                val v = try { java.lang.Long.decode(valStr).toInt() } catch (e: Exception) { 0xAA55 }
                val low = (v and 0xFF).toByte()
                val high = ((v shr 8) and 0xFF).toByte()
                Triple(byteArrayOf(low, high), "DW 0x${Integer.toHexString(v).uppercase()} (Define Word)", 2)
            }

            clean.startsWith("db ") -> {
                val valStr = clean.removePrefix("db ").trim()
                val v = try { java.lang.Long.decode(valStr).toInt() } catch (e: Exception) { 0 }
                Triple(byteArrayOf((v and 0xFF).toByte()), "DB 0x${Integer.toHexString(v).uppercase()} (Define Byte)", 1)
            }

            clean.startsWith("mov eax,") || clean.startsWith("mov eax ,") -> {
                val valStr = clean.substringAfter("mov eax,").trim().removePrefix("0x")
                val v = try { valStr.toLong(16) } catch (e: Exception) { 0L }
                val b0 = (v and 0xFF).toByte()
                val b1 = ((v shr 8) and 0xFF).toByte()
                val b2 = ((v shr 16) and 0xFF).toByte()
                val b3 = ((v shr 24) and 0xFF).toByte()
                cpuRegisters = cpuRegisters.copy(eax = "0x" + v.toString(16).uppercase().padStart(8, '0'))
                Triple(byteArrayOf(0xB8.toByte(), b0, b1, b2, b3), "MOV EAX, 0x${v.toString(16).uppercase()}", 1)
            }

            clean.startsWith("mov ebx,") || clean.startsWith("mov ebx ,") -> {
                val valStr = clean.substringAfter("mov ebx,").trim().removePrefix("0x")
                val v = try { valStr.toLong(16) } catch (e: Exception) { 0L }
                val b0 = (v and 0xFF).toByte()
                val b1 = ((v shr 8) and 0xFF).toByte()
                val b2 = ((v shr 16) and 0xFF).toByte()
                val b3 = ((v shr 24) and 0xFF).toByte()
                cpuRegisters = cpuRegisters.copy(ebx = "0x" + v.toString(16).uppercase().padStart(8, '0'))
                Triple(byteArrayOf(0xBB.toByte(), b0, b1, b2, b3), "MOV EBX, 0x${v.toString(16).uppercase()}", 1)
            }

            clean.startsWith("mov esp,") || clean.startsWith("mov esp ,") -> {
                val valStr = clean.substringAfter("mov esp,").trim().removePrefix("0x")
                val v = try { valStr.toLong(16) } catch (e: Exception) { 0x90000L }
                val b0 = (v and 0xFF).toByte()
                val b1 = ((v shr 8) and 0xFF).toByte()
                val b2 = ((v shr 16) and 0xFF).toByte()
                val b3 = ((v shr 24) and 0xFF).toByte()
                cpuRegisters = cpuRegisters.copy(esp = "0x" + v.toString(16).uppercase().padStart(8, '0'))
                Triple(byteArrayOf(0xBC.toByte(), b0, b1, b2, b3), "MOV ESP, 0x${v.toString(16).uppercase()}", 1)
            }

            else -> {
                Triple(byteArrayOf(0x90.toByte()), "GENERIC x86 OP: $mnemonic (1 cycle)", 1)
            }
        }
    }

    /**
     * Generates a rich Unix-style manual documentation page for any command.
     */
    fun getManualPage(command: String): List<TerminalLine> {
        val target = command.trim().lowercase()
        val lines = mutableListOf<TerminalLine>()

        when (target) {
            "man" -> {
                lines.add(TerminalLine("MAN(1)                         Genesis Manual Pages                         MAN(1)", TerminalLineType.HEADER))
                lines.add(TerminalLine("NAZWA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    man - interfejs do internetowych podręczników systemowych i dokumentacji jądra", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("SKŁADNIA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    man [POLECENIE]", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("OPIS", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    man formatuje i wyświetla szczegółowe strony podręcznika systemowego dla wszystkich narzędzi w toolchainie GenesisOS.", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("DOSTĘPNE STRONY PODRĘCZNIKA:", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    man dmesg, man mem, man asm, man build, man test, man boot, man whoami, man quests, man arch, man ai", TerminalLineType.OUTPUT))
            }

            "dmesg" -> {
                lines.add(TerminalLine("DMESG(1)                       Genesis Manual Pages                       DMESG(1)", TerminalLineType.HEADER))
                lines.add(TerminalLine("NAZWA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    dmesg - zrzut i kontrola bufora cyklicznego komunikatów jądra (Kernel Ring Buffer)", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("SKŁADNIA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    dmesg [-c] [-l poziom]", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("OPIS", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    Wyświetla komunikaty zdarzeń niskopoziomowych jądra (błędy alokacji pamięci MMU, rozruch procesora x86, przerwania sprzętowe PIC/IDT, pomyślna asemblacja kodu) w specjalnie sformatowanym stylu uniksowym z prefiksami znaczników czasu.", TerminalLineType.OUTPUT))
            }

            "mem", "memory" -> {
                lines.add(TerminalLine("MEM(1)                         Genesis Manual Pages                         MEM(1)", TerminalLineType.HEADER))
                lines.add(TerminalLine("NAZWA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    mem - inspektor i edytor pamięci wirtualnej oraz rejestrów x86", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("SKŁADNIA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    mem [SUBKOMENDA] [PARAMETRY...]", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("OPIS", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    Wyświetla interaktywną mapę pamięci fizycznej i wirtualnej (MBR 0x7C00, Page Directory 0x9C000, VGA 0xB8000, Kernel 0x100000) oraz pozwala na bezpośrednią edycję bajtów i rejestrów.", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("SUBKOMENDY:", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    (brak)                      - Wyświetla pełną tabelę pamięci i rejestrów IA-32", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("    mem set <addr> <hex...>     - Zapisuje bajty szesnastkowe pod podany adres (np. 'mem set 0x7C00 55 AA')", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("    mem reg <rejestr> <wartość> - Ustawia rejestr CPU (eax, ebx, ecx, edx, esp, ebp, cr0, cr3)", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("    mem dump <addr>             - Wykonuje zrzut wybranego sektora pamięci", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("    mem reset                   - Przywraca domyślny stan RAM i rejestrów", TerminalLineType.OUTPUT))
            }

            "asm", "assemble" -> {
                lines.add(TerminalLine("ASM(1)                         Genesis Manual Pages                         ASM(1)", TerminalLineType.HEADER))
                lines.add(TerminalLine("NAZWA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    asm - wbudowany asembler instrukcji maszynowych x86 w czasie rzeczywistym z pomiarem cykli CPU", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("SKŁADNIA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    asm <adres_hex> <instrukcje_x86>", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("OPIS", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    Tłumaczy mnemoniki x86 (IA-32) na kody maszynowe (opcodes) i zapisuje je bezpośrednio pod podany adres w pamięci wirtualnej 'mem', obliczając zużycie cykli zegara CPU.", TerminalLineType.OUTPUT))
            }

            "whoami" -> {
                lines.add(TerminalLine("WHOAMI(1)                      Genesis Manual Pages                      WHOAMI(1)", TerminalLineType.HEADER))
                lines.add(TerminalLine("NAZWA", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("    whoami - wyświetla tożsamość zalogowanego programisty jądra i poziom uprawnień Ring 0", TerminalLineType.OUTPUT))
            }

            else -> {
                lines.add(TerminalLine("Brak wpisu podręcznika systemowego dla '$target'.", TerminalLineType.WARNING))
                lines.add(TerminalLine("Wpisz 'man' aby wyświetlić listę wszystkich dostępnych stron dokumentacji.", TerminalLineType.OUTPUT))
            }
        }

        return lines
    }

    fun executeHostCommand(
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
        if (trimmed.isNotBlank()) {
            executionHistory.add(trimmed)
        }

        val parts = trimmed.split("\\s+".toRegex())
        val cmd = parts.getOrNull(0)?.lowercase() ?: ""
        val args = parts.drop(1)

        val lines = mutableListOf<TerminalLine>()
        lines.add(TerminalLine("> $commandStr", TerminalLineType.INPUT))

        when (cmd) {
            "help", "?" -> {
                lines.add(TerminalLine("╔══════════════════════════════════════════════════════════════════╗", TerminalLineType.HEADER))
                lines.add(TerminalLine("║        GENESIS OS ARCHITECT - HOST DEVELOPER TOOLCHAIN           ║", TerminalLineType.HEADER))
                lines.add(TerminalLine("╚══════════════════════════════════════════════════════════════════╝", TerminalLineType.HEADER))
                lines.add(TerminalLine("LOGI JĄDRA & DOKUMENTACJA:", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("  dmesg              - Zrzut logów bufora cyklicznego zdarzeń jądra", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("  man [polecenie]    - Wyświetl szczegółowy podręcznik systemowy Unix", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("  help / ?           - Skrócona lista komend", TerminalLineType.OUTPUT))
                lines.add(TerminalLine(""))
                lines.add(TerminalLine("PAMIĘĆ, ASEMBLER I REJESTRY:", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("  mem                - Wizualny zrzut pamięci RAM i rejestrów CPU", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("  mem set <a hex...> - Wpisz bajty pod adres pamięci (np. 'mem set 0x7C00 55 AA')", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("  mem reg <r> <val>  - Ustaw rejestr x86 (eax, ebx, esp, cr0, cr3)", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("  asm <addr> <inst>  - Asembluj mnemoniki x86 i symuluj cykle zegara CPU", TerminalLineType.OUTPUT))
                lines.add(TerminalLine(""))
                lines.add(TerminalLine("SYSTEM & IDENTYFIKACJA:", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("  whoami             - Informacje o tożsamości programisty i uprawnieniach Ring 0", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("  clear / cls        - Wyczyść ekran terminala", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("  date / uptime      - Zegar systemowy i czas pracy stacji roboczej", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("  pwd / echo         - Ścieżka robocza i wypisywanie tekstu", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("  version / history  - Wersje łańcucha narzędzi i historia poleceń", TerminalLineType.OUTPUT))
                lines.add(TerminalLine(""))
                lines.add(TerminalLine("ZARZĄDZANIE PROJEKTEM I ZADANIAMI:", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("  quests / tasks     - Wyświetla drzewo 10 faz budowy systemu operacyjnego", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("  quest <id>         - Otwórz konkretne zadanie w edytorze kodu", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("  hint               - Podpowiedź architektoniczna do bieżącego zadania", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("  status / stats     - Statystyki programisty (Poziom, XP, Bits, Faza)", TerminalLineType.OUTPUT))
                lines.add(TerminalLine(""))
                lines.add(TerminalLine("KOMPILACJA I TESTOWANIE JĄDRA:", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("  build / compile    - Kompiluj bieżący moduł (NASM / GCC)", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("  test               - Uruchom zautomatyzowane testy jednostkowe modułu", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("  boot / run         - Uruchom system operacyjny w maszynie wirtualnej QEMU", TerminalLineType.OUTPUT))
                lines.add(TerminalLine(""))
                lines.add(TerminalLine("SYSTEM PLIKÓW I NARZĘDZIA:", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("  ls / files         - Lista plików źródłowych jądra", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("  cat <file>         - Podejrzyj zawartość pliku", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("  arch / specs       - Zrzut rejestrów x86 i mapa pamięci MMU", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("  ai <pytanie>       - Skonsultuj się z mentorką Adą (Gemini High Thinking)", TerminalLineType.OUTPUT))
            }

            "dmesg" -> {
                lines.add(TerminalLine("╔══════════════════════════════════════════════════════════════════╗", TerminalLineType.HEADER))
                lines.add(TerminalLine("║           GENESIS KERNEL RING BUFFER (dmesg log)                 ║", TerminalLineType.HEADER))
                lines.add(TerminalLine("╚══════════════════════════════════════════════════════════════════╝", TerminalLineType.HEADER))
                dmesgEntries.forEach { entry ->
                    val lineType = when (entry.level) {
                        "OK" -> TerminalLineType.SUCCESS
                        "ERR" -> TerminalLineType.ERROR
                        "WARN" -> TerminalLineType.WARNING
                        else -> TerminalLineType.OUTPUT
                    }
                    lines.add(TerminalLine(entry.formatFormattedLine(), lineType))
                }
            }

            "man", "manual" -> {
                val targetCmd = args.getOrNull(0) ?: "man"
                lines.addAll(getManualPage(targetCmd))
            }

            "asm", "assemble" -> {
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

                            var currentTarget = memoryMap.getOrPut(addr) { ByteArray(16) { 0 } }
                            var writeOffset = 0
                            var totalCyclesAdded = 0
                            var lastOpName = "NOP"
                            var lastCycles = 1

                            lines.add(TerminalLine("=== ASEMPLACJA INSTRUKCJI POD ADRES 0x${java.lang.Long.toHexString(addr).uppercase()} ===", TerminalLineType.HEADER))

                            instructions.forEach { instr ->
                                val (opcodes, desc, cycles) = assembleInstruction(instr)
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

                            memoryMap[addr] = currentTarget
                            cpuHardwareState = cpuHardwareState.copy(
                                totalCycles = cpuHardwareState.totalCycles + totalCyclesAdded,
                                lastOpCycles = lastCycles,
                                lastOpMnemonic = lastOpName
                            )

                            // Add to dmesg kernel log
                            val timestamp = (System.currentTimeMillis() % 100000) / 1000.0
                            dmesgEntries.add(
                                DmesgEntry(
                                    timestampSeconds = timestamp,
                                    subsystem = "asm/cpu",
                                    level = "OK",
                                    message = "Assembled ${instructions.size} opcodes into 0x${java.lang.Long.toHexString(addr).uppercase()} (+$totalCyclesAdded CPU clocks)"
                                )
                            )

                            val finalDump = currentTarget.joinToString(" ") { String.format("%02X", it) }
                            lines.add(TerminalLine("✔ Zapisano pomyślnie do pamięci RAM (0x${java.lang.Long.toHexString(addr).uppercase()}):", TerminalLineType.SYSTEM))
                            lines.add(TerminalLine("  0x${java.lang.Long.toHexString(addr).uppercase().padStart(8, '0')}: $finalDump", TerminalLineType.OUTPUT))
                            lines.add(TerminalLine("⏱ Zużyto $totalCyclesAdded cykli procesora (Zegar: ${cpuHardwareState.clockspeedGhz} GHz)", TerminalLineType.SYSTEM))
                        } catch (e: Exception) {
                            val timestamp = (System.currentTimeMillis() % 100000) / 1000.0
                            dmesgEntries.add(
                                DmesgEntry(
                                    timestampSeconds = timestamp,
                                    subsystem = "asm/cpu",
                                    level = "ERR",
                                    message = "Assembly translation failure: ${e.message}"
                                )
                            )
                            lines.add(TerminalLine("Błąd asemblacji: ${e.message}", TerminalLineType.ERROR))
                        }
                    }
                }
            }

            "whoami" -> {
                lines.add(TerminalLine("uid=0(root) gid=0(root) groups=0(root),1(kernel-dev),42(os-architect)", TerminalLineType.SYSTEM))
                lines.add(TerminalLine("USER: Lead OS Architect & Kernel Engineer (Ring 0 Superuser)", TerminalLineType.SUCCESS))
                lines.add(TerminalLine("HOST: Genesis Workstation x86_64 [Security Sandbox: Active]", TerminalLineType.OUTPUT))
            }

            "mem", "memview", "memory" -> {
                if (args.isEmpty()) {
                    lines.add(TerminalLine("╔══════════════════════════════════════════════════════════════════════════════════╗", TerminalLineType.HEADER))
                    lines.add(TerminalLine("║                   GENESIS OS VIRTUAL MEMORY & REGISTER MAP                      ║", TerminalLineType.HEADER))
                    lines.add(TerminalLine("╠══════════════════════════════════════════════════════════════════════════════════╣", TerminalLineType.HEADER))
                    lines.add(TerminalLine("║ CPU REGISTERS (IA-32):                                                           ║", TerminalLineType.SYSTEM))
                    lines.add(TerminalLine("║   EAX: ${cpuRegisters.eax.padEnd(10)} EBX: ${cpuRegisters.ebx.padEnd(10)} ECX: ${cpuRegisters.ecx.padEnd(10)} EDX: ${cpuRegisters.edx.padEnd(10)}║", TerminalLineType.OUTPUT))
                    lines.add(TerminalLine("║   ESP: ${cpuRegisters.esp.padEnd(10)} EBP: ${cpuRegisters.ebp.padEnd(10)} EIP: ${cpuRegisters.eip.padEnd(10)} EFLAGS: ${cpuRegisters.eflags} (IF=1)  ║", TerminalLineType.OUTPUT))
                    lines.add(TerminalLine("║   CR0: ${cpuRegisters.cr0.padEnd(12)} (PG=1, PE=1)    CR3: ${cpuRegisters.cr3.padEnd(12)} (Page Directory Table)   ║", TerminalLineType.OUTPUT))
                    lines.add(TerminalLine("║   CS:  ${cpuRegisters.cs.padEnd(6)} (Kernel Code)       DS:  ${cpuRegisters.ds.padEnd(6)} (Kernel Data)              ║", TerminalLineType.OUTPUT))
                    lines.add(TerminalLine("╠══════════════════════════════════════════════════════════════════════════════════╣", TerminalLineType.HEADER))
                    lines.add(TerminalLine("║ PHYSICAL RAM DUMP (HEX & ASCII):                                                 ║", TerminalLineType.SYSTEM))

                    memoryMap.forEach { (addr, bytes) ->
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
                    lines.add(TerminalLine("Wskazówka: Użyj przycisku [MEM GRID] na górnym pasku lub poleceń 'mem set', 'asm'.", TerminalLineType.SYSTEM))
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
                                    val existing = memoryMap[addr] ?: ByteArray(16) { 0 }
                                    for (i in parsedBytes.indices) {
                                        if (i < existing.size) {
                                            existing[i] = parsedBytes[i]
                                        }
                                    }
                                    memoryMap[addr] = existing
                                    val hexResult = existing.joinToString(" ") { String.format("%02X", it) }

                                    val timestamp = (System.currentTimeMillis() % 100000) / 1000.0
                                    dmesgEntries.add(
                                        DmesgEntry(
                                            timestampSeconds = timestamp,
                                            subsystem = "mem/write",
                                            level = "OK",
                                            message = "Hex write ${parsedBytes.size} bytes to 0x${java.lang.Long.toHexString(addr).uppercase()}"
                                        )
                                    )

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
                                    cpuRegisters = when (regName) {
                                        "eax" -> cpuRegisters.copy(eax = formattedVal)
                                        "ebx" -> cpuRegisters.copy(ebx = formattedVal)
                                        "ecx" -> cpuRegisters.copy(ecx = formattedVal)
                                        "edx" -> cpuRegisters.copy(edx = formattedVal)
                                        "esp" -> cpuRegisters.copy(esp = formattedVal)
                                        "ebp" -> cpuRegisters.copy(ebp = formattedVal)
                                        "eip" -> cpuRegisters.copy(eip = formattedVal)
                                        "cr0" -> cpuRegisters.copy(cr0 = formattedVal)
                                        "cr3" -> cpuRegisters.copy(cr3 = formattedVal)
                                        "eflags" -> cpuRegisters.copy(eflags = formattedVal)
                                        "cs" -> cpuRegisters.copy(cs = formattedVal)
                                        "ds" -> cpuRegisters.copy(ds = formattedVal)
                                        else -> cpuRegisters
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
                                val bytes = memoryMap[addr] ?: ByteArray(16) { 0 }
                                val hexStr = bytes.joinToString(" ") { String.format("%02X", it) }
                                val asciiStr = bytes.map { if (it in 32..126) it.toInt().toChar() else '.' }.joinToString("")
                                lines.add(TerminalLine("=== ZRZUT PAMIĘCI RAM OD ADRESU 0x${java.lang.Long.toHexString(addr).uppercase()} ===", TerminalLineType.HEADER))
                                lines.add(TerminalLine("0x${java.lang.Long.toHexString(addr).uppercase().padStart(8, '0')}: $hexStr |$asciiStr|", TerminalLineType.OUTPUT))
                            } catch (e: Exception) {
                                lines.add(TerminalLine("Błąd adresu hex: ${e.message}", TerminalLineType.ERROR))
                            }
                        }

                        "reset" -> {
                            cpuRegisters = CpuRegisters()
                            lines.add(TerminalLine("✔ Rejestry procesora i pamięć wirtualna zostały zresetowane do wartości domyślnych.", TerminalLineType.SUCCESS))
                        }

                        else -> {
                            lines.add(TerminalLine("Nieznana opcja 'mem $sub'. Użyj: 'mem set', 'mem reg', 'mem dump', 'mem reset'", TerminalLineType.WARNING))
                        }
                    }
                }
            }

            "reg", "registers" -> {
                if (args.isEmpty()) {
                    lines.add(TerminalLine("=== REJESTRY I ARCHITEKTURA CPU x86 (IA-32) ===", TerminalLineType.HEADER))
                    lines.add(TerminalLine("EAX: ${cpuRegisters.eax} | EBX: ${cpuRegisters.ebx} | ECX: ${cpuRegisters.ecx} | EDX: ${cpuRegisters.edx}", TerminalLineType.OUTPUT))
                    lines.add(TerminalLine("ESP: ${cpuRegisters.esp} | EBP: ${cpuRegisters.ebp} | EIP: ${cpuRegisters.eip} | EFLAGS: ${cpuRegisters.eflags}", TerminalLineType.OUTPUT))
                    lines.add(TerminalLine("CR0: ${cpuRegisters.cr0} (PG=1, PE=1) | CR3: ${cpuRegisters.cr3} (Page Directory)", TerminalLineType.OUTPUT))
                    lines.add(TerminalLine("CS:  ${cpuRegisters.cs} (Kernel Code)   | DS:  ${cpuRegisters.ds} (Kernel Data)", TerminalLineType.OUTPUT))
                    lines.add(TerminalLine("Aby zmodyfikować rejestr: 'mem reg <nazwa> <wartość_hex>' (np. 'mem reg eax 0x1234')", TerminalLineType.SYSTEM))
                } else {
                    val r = args.getOrNull(0) ?: ""
                    val v = args.getOrNull(1) ?: ""
                    lines.addAll(executeHostCommand("mem reg $r $v", currentQuest, userCode, completedQuestIds, onSelectQuest, onRunTests, onBootVm, onAskAi))
                }
            }

            "date" -> {
                val formatter = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US)
                lines.add(TerminalLine("System Clock: ${formatter.format(Date())}", TerminalLineType.OUTPUT))
            }

            "uptime" -> {
                lines.add(TerminalLine("03:01:42 up 5 days, 14:22, 1 user, load average: 0.08, 0.04, 0.01", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("CPU Cores: 4x Virtual Intel Core @ 3.40GHz | Memory: 64MB / 4GB V-RAM", TerminalLineType.SYSTEM))
            }

            "pwd" -> {
                lines.add(TerminalLine("/usr/src/genesis-kernel/v1.0", TerminalLineType.OUTPUT))
            }

            "echo" -> {
                val message = args.joinToString(" ")
                lines.add(TerminalLine(message, TerminalLineType.OUTPUT))
            }

            "version" -> {
                lines.add(TerminalLine("Genesis Toolchain Suite v1.0.0-LTS", TerminalLineType.HEADER))
                lines.add(TerminalLine("• Target Architecture: i386-pc-elf (32-bit x86 Protected Mode)", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("• Cross-Compiler: GNU GCC 13.2.0 (freestanding)", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("• Assembler: Netwide Assembler (NASM 2.16.01) + Live CPU Cycle Simulator", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("• Emulator Engine: QEMU PC i440FX + SeaBIOS 1.15.0", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("• AI Kernel Mentor: Gemini 3.1 Pro Preview (High Thinking Level)", TerminalLineType.SUCCESS))
            }

            "history" -> {
                lines.add(TerminalLine("=== HISTORIA POLECEŃ TERMINALA ===", TerminalLineType.HEADER))
                if (executionHistory.isEmpty()) {
                    lines.add(TerminalLine("(Brak wcześniejszych poleceń w tej sesji)", TerminalLineType.OUTPUT))
                } else {
                    executionHistory.takeLast(15).forEachIndexed { index, item ->
                        lines.add(TerminalLine("  ${index + 1}  $item", TerminalLineType.OUTPUT))
                    }
                }
            }

            "quests", "tasks" -> {
                lines.add(TerminalLine("=== DRZEWO PROJEKTOWE: GENESIS OS ===", TerminalLineType.HEADER))
                QuestsData.allQuests.forEachIndexed { idx, q ->
                    val isDone = completedQuestIds.contains(q.id)
                    val statusIcon = if (isDone) "✔ [UKOŃCZONE]" else "● [OTWARTE]"
                    val lineType = if (isDone) TerminalLineType.SUCCESS else TerminalLineType.OUTPUT
                    lines.add(
                        TerminalLine(
                            "${idx + 1}. [${q.id}] ${q.title} ($statusIcon)",
                            lineType
                        )
                    )
                }
                lines.add(TerminalLine("Wpisz 'quest <id>' (np. 'quest Q1_1_MBR_MAGIC') aby otworzyć zadanie.", TerminalLineType.SYSTEM))
            }

            "quest", "open" -> {
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
            }

            "build", "compile" -> {
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
            }

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

            "hint" -> {
                if (currentQuest == null) {
                    lines.add(TerminalLine("Wybierz najpierw zadanie wpisując 'quests'.", TerminalLineType.WARNING))
                } else {
                    lines.add(TerminalLine("💡 PODPOWIEDŹ ARCHITEKTONICZNA:", TerminalLineType.HEADER))
                    lines.add(TerminalLine(currentQuest.conceptExplanation, TerminalLineType.OUTPUT))
                    lines.add(TerminalLine("Instrukcja: " + currentQuest.taskInstructions, TerminalLineType.SYSTEM))
                }
            }

            "ai", "ask" -> {
                val prompt = args.joinToString(" ")
                if (prompt.isBlank()) {
                    lines.add(TerminalLine("Użycie: ai <twoje pytanie dotyczące jądra>", TerminalLineType.WARNING))
                } else {
                    lines.add(TerminalLine("Wysyłanie zapytania do mentorki Ady (Gemini 3.1 Pro High Thinking)...", TerminalLineType.SYSTEM))
                    onAskAi(prompt)
                }
            }

            "ls", "files" -> {
                lines.add(TerminalLine("Wirtualny projekt jądra (/src):", TerminalLineType.HEADER))
                QuestsData.allQuests.forEach { q ->
                    lines.add(TerminalLine("  ${q.filePath} [${q.category}]", TerminalLineType.OUTPUT))
                }
            }

            "cat" -> {
                val target = args.getOrNull(0) ?: ""
                val q = QuestsData.allQuests.find { it.filePath.contains(target, ignoreCase = true) }
                if (q != null) {
                    lines.add(TerminalLine("--- ${q.filePath} ---", TerminalLineType.HEADER))
                    q.defaultCode.lines().forEach { l ->
                        lines.add(TerminalLine(l, TerminalLineType.OUTPUT))
                    }
                } else {
                    lines.add(TerminalLine("Plik nie odnaleziony. Wpisz 'ls' aby zobaczyć pliki.", TerminalLineType.ERROR))
                }
            }

            "arch", "specs" -> {
                lines.add(TerminalLine("=== REJESTRY I ARCHITEKTURA CPU x86 (IA-32) ===", TerminalLineType.HEADER))
                lines.add(TerminalLine("EAX: ${cpuRegisters.eax} | EBX: ${cpuRegisters.ebx} | ECX: ${cpuRegisters.ecx} | EDX: ${cpuRegisters.edx}", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("ESP: ${cpuRegisters.esp} | EBP: ${cpuRegisters.ebp} | EIP: ${cpuRegisters.eip} | EFLAGS: ${cpuRegisters.eflags}", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("CR0: ${cpuRegisters.cr0} (PG=1, PE=1) | CR3: ${cpuRegisters.cr3} (Page Directory)", TerminalLineType.OUTPUT))
                lines.add(TerminalLine("CS:  ${cpuRegisters.cs} (Kernel Code)   | DS:  ${cpuRegisters.ds} (Kernel Data)", TerminalLineType.OUTPUT))
            }

            "status", "stats" -> {
                val total = QuestsData.allQuests.size
                val done = completedQuestIds.size
                lines.add(TerminalLine("=== STATYSTYKI ARCHITEKTA SYSTEMU ===", TerminalLineType.HEADER))
                lines.add(TerminalLine("Ukończone moduły: $done / $total (${(done * 100) / total}%)", TerminalLineType.SUCCESS))
                lines.add(TerminalLine("Aktywny moduł: ${currentQuest?.title ?: "Brak"}", TerminalLineType.OUTPUT))
            }

            "clear", "cls" -> {
                return emptyList()
            }

            "" -> {
                // Empty line
            }

            else -> {
                lines.add(
                    TerminalLine(
                        "Nieznane polecenie: '$cmd'. Wpisz 'help', 'dmesg' lub 'man' aby wyświetlić listę dostępnych komend.",
                        TerminalLineType.ERROR
                    )
                )
            }
        }

        return lines
    }
}
