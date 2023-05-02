package huskybot.modules.modmail

import huskybot.Database
import huskybot.HuskyBot
import huskybot.modules.modmail.GuildSelector.getGuilds
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
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.awt.Color
import java.time.Instant
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

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

        val guild = event.jda.getGuildById( Database.getPreviousGuild(event.author.idLong)!! )
            ?: event.author.mutualGuilds[0]

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
                    .setColor(HuskyBot.color)
                    .build()
            ).addActionRow(
                Button.success("modmail:confirm", "Confirm"),           //Confirm button for confirming guild
                Button.secondary("modmail:select", "Select Guild"),     //Select guild button for choosing a different guild
                Button.danger("modmail:cancel", "Cancel")               //Cancel button for canceling the message
            ).queue()
        }
    }

    /**
     * Function that handles when a user interacts with a context button in
     * direct messages
     */
    fun onButtonPress(event: ButtonInteractionEvent) {

        when (event.componentId) {
            "modmail:confirm" -> {
                val guild = event.jda.getGuildById( Database.getPreviousGuild(event.user.idLong)!! )
                    ?: event.user.mutualGuilds[0]

                event.message.delete().queue()      //Deletes the original message prompt

                /* Check if User is in Guild */
                if (!guild?.isMember(event.user)!!) {
                    event.reply("❌ **You are not apart of this guild!** ❌")
                        .queue()
                    return
                }

                /* Check if Guild has Modmail Enabled */
                if (!Database.getModmailState(guild.idLong)) {
                    event.reply("❌ **This guild does not have modmail enabled!** ❌")
                        .queue()
                    return
                }

                /* Get Modmail Category */
                val category = guild.getCategoryById(Database.getCategory(guild.idLong)!!)

                /* Check if User Has an Open Ticket */
                var ticketChannel:TextChannel? = null
                for (channel in category?.textChannels!!) {
                    if (channel.topic?.toLongOrNull() == event.user.idLong) {
                        ticketChannel = channel
                        break
                    }
                }

                if (ticketChannel == null) {

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

                    createTicket(event, event.jda, guild!!, event.user, null, message, false)
                    return
                }

                buttonMessageTicket(event, event.jda, guild!!, event.user)
            }
            "modmail:select" -> {
                getGuilds(event)
            }
            "modmail:cancel" -> {
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

        /* Check if a Ticket is Already Active */
        val category = guild?.getCategoryById(Database.getCategory(guild.idLong)!!)

        var ticketChannel:TextChannel? = null
        for (channel in category?.textChannels!!) {
            if (channel.topic?.toLongOrNull() == event.user.idLong) {
                ticketChannel = channel
                break
            }
        }

        if (ticketChannel != null) {
            event.reply("❌ **You already have an actve ticket!** ❌")
                .setEphemeral(true)
                .queue()
            return
        }

        /* Create Ticket */
        event.reply("✅ **Ticket Created** ✅").setEphemeral(true)
            .queue()

        /* Add Server To Database */
        Database.setPreviousGuild(member.user.idLong, guild!!.idLong)

        createTicket(event, event.jda, guild, member.user, null, body!!, false)
    }

    /**
     * Method that handles when a user selects a guild through the guild selection
     * menu, will return if modmail is not enabled in the guild
     */
    fun onGuildSelect(event: StringSelectInteractionEvent) {

        event.message.delete().queue()  //Delete original message

        /* Get Guild From Menu Value */
        val id = event.values.get(0).subSequence(12, event.values.get(0).length).toString()
        val guild = event.jda.getGuildById(id.toLong())


        /* Check if Modmail is Enabled */
        if (!Database.getModmailState(id.toLong())) {
            event.reply("❌ **Modmail is not enabled in this server!** ❌").setEphemeral(true)
                .queue()
            return
        }

        /* Get Modmail Category */
        val category = guild?.getCategoryById(Database.getCategory(guild.idLong)!!)

        /* Check if User Has an Open Ticket */
        var ticketChannel:TextChannel? = null
        for (channel in category?.textChannels!!) {
            if (channel.topic?.toLongOrNull() == event.user.idLong) {
                ticketChannel = channel
                break
            }
        }

        if (ticketChannel == null) {

            /* Get message */
            val messages = event.channel.iterableHistory
                .takeAsync(3)
                .thenApply {
                        it.stream()
                        .filter{m -> m.getAuthor().equals(event.user)}
                        .collect(Collectors.toList())
                }
            val message = messages.get().get(0).contentRaw

            createTicket(event, event.jda, guild!!, event.user, null, message, false)
            return
        }

        buttonMessageTicket(event, event.jda, guild!!, event.user)
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

        /* Set User's Previous Guild to This Guild */
        Database.setPreviousGuild(author.idLong, guild.idLong)

        /* Get Guild Information */
        val category = guild.getCategoryById(Database.getCategory(guild.idLong)!!)
        val logChannel = guild.getTextChannelById(Database.getModmailLog(guild.idLong)!!)

        /* Check if User is Anon */
        val userString = if (!isAnonymous) "${author.name}#${author.discriminator} (${author.idLong})" else "Anonymous#0000"
        val userAvatarURL = if (!isAnonymous) author.avatarUrl ?: "https://cdn.discordapp.com/embed/avatars/0.png"
            else "https://cdn.discordapp.com/embed/avatars/0.png"

        /* Setup EmbedBuilder and User Info */
        val userInfo = arrayOf(userString, userAvatarURL)
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
            .setColor(HuskyBot.color)
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
            val modAvatarURL = if (!isAnonymous) event.author.avatarUrl ?: "https://cdn.discordapp.com/embed/avatars/0.png"
                else "https://cdn.discordapp.com/embed/avatars/0.png"
            val modInfo = arrayOf(modString, modAvatarURL)

            /* Get the User From the Channel Info */
            val channel = event.channel.asTextChannel()
            val user = guild.getMemberById(channel.topic!!.toLong())?.user
            val userAvatar = user?.avatarUrl ?: "https://cdn.discordapp.com/embed/avatars/0.png"
            val parsedUserInfo = arrayOf("${user?.name}#${user?.discriminator} (${user?.idLong})", userAvatar)

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

            /* Send Attachments If There Are Any */
            if (!event.message.attachments.isEmpty()) {
                for (attachment in event.message.attachments) {
                    channel.sendMessage(attachment.url).queue()
                    user?.openPrivateChannel()?.queue {
                        it.sendMessage(attachment.url).queue()
                    }
                }
            }

        } else {

            /* Get Ticket Channel */
            var ticketChannel:TextChannel? = null

            /* Check if User is Anon */
            val userString = if (!isAnonymous) "${author.name}#${author.discriminator} (${author.idLong})" else "Anonymous#0000"
            val userAvatarURL = if (!isAnonymous) author.avatarUrl ?: "https://cdn.discordapp.com/embed/avatars/0.png"
                else "https://cdn.discordapp.com/embed/avatars/0.png"

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

            /* Send Attachments If There Are Any */
            if (!event.message.attachments.isEmpty()) {
                for (attachment in event.message.attachments) {
                    ticketChannel.sendMessage(attachment.url).queue()
                    event.channel.sendMessage(attachment.url).queue()
                }
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
    private fun buttonMessageTicket(event: GenericComponentInteractionCreateEvent, jda: JDA, guild: Guild, author: User) {
        /* Get Guild Information */
        val category = guild.getCategoryById(Database.getCategory(guild.idLong)!!)

        /* Get Ticket Channel */
        var ticketChannel:TextChannel? = null

        /* Build User Info */
        val userString = "${author.name}#${author.discriminator} (${author.idLong})"
        val userAvatarURL = author.avatarUrl ?: "https://cdn.discordapp.com/embed/avatars/0.png"

        /* Get Channel */
        ticketChannel = guild.getTextChannelsByName("${author.name}${author.discriminator}", true).get(0)

        if (ticketChannel == null) {
            return
        }

        /* Get message */
        val messages = event.channel.iterableHistory
            .takeAsync(3)
            .thenApply {
                    it.stream()
                    .filter{m -> m.getAuthor().equals(event.user)}
                    .collect(Collectors.toList())
            }
        val message = messages.get()[0].contentRaw

        /* Setup EmbedBuilder and User Info */
        val userInfo = arrayOf(userString, userAvatarURL)
        val builder = ModmailEmbedBuilder(jda, guild, userInfo, null, message)

        ticketChannel.sendMessageEmbeds(
            builder.guildReceiveEmbed
        ).queue{
            event.channel.sendMessageEmbeds(
                builder.pmSendEmbed
            ).queue()
        }

        /* Send Attachments If There Are Any */
        if (!messages.get()[0].attachments.isEmpty()) {
            for (attachment in messages.get()[0].attachments) {
                ticketChannel.sendMessage(attachment.url).queue()
                event.channel.sendMessage(attachment.url).queue()
            }
        }
    }

    /**
     * Private function that handles closing a ticket
     * @param event SlashCommandInteractionEvent object
     * @param guild Guild object
     * @param user User object of whom initaited the closing of the ticket
     * @param reason Reason for why the ticket was closed
     */
    private fun closeTicket(event: SlashCommandInteractionEvent, guild: Guild, user: User, reason: String) {

        /* Get Guild and Ticket Info */
        val ticketLog = guild.getTextChannelById( Database.getModmailLog(guild.idLong)!! )
        val channel = event.channel.asTextChannel()
        val ticketAuthor = guild.getMemberById(channel.topic!!.toLong())?.user

        /* Log Ticket Closing */

        ticketLog?.sendMessageEmbeds(
            EmbedBuilder()
                .setTitle("Ticket Closed")
                .addField(MessageEmbed.Field("Reason", reason, true))
                .setAuthor("${ticketAuthor?.name}#${ticketAuthor?.discriminator} (${ticketAuthor?.idLong})", null, ticketAuthor?.avatarUrl)
                .setFooter("${user.name}#${user.discriminator}", user.avatarUrl)
                .setColor(Color.RED)
                .setTimestamp(Instant.now())
                .build()
        )?.queue()

        /* Delete Channel and Notify User */

        event.replyEmbeds(
            EmbedBuilder()
                .setTitle("Ticket Closed")
                .setDescription("Deleteing channel in 10 seconds...")
                .setColor(Color.RED)
                .build()
        ).queue()

        ticketAuthor?.openPrivateChannel()?.queue{
            it.sendMessageEmbeds(
                EmbedBuilder()
                    .setTitle("Ticket Closed")
                    .addField(MessageEmbed.Field("Reason", reason, true))
                    .setFooter("${guild.name} (${guild.idLong})", guild.iconUrl)
                    .setColor(Color.RED)
                    .setTimestamp(Instant.now())
                    .build()
            ).queue()
        }

        channel.delete().queueAfter(10, TimeUnit.SECONDS)

    }
}