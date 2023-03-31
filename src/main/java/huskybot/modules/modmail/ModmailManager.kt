package huskybot.modules.modmail

import huskybot.Database
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

object ModmailManager {

    /**
     * Method that handles when a message is sent within a ticket channel, will ignore the message
     * if otherwise
     */
    fun onGuildMessage(event: MessageReceivedEvent) {

        /* Get Modmail Category and Guild Settings */
        val category = Database.getCategory(event.guild.idLong)

        if (event.message.category!!.idLong != category) {
            return
        }

    }

    /**
     * Method that handles when a private message is sent to the bot
     */
    fun onPrivateMessage(event: MessageReceivedEvent) {

    }
}