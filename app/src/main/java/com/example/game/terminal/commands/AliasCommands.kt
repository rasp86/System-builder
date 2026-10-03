package com.example.game.terminal.commands

import com.example.data.game.TerminalLine
import com.example.data.game.TerminalLineType
import com.example.game.terminal.CommandContext
import com.example.game.terminal.TerminalCommand

class AliasCommand : TerminalCommand {
    override val name = "alias"
    override val description = "Definiuje lub wyświetla zdefiniowane aliasy powłoki"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
        val disp = context.dispatcher
        return disp?.execute("alias ${args.joinToString(" ")}", context.currentQuest, context.userCode, context.completedQuestIds, context.onSelectQuest, context.onRunTests, context.onBootVm, context.onAskAi)?.filter { !it.text.startsWith("> alias") }
            ?: listOf(TerminalLine("alias: dispatcher niedostępny", TerminalLineType.ERROR))
    }
}

class UnaliasCommand : TerminalCommand {
    override val name = "unalias"
    override val description = "Usuwa zdefiniowany alias powłoki"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
        val disp = context.dispatcher
        return disp?.execute("unalias ${args.joinToString(" ")}", context.currentQuest, context.userCode, context.completedQuestIds, context.onSelectQuest, context.onRunTests, context.onBootVm, context.onAskAi)?.filter { !it.text.startsWith("> unalias") }
            ?: listOf(TerminalLine("unalias: dispatcher niedostępny", TerminalLineType.ERROR))
    }
}
