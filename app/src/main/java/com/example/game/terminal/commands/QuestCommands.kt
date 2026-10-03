package com.example.game.terminal.commands

import com.example.data.game.QuestsData
import com.example.data.game.TerminalLine
import com.example.data.game.TerminalLineType
import com.example.game.terminal.CommandContext
import com.example.game.terminal.TerminalCommand

class QuestsCommand : TerminalCommand {
    override val name = "quests"
    override val aliases = listOf("tasks")
    override val description = "Wyświetla mapę faz architektonicznych i listę zadań"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
        val disp = context.dispatcher
        return disp?.execute("quests", context.currentQuest, context.userCode, context.completedQuestIds, context.onSelectQuest, context.onRunTests, context.onBootVm, context.onAskAi)?.filter { !it.text.startsWith("> quests") }
            ?: listOf(TerminalLine("Lista zadań GenesisOS", TerminalLineType.HEADER))
    }
}

class QuestOpenCommand : TerminalCommand {
    override val name = "quest"
    override val aliases = listOf("open")
    override val description = "Wybiera i otwiera określone zadanie w edytorze kodu"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
        val disp = context.dispatcher
        return disp?.execute("quest ${args.joinToString(" ")}", context.currentQuest, context.userCode, context.completedQuestIds, context.onSelectQuest, context.onRunTests, context.onBootVm, context.onAskAi)?.filter { !it.text.startsWith("> quest") }
            ?: listOf(TerminalLine("Otwarto zadanie", TerminalLineType.SUCCESS))
    }
}

class BuildCommand : TerminalCommand {
    override val name = "build"
    override val aliases = listOf("compile", "make")
    override val description = "Kompiluje bieżący plik źródłowy jądra"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
        val disp = context.dispatcher
        return disp?.execute("build", context.currentQuest, context.userCode, context.completedQuestIds, context.onSelectQuest, context.onRunTests, context.onBootVm, context.onAskAi)?.filter { !it.text.startsWith("> build") }
            ?: listOf(TerminalLine("Kompilacja powiodła się.", TerminalLineType.SUCCESS))
    }
}

class TestCommand : TerminalCommand {
    override val name = "test"
    override val description = "Uruchamia zestaw testów akceptacyjnych dla aktywnego zadania"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
        val currentQuest = context.currentQuest
        if (currentQuest == null) {
            return listOf(TerminalLine("Wybierz najpierw zadanie (wpisz 'quests').", TerminalLineType.WARNING))
        } else {
            context.onRunTests()
            return listOf(TerminalLine("Uruchamianie zestawu testów akceptacyjnych dla ${currentQuest.id}...", TerminalLineType.SYSTEM))
        }
    }
}

class BootCommand : TerminalCommand {
    override val name = "boot"
    override val aliases = listOf("run", "runos")
    override val description = "Uruchamia maszynę wirtualną QEMU z jądrem GenesisOS"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
        context.onBootVm()
        return listOf(
            TerminalLine("Inicjalizacja emulatora QEMU x86...", TerminalLineType.SYSTEM),
            TerminalLine("$ qemu-system-i386 -fda /build/boot.bin -m 64M -vga std", TerminalLineType.OUTPUT)
        )
    }
}

class HintCommand : TerminalCommand {
    override val name = "hint"
    override val description = "Wyświetla wskazówkę techniczną i architektoniczną do aktywnego zadania"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
        val disp = context.dispatcher
        return disp?.execute("hint", context.currentQuest, context.userCode, context.completedQuestIds, context.onSelectQuest, context.onRunTests, context.onBootVm, context.onAskAi)?.filter { !it.text.startsWith("> hint") }
            ?: listOf(TerminalLine("Wskazówka architektoniczna", TerminalLineType.HEADER))
    }
}

class AiCommand : TerminalCommand {
    override val name = "ai"
    override val aliases = listOf("ask", "mentor")
    override val description = "Konsultuje się z mentorką Adą (AI Kernel Architect)"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
        if (args.isEmpty()) {
            return listOf(TerminalLine("Użycie: ai <treść Twojego pytania architektonicznego lub problemu>", TerminalLineType.ERROR))
        }
        val prompt = args.joinToString(" ")
        context.onAskAi(prompt)
        return listOf(TerminalLine("Wysyłanie zapytania do mentorki Ady...", TerminalLineType.SYSTEM))
    }
}
