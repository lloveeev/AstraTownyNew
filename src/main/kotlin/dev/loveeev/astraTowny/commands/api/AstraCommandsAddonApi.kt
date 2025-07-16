package dev.loveeev.astratowny.commands.api

import org.bukkit.command.CommandSender

typealias CommandHandler = (sender: CommandSender, args: Array<out String>) -> Unit

object AstraCommandsAddonApi {

    private val dynamicArgs: MutableMap<BaseCommandType, MutableMap<String, CommandHandler>> = mutableMapOf()

    class RegisteredArgument(
        private val commandType: BaseCommandType,
        private val argument: String
    ) {
        fun setExecutor(handler: CommandHandler): RegisteredArgument {
            val commandArgs = dynamicArgs.getOrPut(commandType) { mutableMapOf() }
            commandArgs[argument.lowercase()] = handler
            return this
        }
    }

    fun registerArgument(commandType: BaseCommandType, argument: String): RegisteredArgument {
        return RegisteredArgument(commandType, argument)
    }

    fun handleDynamicArgument(
        commandType: BaseCommandType,
        sender: CommandSender,
        args: Array<out String>
    ): Boolean {
        if (args.isEmpty()) return false
        val commandArgs = dynamicArgs[commandType] ?: return false
        val subArg = args[0].lowercase()
        val handler = commandArgs[subArg] ?: return false
        handler.invoke(sender, args.drop(1).toTypedArray())
        return true
    }
}

