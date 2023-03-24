package huskybot.modules.logging

import huskybot.Database
import huskybot.cmdFramework.Context
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import java.awt.Color
import java.time.Instant

object ModlogManager {

    /**
     * Interface function for logging a ban
     * @param ctx Context object
     * @param moderator User who issued a moderation command
     * @param user User who the moderation command is being acted onto
     * @param reason Reason for why the moderation command was issued
     */
    fun logBan(ctx: Context, moderator: User, user: User, reason: String) {
        logAction(
            ctx,
            moderator,
            user,
            reason,
            "Ban",
            ModlogColor.BAN.color,
            null
        )
    }

    /**
     * Interface function for logging an unban
     * @param ctx Context object
     * @param moderator User who issued a moderation command
     * @param user User who the moderation command is being acted onto
     * @param reason Reason for why the moderation command was issued
     */
    fun logUnban(ctx: Context, moderator: User, user: User, reason: String) {
        logAction(
            ctx,
            moderator,
            user,
            reason,
            "Unban",
            ModlogColor.UNBAN.color,
            null
        )
    }

    /**
     * Interface function for logging a kick
     * @param ctx Context object
     * @param moderator User who issued a moderation command
     * @param user User who the moderation command is being acted onto
     * @param reason Reason for why the moderation command was issued
     */
    fun logKick(ctx: Context, moderator: User, user: User, reason: String) {
        logAction(
            ctx,
            moderator,
            user,
            reason,
            "Kick",
            ModlogColor.KICK.color,
            null
        )
    }

    /**
     * Interface function for logging a warn
     * @param ctx Context object
     * @param moderator User who issued a moderation command
     * @param user User who the moderation command is being acted onto
     * @param reason Reason for why the moderation command was issued
     * @param warnCount Current warning count of the user
     */
    fun logWarn(ctx: Context, moderator: User, user: User, reason: String, warnCount: Int) {
        logAction(
            ctx,
            moderator,
            user,
            reason,
            "Warn",
            ModlogColor.WARN.color,
            arrayOf(MessageEmbed.Field("Warnings", "${warnCount}", true))
        )
    }

    /**
     * Interface method for logging a pardon
     * @param ctx Context object
     * @param moderator User who issued a moderation command
     * @param user User who the moderation command is being acted onto
     * @param reason Reason for why the moderation command was issued
     * @param warnCount Current warning count of the user
     */
    fun logPardon(ctx: Context, moderator: User, user: User, reason: String, warnCount: Int) {
        logAction(
            ctx,
            moderator,
            user,
            reason,
            "Pardon",
            ModlogColor.PARDON.color,
            arrayOf(MessageEmbed.Field("Warnings", "${warnCount}", true))
        )
    }

    /**
     * Private function that builds and submits a log to the modlog channel if the modlog is enabled in the guild
     * @param ctx Context object
     * @param moderator User who issued a moderation command
     * @param user User who the moderation command is being acted onto
     * @param reason Reason for why the moderation command was issued
     * @param type Type of moderation command used
     * @param color Color that the log embed will use
     * @param fields Any extra fields that the log needs to display, can be null if not needed.
     */
    private fun logAction(ctx: Context, moderator: User, user: User, reason: String,
                          type: String, color: Color, fields: Array<MessageEmbed.Field>?) {
        //Get modLog ID
        val modLogID = ctx.guild?.let { Database.getModlogChannel(it.idLong) } ?: 0

        /* Check if modlog is enabled in the guild */
        if (modLogID.equals(0)) {
            return              //Returns only if modlog is not enabled (i.e. the log id comes back as 0)
        }

        val caseCount = ctx.guild?.let { Database.getCaseCount(it.idLong) }     //Current case count in the modlogs
        if (caseCount === null) {
            return
        }

        /* Update modlog case count */
        ctx.guild.let { Database.updateCaseCount(it.idLong) }

        /* Build the embeded message that will contain the log info */
        val embed = EmbedBuilder()
            .setTitle("Case: $caseCount")
            .setColor(color)
            .setTimestamp(Instant.now())
            .setAuthor("${user.name}#${user.discriminator} (${user.idLong})", null, user.avatarUrl)
            .setFooter(
                "${moderator.name}#${moderator.discriminator} (${moderator.idLong})",
                moderator.avatarUrl)
            .addField(MessageEmbed.Field("Type", type, true))
            .addField(MessageEmbed.Field("Reason", reason, true))

        /* Add extra fields if needed */
        if (fields != null) {
            for (field in fields) {
                embed.addField(field)
            }
        }

        ctx.hook.interaction.guild?.getTextChannelById(modLogID)?.sendMessageEmbeds(embed.build())?.queue()     //Send log to modlog channel
    }
}