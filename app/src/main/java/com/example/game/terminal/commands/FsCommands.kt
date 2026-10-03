package com.example.game.terminal.commands

import com.example.data.game.TerminalLine
import com.example.data.game.TerminalLineType
import com.example.game.terminal.CommandContext
import com.example.game.terminal.TerminalCommand

class LsCommand : TerminalCommand {
    override val name = "ls"
    override val description = "Listuje zawartość katalogu VFS"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
        val disp = context.dispatcher
        return disp?.execute("ls ${args.joinToString(" ")}", context.currentQuest, context.userCode, context.completedQuestIds, context.onSelectQuest, context.onRunTests, context.onBootVm, context.onAskAi)?.filter { !it.text.startsWith("> ls") }
            ?: listOf(TerminalLine("ls: VFS dispatcher niedostępny", TerminalLineType.ERROR))
    }
}

class CatCommand : TerminalCommand {
    override val name = "cat"
    override val description = "Wypisuje treść pliku z VFS"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
        val disp = context.dispatcher
        return disp?.execute("cat ${args.joinToString(" ")}", context.currentQuest, context.userCode, context.completedQuestIds, context.onSelectQuest, context.onRunTests, context.onBootVm, context.onAskAi)?.filter { !it.text.startsWith("> cat") }
            ?: listOf(TerminalLine("cat: missing operand", TerminalLineType.ERROR))
    }
}

class TouchCommand : TerminalCommand {
    override val name = "touch"
    override val description = "Tworzy pusty plik lub aktualizuje datę modyfikacji"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
        val disp = context.dispatcher
        return disp?.execute("touch ${args.joinToString(" ")}", context.currentQuest, context.userCode, context.completedQuestIds, context.onSelectQuest, context.onRunTests, context.onBootVm, context.onAskAi)?.filter { !it.text.startsWith("> touch") }
            ?: listOf(TerminalLine("touch: created", TerminalLineType.SUCCESS))
    }
}

class MkdirCommand : TerminalCommand {
    override val name = "mkdir"
    override val description = "Tworzy nowy katalog w VFS"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
        val disp = context.dispatcher
        return disp?.execute("mkdir ${args.joinToString(" ")}", context.currentQuest, context.userCode, context.completedQuestIds, context.onSelectQuest, context.onRunTests, context.onBootVm, context.onAskAi)?.filter { !it.text.startsWith("> mkdir") }
            ?: listOf(TerminalLine("mkdir: created directory", TerminalLineType.SUCCESS))
    }
}

class RmCommand : TerminalCommand {
    override val name = "rm"
    override val description = "Usuwa plik lub katalog z VFS"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
        val disp = context.dispatcher
        return disp?.execute("rm ${args.joinToString(" ")}", context.currentQuest, context.userCode, context.completedQuestIds, context.onSelectQuest, context.onRunTests, context.onBootVm, context.onAskAi)?.filter { !it.text.startsWith("> rm") }
            ?: listOf(TerminalLine("rm: removed", TerminalLineType.OUTPUT))
    }
}

class ChmodCommand : TerminalCommand {
    override val name = "chmod"
    override val description = "Zmienia uprawnienia pliku lub katalogu"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
        val disp = context.dispatcher
        return disp?.execute("chmod ${args.joinToString(" ")}", context.currentQuest, context.userCode, context.completedQuestIds, context.onSelectQuest, context.onRunTests, context.onBootVm, context.onAskAi)?.filter { !it.text.startsWith("> chmod") }
            ?: listOf(TerminalLine("chmod: changed", TerminalLineType.SUCCESS))
    }
}

class DfCommand : TerminalCommand {
    override val name = "df"
    override val description = "Raportuje zużycie pamięci masowej w systemie VFS"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
        val disp = context.dispatcher
        return disp?.execute("df ${args.joinToString(" ")}", context.currentQuest, context.userCode, context.completedQuestIds, context.onSelectQuest, context.onRunTests, context.onBootVm, context.onAskAi)?.filter { !it.text.startsWith("> df") }
            ?: listOf(TerminalLine("Filesystem Size Used Avail Use% Mounted on", TerminalLineType.HEADER))
    }
}

class TreeCommand : TerminalCommand {
    override val name = "tree"
    override val description = "Rysuje hierarchię katalogów VFS"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
        val disp = context.dispatcher
        return disp?.execute("tree ${args.joinToString(" ")}", context.currentQuest, context.userCode, context.completedQuestIds, context.onSelectQuest, context.onRunTests, context.onBootVm, context.onAskAi)?.filter { !it.text.startsWith("> tree") }
            ?: listOf(TerminalLine("/", TerminalLineType.HEADER))
    }
}

class GrepCommand : TerminalCommand {
    override val name = "grep"
    override val description = "Wyszukuje podany ciąg znaków w pliku"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
        val disp = context.dispatcher
        return disp?.execute("grep ${args.joinToString(" ")}", context.currentQuest, context.userCode, context.completedQuestIds, context.onSelectQuest, context.onRunTests, context.onBootVm, context.onAskAi)?.filter { !it.text.startsWith("> grep") }
            ?: listOf(TerminalLine("(Brak dopasowań)", TerminalLineType.OUTPUT))
    }
}
