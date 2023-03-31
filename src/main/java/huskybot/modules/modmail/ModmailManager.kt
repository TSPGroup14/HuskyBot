package huskybot.modules.modmail

import huskybot.Database
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.awt.Color
import java.time.Instant

object ModmailManager {

    /**
     * Method that handles when a message is sent within a ticket channel, will ignore the message
     * if otherwise
     */
    fun onGuildMessage(event: MessageReceivedEvent) {

        /* Get Modmail Category and Guild Settings */
        val category = Database.getCategory(event.guild.idLong)
        val logChannel = Database.getModmailLog(event.guild.idLong)

        if (event.message.category!!.idLong != category) {
            return
        }

        if (event.message.channel.idLong == logChannel) {
            return
        }

    }

    /**
     * Method that handles when a private message is sent to the bot
     */
    fun onPrivateMessage(event: MessageReceivedEvent) {

    }

    /**
     * Method that handles when a user submits a ticket modal, will return
     * if modmail is not enabled within the guild
     */
    fun onModalSubmit(event: ModalInteractionEvent) {

        /* Check if Modmail is Enabled */
        if (!Database.getModmailState(event.guild!!.idLong)) {
            event.reply("❌ **Modmail is not enabled in this server!** ❌").setEphemeral(true)
                .queue()
            return
        }

        /* Get Values */
        val guild = event.guild
        val member = event.member
        val hook = event.hook
        val subject = event.getValue("subject")?.asString
        val body = event.getValue("body")?.asString

        event.reply("✅ **Ticket Created** ✅").setEphemeral(true)
            .queue()

        createTicket(event, event.jda, guild!!, member!!.user, null, body!!, false)
    }

    /**
     * Private function that handles creating a new ticket within a guild
     * @param event Event object
     * @param jda JDA object
     * @param guild Guild object
     * @param author Author of the message/creator of the modal
     * @param message Message from the author given as a Message object
     * @param messageString Message from the author given as a string
     * @param isAnonymous Boolean that indicated if the ticket author wants to be annonymous
     */
    private fun createTicket(event: GenericEvent, jda: JDA, guild: Guild, author: User, message: Message?, messageString: String, isAnonymous: Boolean) {

        /* Get Guild Information */
        val category = guild.getCategoryById(Database.getCategory(guild.idLong)!!)
        val logChannel = guild.getTextChannelById(Database.getModmailLog(guild.idLong)!!)

        /* Check if User is Anon */
        val userString = if (!isAnonymous) "${author.name}#${author.discriminator} (${author.idLong})" else "Anonymous#0000"
        val userAvatarURL = if (!isAnonymous) author.avatarUrl else "https://cdn.discordapp.com/embed/avatars/0.png"

        /* Open New Channel and Create Log of New Ticket */

        val channel = category?.createTextChannel("${author.name}${author.discriminator}")
            ?.submit()

        val logEmbed = EmbedBuilder()
            .setTitle("Ticket Opened")
            .setAuthor(userString, null, userAvatarURL)
            .setColor(Color.GREEN)
            .setTimestamp(Instant.now())
            .build()

        logChannel?.sendMessageEmbeds(logEmbed)?.queue()        //Log new ticket

        /* Send Ticket Message to New Channel */

        channel?.get()?.sendMessageEmbeds(
            //Inital embed that contains information on how the modmail system works
            EmbedBuilder()
            .setTitle("New Ticket")
            .setDescription(
                "Type a message in this channel to reply. Messages starting with `//` will be ignored. To close this ticket, " +
                        "Use the command `/ticket close`")
            .setFooter(jda.selfUser.name, jda.selfUser.avatarUrl)
            .setTimestamp(Instant.now())
            .setColor(Color.YELLOW)
            .build(),

            //Embeded message with user message
            EmbedBuilder()
                .setTitle("Message recieved")
                .setDescription(messageString)
                .setAuthor(userString, null, userAvatarURL)
                .setFooter(jda.selfUser.name, jda.selfUser.avatarUrl)
                .setTimestamp(Instant.now())
                .setColor(Color.RED)
                .build()
        )?.queue{

            /* Send Confirmation Response to User */
            author.openPrivateChannel().queue{
                it.sendMessageEmbeds(
                    EmbedBuilder()
                        .setTitle("Message Sent")
                        .setDescription(messageString)
                        .setFooter("${guild.name} (${guild.idLong})", guild.iconUrl)
                        .setTimestamp(Instant.now())
                        .setColor(Color.GREEN)
                        .build()
                ).queue()
            }
        }
    }
}