package com.example.game.terminal.commands

import com.example.data.game.TerminalLine
import com.example.data.game.TerminalLineType
import com.example.game.terminal.CommandContext
import com.example.game.terminal.TerminalCommand

class PsCommand : TerminalCommand {
    override val name = "ps"
    override val aliases = listOf("top", "tasks_list")
    override val description = "Zwraca listę aktywnych procesów w tabeli schedulera"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
        return context.processScheduler.listProcesses()
    }
}

class KillCommand : TerminalCommand {
    override val name = "kill"
    override val aliases = listOf("pkill", "killall")
    override val description = "Kończy działanie procesu wg numeru PID lub nazwy"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
        val isNameMatch = args.any { it.matches("[a-zA-Z_]+".toRegex()) }
        return context.processScheduler.killProcess(args, isNameMatch)
    }
}

class DmesgCommand : TerminalCommand {
    override val name = "dmesg"
    override val description = "Odczytuje komunikaty z bufora pierścieniowego jądra"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
        val disp = context.dispatcher
        return disp?.execute("dmesg ${args.joinToString(" ")}", context.currentQuest, context.userCode, context.completedQuestIds, context.onSelectQuest, context.onRunTests, context.onBootVm, context.onAskAi)?.filter { !it.text.startsWith("> dmesg") }
            ?: listOf(TerminalLine("dmesg: bufor jądra", TerminalLineType.HEADER))
    }
}
