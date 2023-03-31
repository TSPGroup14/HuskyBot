package huskybot.handlers

import huskybot.HuskyBot
import huskybot.cmdFramework.Arguments
import huskybot.cmdFramework.CommandScanner
import huskybot.cmdFramework.Context
import huskybot.modules.modmail.ModmailManager.onModalSubmit
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.EventListener

/**
 * Command and Interaction handler for discord's interaction api
 */
class CommandHandler : EventListener {
    init {
        HuskyBot.log.info("${commands.size} commands in registry")
    }

    override fun onEvent(event: GenericEvent) {
        when (event) {
            is SlashCommandInteractionEvent ->
                if (event.isFromGuild)                  //Only handle the command if its from a guild
                    onSlashCommandEvent(event)

            is MessageReceivedEvent ->
                if (event.isFromGuild) {                //Only handle the command if its from a guild
                    //onGuildMessageRecieved(event)
                }

            is ModalInteractionEvent ->
                onModalSubmitEvent(event)
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

    fun onModalSubmitEvent(e: ModalInteractionEvent) {

        val modal = e.modalId

        when (modal) {
            "ticket" -> onModalSubmit(e)
        }

    }

    companion object {
        val commands = CommandScanner("huskybot.commands").scan().toMutableMap()
    }
}