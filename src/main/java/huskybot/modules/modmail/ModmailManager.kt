package huskybot.modules.modmail

import huskybot.Database
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.awt.Color
import java.time.Instant
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import javax.print.attribute.standard.JobStateReason

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

        if (event.message.contentRaw.startsWith("//")) {
            return      //Returns when the message starts with a comment
        }

        messageTicket(event, event.jda, event.guild, event.author, event.message, event.message.contentRaw, false)

    }

    /**
     * Method that handles when a private message is sent to the bot
     */
    fun onPrivateMessage(event: MessageReceivedEvent) {

        var guild = event.jda.getGuildById(641672207491137576)

        if (Database.getPreviousGuild(event.author.idLong) != null) {
            guild = event.jda.getGuildById(
                Database.getPreviousGuild(event.author.idLong)!!
            )!!
        }

        val guildString = "**${guild?.name}** (${guild?.idLong})"

        val confirmation = Database.getConfirmationState(event.author.idLong)

        if (confirmation === 1) {
            guild?.let { messageTicket(event, event.jda, guild, event.author, event.message, event.message.contentRaw, false) }
        } else {
            event.channel.sendMessageEmbeds(
                EmbedBuilder()
                    .setTitle("Confirmation")
                    .setDescription(
                    "Curently sending to $guildString, press ``Confirm`` to continue. \n" +
                            "Need to message a different server? Press ``Select Guild`` to select a diffent guild. \n" +
                            "To cancel this request, press ``Cancel``.")
                    .setColor(Color.YELLOW)
                    .build()
            ).addActionRow(
                Button.success("confirm", "Confirm"),           //Confirm button for confirming guild
                Button.secondary("select", "Select Guild"),     //Select guild button for choosing a different guild
                Button.danger("cancel", "Cancel")               //Cancel button for canceling the message
            ).queue()
        }
    }

    /**
     * Function that handles when a user interacts with a context button in
     * direct messages
     */
    fun onButtonPress(event: ButtonInteractionEvent) {

        when (event.componentId) {
            "confirm" -> {
                val guild = event.jda.getGuildById( Database.getPreviousGuild(event.user.idLong)!! )

                event.message.delete().queue()

                buttonMessageTicket(event, event.jda, guild!!, event.user)
            }
            "select" -> null
            "cancel" -> {
                /* Delete Message and Respond */
                event.message.delete().queue {
                    event.channel.sendMessageEmbeds(
                        EmbedBuilder()
                            .setDescription("❌ Request Cancelled ❌")
                            .setColor(Color.RED)
                            .build()
                    ).queue()
                }
            }
        }
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
        val member = event.member!!
        val hook = event.hook
        val subject = event.getValue("subject")?.asString
        val body = event.getValue("body")?.asString

        event.reply("✅ **Ticket Created** ✅").setEphemeral(true)
            .queue()

        /* Add Server To Database */
        Database.setPreviousGuild(member.user.idLong, guild!!.idLong)

        createTicket(event, event.jda, guild, member.user, null, body!!, false)
    }

    /**
     * Interface function that handles a request to close a ticket
     */
    fun tryCloseTicket(event: SlashCommandInteractionEvent, guild: Guild, user: User, reason: String) {
        closeTicket(event, guild, user, reason)
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

        /* Setup EmbedBuilder and User Info */
        val userInfo = arrayOf(userString, userAvatarURL!!)
        val builder = ModmailEmbedBuilder(jda, guild, userInfo, null, messageString)

        /* Open New Channel and Create Log of New Ticket */

        val channel = category?.createTextChannel("${author.name}${author.discriminator}")?.setTopic("${author.idLong}")
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
            builder.guildReceiveEmbed

        )?.queue{
            /* Send Confirmation Response to User */
            author.openPrivateChannel().queue{
                it.sendMessageEmbeds(
                builder.pmSendEmbed
                ).queue()
            }
        }
    }

    /**
     * Private function that handles sending a message to an active ticket within a guild
     * @param event MessageRecievedEvent object
     * @param jda JDA object
     * @param guild Guild object
     * @param author Author of the message/creator of the modal
     * @param message Message from the author given as a Message object
     * @param messageString Message from the author given as a string
     * @param isAnonymous Boolean that indicated if the ticket author wants to be annonymous
     */
    private fun messageTicket(event: MessageReceivedEvent, jda: JDA, guild: Guild, author: User, message: Message?, messageString: String, isAnonymous: Boolean) {

        /* Get Guild Information */
        val category = guild.getCategoryById(Database.getCategory(guild.idLong)!!)

        /* Split Function for Guild and Private Messages */
        if (event.isFromGuild) {
            /* Get Moderator Info and Check if Anon */
            val modString = if (!isAnonymous) "${event.author.name}#${event.author.discriminator} (${event.author.idLong})" else "Anonymous#0000"
            val modAvatarURL = if (!isAnonymous) event.author.avatarUrl else "https://cdn.discordapp.com/embed/avatars/0.png"
            val modInfo = arrayOf(modString, modAvatarURL!!)

            /* Get the User From the Channel Info */
            val channel = event.channel.asTextChannel()
            val user = guild.getMemberById(channel.topic!!.toLong())?.user
            val parsedUserInfo = arrayOf("${user?.name}#${user?.discriminator} (${user?.idLong})", user?.avatarUrl!!)

            /* Setup EmbedBuilder */
            val builder = ModmailEmbedBuilder(jda, guild, parsedUserInfo, modInfo, messageString)

            /* Send Message Embeds to Guild and User */
            channel.sendMessageEmbeds(
                builder.guildSendEmbed
            ).queue{
                user?.openPrivateChannel()?.queue {
                    it.sendMessageEmbeds(builder.pmRecieveEmbed).queue()
                }
            }

        } else {

            /* Get Ticket Channel */
            var ticketChannel:TextChannel? = null

            /* Check if User is Anon */
            val userString = if (!isAnonymous) "${author.name}#${author.discriminator} (${author.idLong})" else "Anonymous#0000"
            val userAvatarURL = if (!isAnonymous) author.avatarUrl else "https://cdn.discordapp.com/embed/avatars/0.png"

            /* Get Channel */
            ticketChannel = guild.getTextChannelsByName("${author.name}${author.discriminator}", true).get(0)

            if (ticketChannel == null) {
                return
            }

            /* Setup EmbedBuilder and User Info */
            val userInfo = arrayOf(userString, userAvatarURL!!)
            val builder = ModmailEmbedBuilder(jda, guild, userInfo, null, messageString)

            ticketChannel.sendMessageEmbeds(
                builder.guildReceiveEmbed
            ).queue{
                event.channel.sendMessageEmbeds(
                    builder.pmSendEmbed
                ).queue()
            }
        }
    }

    /**
     * Private function that handles sending a message to an active ticket when a user
     * sends a direct message
     * @param event ButtonInteractionEvent object
     * @param jda JDA object
     * @param guild Guild object
     * @param author User who interacted with the bot
     */
    private fun buttonMessageTicket(event: ButtonInteractionEvent, jda: JDA, guild: Guild, author: User) {
        /* Get Guild Information */
        val category = guild.getCategoryById(Database.getCategory(guild.idLong)!!)

        /* Get Ticket Channel */
        var ticketChannel:TextChannel? = null

        /* Build User Info */
        val userString = "${author.name}#${author.discriminator} (${author.idLong})"
        val userAvatarURL = author.avatarUrl

        /* Get Channel */
        ticketChannel = guild.getTextChannelsByName("${author.name}${author.discriminator}", true).get(0)

        if (ticketChannel == null) {
            return
        }

        /* Get message */
        val messages = event.channel.iterableHistory
            .takeAsync(3)
            .thenApply {
                    list ->
                list.stream()
                    .filter{m -> m.getAuthor().equals(event.user)}
                    .collect(Collectors.toList())
            }
        val message = messages.get().get(0).contentRaw

        /* Setup EmbedBuilder and User Info */
        val userInfo = arrayOf(userString, userAvatarURL!!)
        val builder = ModmailEmbedBuilder(jda, guild, userInfo, null, message)

        ticketChannel.sendMessageEmbeds(
            builder.guildReceiveEmbed
        ).queue{
            event.channel.sendMessageEmbeds(
                builder.pmSendEmbed
            ).queue()
        }
    }

    /**
     * Private function that handles closing a ticket
     * @param event SlashCommandInteractionEvent object
     * @param guild Guild object
     * @param user User object of who initaited the closing of the ticket
     * @param reason Reason for why the ticket was closed
     */
    private fun closeTicket(event: SlashCommandInteractionEvent, guild: Guild, user: User, reason: String) {

        /* Get Guild and Ticket Info */
        val ticketLog = guild.getTextChannelById( Database.getModmailLog(guild.idLong)!! )
        val channel = event.channel.asTextChannel()
        val ticketAuthor = guild.getMemberById(channel.topic!!.toLong())?.user

        /* Log Ticket Closing */

        event.replyEmbeds(
            EmbedBuilder()
                .setTitle("Ticket Closed")
                .addField(MessageEmbed.Field("Reason", reason, true))
                .setFooter("${user.name}#${user.discriminator}", user.avatarUrl)
                .setColor(Color.RED)
                .setTimestamp(Instant.now())
                .build()
        )?.queue()

        /* Delete Channel and Notify User */

        channel.sendMessageEmbeds(
            EmbedBuilder()
                .setTitle("Ticket Closed")
                .setDescription("Deleteing channel in 10 seconds...")
                .setColor(Color.RED)
                .build()
        ).queue()

        user.openPrivateChannel().queue{
            it.sendMessageEmbeds(
                EmbedBuilder()
                    .setTitle("Ticket Closed")
                    .addField(MessageEmbed.Field("Reason", reason, true))
                    .setFooter("${user.name}#${user.discriminator}", user.avatarUrl)
                    .setColor(Color.RED)
                    .setTimestamp(Instant.now())
                    .build()
            ).queue()
        }

        channel.delete().queueAfter(10, TimeUnit.SECONDS)

    }
}