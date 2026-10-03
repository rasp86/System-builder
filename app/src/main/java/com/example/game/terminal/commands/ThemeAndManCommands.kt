package com.example.game.terminal.commands

import com.example.data.game.TerminalLine
import com.example.data.game.TerminalLineType
import com.example.data.game.engine.ManualPagesProvider
import com.example.game.terminal.CommandContext
import com.example.game.terminal.TerminalCommand

class ThemeCommand : TerminalCommand {
    override val name = "theme"
    override val aliases = listOf("colorscheme", "scheme")
    override val description = "Zmienia motyw kolorystyczny terminala CRT"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
        val themeName = args.getOrNull(0)?.lowercase()
        val availableThemes = listOf(
            "amber" to "Klasyczny bursztynowy monitor CRT (MDA/CGA)",
            "green" to "Klasyczny zielony fosfor (VT220 / IBM 3270)",
            "cyan" to "Nowoczesny chłodny cyjan (Cyberpunk / Terminal)",
            "matrix" to "Matrix Digital Rain (Neon Green / Hacker)",
            "monochrome" to "Biały papierowy fosfor (VT100 Monochrome)"
        )

        if (themeName == null) {
            val lines = mutableListOf<TerminalLine>()
            lines.add(TerminalLine("Dostępne schematy kolorów terminala:", TerminalLineType.HEADER))
            availableThemes.forEach { (id, desc) ->
                val active = if (context.getThemeId().lowercase() == id) " [AKTYWNY]" else ""
                lines.add(TerminalLine("  * $id$active - $desc", TerminalLineType.OUTPUT))
            }
            lines.add(TerminalLine("Użycie: theme <nazwa> (np. theme green)", TerminalLineType.SYSTEM))
            return lines
        }

        val found = availableThemes.find { it.first == themeName }
        return if (found != null) {
            context.setThemeId(found.first)
            listOf(TerminalLine("Zmieniono motyw kolorystyczny na: ${found.first} (${found.second})", TerminalLineType.SUCCESS))
        } else {
            listOf(TerminalLine("Nieznany schemat '$themeName'. Dostępne: ${availableThemes.joinToString { it.first }}", TerminalLineType.ERROR))
        }
    }
}

class ManCommand : TerminalCommand {
    override val name = "man"
    override val aliases = listOf("manual")
    override val description = "Strony podręcznika systemowego dla instrukcji x86 i funkcji jądra"

    override fun execute(args: List<String>, context: CommandContext): List<TerminalLine> {
        val pageName = args.getOrNull(0) ?: "man"
        return ManualPagesProvider.getManualPage(pageName)
    }
}
