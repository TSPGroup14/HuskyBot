package huskybot.modules.logging

import huskybot.Database
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import java.awt.Color
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object GuildLogManager {

    /**
     * Function that logs when a user joins the guild if the joinlog is enabled in that guild.
     * @param event GuildMemberJoinEvent object
     */
    fun logUserJoin(event: GuildMemberJoinEvent) {

        /* Get log channel and user */
        val channel = Database.getJoinlogChannel(event.guild.idLong) ?: 0
        val user = event.user

        /* Check if log channel exists */
        if (channel.equals(0)) {
            return
        }

        /* Build embedded message that will contain the log information */
        val embed = EmbedBuilder()
            .setTitle("User Joined")
            .addField(
                MessageEmbed.Field("Account Created", user.timeCreated.format(
                    DateTimeFormatter.ofLocalizedDate(
                    FormatStyle.LONG)), true)
            )
            .setAuthor("${user.name} (${user.idLong})",null, user.avatarUrl)
            .setColor(Color.RED)
            .setTimestamp(Instant.now())
            .build()

        /* Send log to channel */
        event.guild.getTextChannelById(channel)?.sendMessageEmbeds(embed)
            ?.queue()
    }
    /**
     * Function that logs when a user leavs the guild if the leavelog is enabled in that guild.
     * @param event GuildMemberRemoveEvent object
     */
    fun logUserLeave(event: GuildMemberRemoveEvent) {

        /* Get log channel and user */
        val channel = Database.getJoinlogChannel(event.guild.idLong) ?: 0
        val user = event.user

        /* Check if log channel exists */
        if (channel.equals(0)) {
            return
        }

        /* Build embedded message that will contain the log information */
        val embed = EmbedBuilder()
            .setTitle("User Left")
            .setAuthor("${user.name} (${user.idLong})",null, user.avatarUrl)
            .setColor(Color.RED)
            .setTimestamp(Instant.now())
            .build()

        /* Send log to channel */
        event.guild.getTextChannelById(channel)?.sendMessageEmbeds(embed)
            ?.queue()
    }
}