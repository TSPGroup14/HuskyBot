package huskybot.handlers

import huskybot.HuskyBot
import huskybot.cmdFramework.Arguments
import huskybot.cmdFramework.Context
import huskybot.utils.separate
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.GenericMessageEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.EventListener
import warden.framework.CommandScanner

class CommandHandler : EventListener {
    init {
        HuskyBot.log.info("${commands.size} commands in registry")
    }

    override fun onEvent(event: GenericEvent) {
        if (event is MessageReceivedEvent) {
            //Only handle the command if its from a guild
            if (event.isFromGuild) {
                //onGuildMessageReceived(event)
            }
        } else if (event is SlashCommandInteractionEvent) {
            onSlashCommandEvent(event)
        }
    }

    /**
     * Checks if the event name matches with a registered command and executes the command if a match is found.
     */
    private fun onSlashCommandEvent(e: SlashCommandInteractionEvent) {

        /* Get command info and arguments */
        val command = e.name

        val foundCommand = commands[command]
            ?: commands.values.firstOrNull { it.properties.aliases.contains(command) }
            ?: return

        try {
            foundCommand.runChecks(Context(e, Arguments.SlashArguments(e)))
        } catch (e: Exception) {
            e.stackTrace
        }
    }

    companion object {
        val commands = CommandScanner("huskybot.commands").scan().toMutableMap()
    }
}