package huskybot.handlers

import huskybot.modules.leveling.MessageCounter.updateLevel
import huskybot.modules.logging.GuildLogManager.logUserJoin
import huskybot.modules.logging.GuildLogManager.logUserLeave
import huskybot.modules.modmail.GuildSelector.getGuildsPrevious
import huskybot.modules.modmail.ModmailManager.onButtonPress
import huskybot.modules.modmail.ModmailManager.onGuildMessage
import huskybot.modules.modmail.ModmailManager.onGuildSelect
import huskybot.modules.modmail.ModmailManager.onPrivateMessage
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.EventListener

class EventHandler : EventListener {
    override fun onEvent(event: GenericEvent) {
        //This is where we will add specific event triggers for everything that isn't a command/interaction.
        when (event) {
            is GuildMemberJoinEvent -> logUserJoin(event)
            is GuildMemberRemoveEvent -> logUserLeave(event)
            is MessageReceivedEvent -> onMessageRecieved(event)
            is ButtonInteractionEvent -> onInteractionButtonPressed(event)
            is StringSelectInteractionEvent -> onGuildSelect(event)
        }
    }

    private fun onMessageRecieved(event: MessageReceivedEvent) {

        if (event.author.isBot) {
            return          //Return if the message author is a bot
        }

        if (event.isFromGuild) {
            updateLevel(event)
            onGuildMessage(event)
        } else {
            onPrivateMessage(event)
        }
    }

    private fun onInteractionButtonPressed(event: ButtonInteractionEvent) {

        if (event.componentId.contains("modmail")) {
            onButtonPress(event)
            return
        }

        when (event.componentId) {
            "ticket:prevpage" -> getGuildsPrevious(event)
        }

    }
}