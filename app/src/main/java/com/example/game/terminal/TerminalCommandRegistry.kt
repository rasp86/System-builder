package com.example.game.terminal

import com.example.data.game.TerminalLine
import com.example.data.game.TerminalLineType

/**
 * Registry and dispatcher for all modular terminal commands.
 */
class TerminalCommandRegistry {

    private val commandMap = mutableMapOf<String, TerminalCommand>()
    private val commandList = mutableListOf<TerminalCommand>()

    fun register(command: TerminalCommand): TerminalCommandRegistry {
        commandList.add(command)
        commandMap[command.name.lowercase()] = command
        for (alias in command.aliases) {
            commandMap[alias.lowercase()] = command
        }
        return this
    }

    fun getCommand(name: String): TerminalCommand? {
        return commandMap[name.lowercase()]
    }

    fun getAllCommands(): List<TerminalCommand> {
        return commandList.distinctBy { it.name }
    }

    fun hasCommand(name: String): Boolean {
        return commandMap.containsKey(name.lowercase())
    }

    fun execute(
        commandName: String,
        args: List<String>,
        context: CommandContext
    ): List<TerminalLine>? {
        val cmd = commandMap[commandName.lowercase()] ?: return null
        return try {
            cmd.execute(args, context)
        } catch (e: Exception) {
            listOf(TerminalLine("Błąd wykonania '${commandName}': ${e.message}", TerminalLineType.ERROR))
        }
    }
}
