package com.example.game.terminal

import com.example.game.terminal.commands.AiCommand
import com.example.game.terminal.commands.AliasCommand
import com.example.game.terminal.commands.ArchCommand
import com.example.game.terminal.commands.AsmCommand
import com.example.game.terminal.commands.BootCommand
import com.example.game.terminal.commands.BuildCommand
import com.example.game.terminal.commands.CatCommand
import com.example.game.terminal.commands.ChmodCommand
import com.example.game.terminal.commands.ClearCommand
import com.example.game.terminal.commands.DateCommand
import com.example.game.terminal.commands.DfCommand
import com.example.game.terminal.commands.DmesgCommand
import com.example.game.terminal.commands.EchoCommand
import com.example.game.terminal.commands.GrepCommand
import com.example.game.terminal.commands.HelpCommand
import com.example.game.terminal.commands.HintCommand
import com.example.game.terminal.commands.HistoryCommand
import com.example.game.terminal.commands.IntCommand
import com.example.game.terminal.commands.KillCommand
import com.example.game.terminal.commands.LsCommand
import com.example.game.terminal.commands.ManCommand
import com.example.game.terminal.commands.MemCommand
import com.example.game.terminal.commands.MkdirCommand
import com.example.game.terminal.commands.PsCommand
import com.example.game.terminal.commands.PwdCommand
import com.example.game.terminal.commands.QuestOpenCommand
import com.example.game.terminal.commands.QuestsCommand
import com.example.game.terminal.commands.RegCommand
import com.example.game.terminal.commands.RmCommand
import com.example.game.terminal.commands.SnapshotCommand
import com.example.game.terminal.commands.TestCommand
import com.example.game.terminal.commands.ThemeCommand
import com.example.game.terminal.commands.TouchCommand
import com.example.game.terminal.commands.TreeCommand
import com.example.game.terminal.commands.UnaliasCommand
import com.example.game.terminal.commands.UptimeCommand
import com.example.game.terminal.commands.VersionCommand
import com.example.game.terminal.commands.WhoamiCommand

object DefaultTerminalCommandRegistry {

    fun create(): TerminalCommandRegistry {
        val registry = TerminalCommandRegistry()
        registry.register(HelpCommand())
        registry.register(UptimeCommand())
        registry.register(WhoamiCommand())
        registry.register(ArchCommand())
        registry.register(VersionCommand())
        registry.register(DateCommand())
        registry.register(PwdCommand())
        registry.register(EchoCommand())
        registry.register(ClearCommand())
        registry.register(HistoryCommand())

        // File system commands
        registry.register(LsCommand())
        registry.register(CatCommand())
        registry.register(TouchCommand())
        registry.register(MkdirCommand())
        registry.register(RmCommand())
        registry.register(ChmodCommand())
        registry.register(DfCommand())
        registry.register(TreeCommand())
        registry.register(GrepCommand())

        // Process commands
        registry.register(PsCommand())
        registry.register(KillCommand())
        registry.register(DmesgCommand())

        // Alias commands
        registry.register(AliasCommand())
        registry.register(UnaliasCommand())

        // Quest & simulation commands
        registry.register(QuestsCommand())
        registry.register(QuestOpenCommand())
        registry.register(BuildCommand())
        registry.register(TestCommand())
        registry.register(BootCommand())
        registry.register(HintCommand())
        registry.register(AiCommand())

        // Theme and manual commands
        registry.register(ThemeCommand())
        registry.register(ManCommand())

        // Hardware and snapshot commands
        registry.register(AsmCommand())
        registry.register(MemCommand())
        registry.register(RegCommand())
        registry.register(IntCommand())
        registry.register(SnapshotCommand())

        return registry
    }
}
