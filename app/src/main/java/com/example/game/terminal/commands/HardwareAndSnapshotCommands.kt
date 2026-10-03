package com.example.game.terminal.commands

import com.example.data.game.TerminalLine
import com.example.data.game.TerminalLineType
import com.example.game.terminal.CommandContext
import com.example.game.terminal.TerminalCommand

class AsmCommand : TerminalCommand {
    override val name = "asm"
    override val aliases = listOf("assemble")
    override val description = "Asembluje instrukcję maszynową x86 i symuluje wykonanie"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
        val disp = context.dispatcher
        return disp?.execute("asm ${args.joinToString(" ")}", context.currentQuest, context.userCode, context.completedQuestIds, context.onSelectQuest, context.onRunTests, context.onBootVm, context.onAskAi)?.filter { !it.text.startsWith("> asm") }
            ?: listOf(TerminalLine("asm: asembler", TerminalLineType.OUTPUT))
    }
}

class MemCommand : TerminalCommand {
    override val name = "mem"
    override val aliases = listOf("memview", "memory", "hexdump")
    override val description = "Zrzut heksadecymalny zadanego obszaru pamięci RAM"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
        val disp = context.dispatcher
        return disp?.execute("mem ${args.joinToString(" ")}", context.currentQuest, context.userCode, context.completedQuestIds, context.onSelectQuest, context.onRunTests, context.onBootVm, context.onAskAi)?.filter { !it.text.startsWith("> mem") }
            ?: listOf(TerminalLine("mem: zrzut pamięci RAM", TerminalLineType.OUTPUT))
    }
}

class RegCommand : TerminalCommand {
    override val name = "reg"
    override val aliases = listOf("registers", "cpu")
    override val description = "Wyświetla stan rejestrów ogólnego i specjalnego przeznaczenia (x86)"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
        val disp = context.dispatcher
        return disp?.execute("reg", context.currentQuest, context.userCode, context.completedQuestIds, context.onSelectQuest, context.onRunTests, context.onBootVm, context.onAskAi)?.filter { !it.text.startsWith("> reg") }
            ?: listOf(TerminalLine("reg: rejestry CPU", TerminalLineType.OUTPUT))
    }
}

class IntCommand : TerminalCommand {
    override val name = "int"
    override val aliases = listOf("interrupt")
    override val description = "Wyzwala przerwanie programowe lub sprzętowe (np. int 0x80)"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
        val disp = context.dispatcher
        return disp?.execute("int ${args.joinToString(" ")}", context.currentQuest, context.userCode, context.completedQuestIds, context.onSelectQuest, context.onRunTests, context.onBootVm, context.onAskAi)?.filter { !it.text.startsWith("> int") }
            ?: listOf(TerminalLine("int: przerwanie wyzwolone", TerminalLineType.OUTPUT))
    }
}

class SnapshotCommand : TerminalCommand {
    override val name = "snapshot"
    override val aliases = listOf("snap", "save", "load")
    override val description = "Tworzy, listuje lub przywraca migawki stanu rejestrów i VFS"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
        val disp = context.dispatcher
        val cmd = args.firstOrNull() ?: "list"
        return disp?.execute("snapshot $cmd", context.currentQuest, context.userCode, context.completedQuestIds, context.onSelectQuest, context.onRunTests, context.onBootVm, context.onAskAi)?.filter { !it.text.startsWith("> snapshot") }
            ?: listOf(TerminalLine("snapshot: migawki stanu systemu", TerminalLineType.OUTPUT))
    }
}
