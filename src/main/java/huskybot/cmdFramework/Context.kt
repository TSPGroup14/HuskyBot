package huskybot.cmdFramework

import huskybot.HuskyBot
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color

/**
 * Class that simplifies the command handling process
 * with helper functions that remove a lot of boilerplate
 */
class Context(val event: SlashCommandInteractionEvent, val args: Arguments) {

    val jda = event.jda
    val guild = event.guild
    val message = event.messageChannel
    val member = event.member!!
    val channel = event.channel
    val hook = event.hook

    val embedColor: Color
        get() = HuskyBot.color

    /**
     * Replies to an interaction with a simple embedded message.
     * @param title Embed title
     * @param description Embed description
     */
    fun embed(title: String, description: String) {
        embed {
            setTitle(title)
            setDescription(description)
        }
    }

    /**
     * Takes in an embed builder codeblock and sends a completed embedded message to the user.
     */
    fun embed(block: EmbedBuilder.() -> Unit) {
        val embed = EmbedBuilder()
            .setColor(embedColor)
            .apply(block)
            .build()

        event.replyEmbeds(embed)
            .queue()
    }

    /**
     * Reply to an interaction with a standard message
     */
    fun post(message: String) {
        event.reply(message)
            .queue()
    }

    /**
     * Reply to an interaction with an embedded message
     */
    fun replyWithEmbed(embed: MessageEmbed) {
        event.replyEmbeds(embed)
            .queue()
    }

    /**
     * Replies to an interaction with a simple embedded message after the initial interaction has been
     * replied to
     * @param title Embed title
     * @param description Embed description
     */
    fun hookedEmbed(title: String, description: String) {
        hookedEmbed {
            setTitle(title)
            setDescription(description)
        }
    }

    /**
     * Post an embedded message after the initial interaction has been replied to
     */
    fun hookedEmbed(block: EmbedBuilder.() -> Unit) {
        val embed = EmbedBuilder()
            .apply(block)
            .build()

        hook.sendMessageEmbeds(embed).queue()
    }

    /**
     * Lets Discord know that the command has been acknowledged and that the bot will take significant
     * time to respond
     */
    fun deferReply() {
        event.deferReply().queue()
    }
}