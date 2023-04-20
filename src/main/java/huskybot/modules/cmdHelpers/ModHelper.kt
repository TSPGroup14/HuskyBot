package huskybot.modules.cmdHelpers

import huskybot.Database
import huskybot.cmdFramework.Context
import huskybot.modules.logging.ModlogManager.logBan
import huskybot.modules.logging.ModlogManager.logKick
import huskybot.modules.logging.ModlogManager.logPardon
import huskybot.modules.logging.ModlogManager.logTimeout
import huskybot.modules.logging.ModlogManager.logUnban
import huskybot.modules.logging.ModlogManager.logWarn
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import java.awt.Color
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * Object class that allows the reuse of code across the bot,
 * as well as ensures that only the correct user has access to
 * moderator actions.
 */
object ModHelper {

    /**
     * Ban helper that returns the outcome of a ban attempt.
     * @param ctx Context object
     * @param member Member that is to be banned
     * @param reason Reason for ban, will be logged if modlog is enabled
     * @param duration How long the ban will last in days
     * @param delDays How many days back worth of the user's messages that will be deleted
     * @return Result of the ban
     */
    fun tryBan(ctx: Context, member: Member,
               reason: String, duration: Int,
               delDays: Int) : CompletableFuture<Result> {

        val self = ctx.guild?.selfMember
        val moderator = ctx.member
        val future = CompletableFuture.supplyAsync {

            /* Pre-Ban Checks */

            if(!self?.hasPermission(Permission.BAN_MEMBERS)!!) {
                return@supplyAsync Result.BOT_NO_PERMS       //Bot lacks ban permission
            }

            if(!moderator.hasPermission(Permission.BAN_MEMBERS)) {
                return@supplyAsync Result.USER_NO_PERMS      //Moderator lacks ban permission
            }

            if(!self.canInteract(member)) {
                return@supplyAsync Result.MEMBER_TOO_HIGH    //Member is above the user or bot
            }

            /* Attempt ban */

            try {
                ctx.guild.ban(member, delDays, TimeUnit.DAYS)
                    .reason(reason)
                    .queue()
            } catch (e: Exception) {
                throw e
            }

            /* Log the ban in the modlog */
            logBan(ctx, ctx.member.user, member.user, reason)

            return@supplyAsync Result.SUCCESS
        }

        return future
    }

    /**
     * Helper method for unbanning a user, which returns the result of the unban attempt.
     * @param ctx Context object
     * @param user User that is to be unbanned
     * @param reason Reason for unban, will be logged if modlog is enabled
     * @return Result of unban attempt
     */
    fun tryUnban(ctx: Context, user: User, reason: String) : CompletableFuture<Result> {

        val self = ctx.guild?.selfMember
        val moderator = ctx.member
        var banned = false
        val future = CompletableFuture.supplyAsync {

            /* Pre-Run Checks */

            if(!self?.hasPermission(Permission.BAN_MEMBERS)!!) {
                return@supplyAsync Result.BOT_NO_PERMS       //Bot lacks ban permission
            }

            if(!moderator.hasPermission(Permission.BAN_MEMBERS)) {
                return@supplyAsync Result.USER_NO_PERMS      //Moderator lacks ban permission
            }

            val banList = ctx.guild.retrieveBanList().submit()                  //List of bans from the guild

            for (ban in banList.get()) {
                banned = (ban.user == user)                                     //Change banned to true if the user is in the ban list
            }

            if (!banned) return@supplyAsync Result.MEMBER_NOT_BANNED    //User is not banned

            /* Unban the user */

            ctx.guild.unban(user)
                .reason(reason)
                .queue()

            /* Log the action in the modlog */
            logUnban(ctx, ctx.member.user, user, reason)

            return@supplyAsync Result.SUCCESS
        }

        return future
    }

    /**
     * Helper method for kicking a user, which returns the result of the kick.
     * @param ctx Context object
     * @param member Member that is to be kicked
     * @param reason Reason for the kick, will be logged if modmail is enabled
     * @return Result of the kick attempt
     */
    fun tryKick(ctx: Context, member: Member, reason: String) : CompletableFuture<Result> {

        val self = ctx.guild?.selfMember
        val moderator = ctx.member
        val future = CompletableFuture.supplyAsync {

            /* Pre-Run Checks */

            if(!self?.hasPermission(Permission.KICK_MEMBERS)!!) {
                return@supplyAsync Result.BOT_NO_PERMS       //Bot lacks kick permission
            }

            if(!moderator.hasPermission(Permission.KICK_MEMBERS)) {
                return@supplyAsync Result.USER_NO_PERMS      //Moderator lacks kick permission
            }

            if(!self.canInteract(member)) {
                return@supplyAsync Result.MEMBER_TOO_HIGH    //Member is above the user or bot
            }

            /* Attempt kick */

            try {
                ctx.guild.kick(member)
                    .reason(reason)
                    .queue()
            } catch (e: Exception) {
                throw e
            }

            /* Log the action in the modlog */
            logKick(ctx, ctx.member.user, member.user, reason)

            return@supplyAsync Result.SUCCESS
        }

        return future
    }

    /**
     * Helper method for warning a user, which returns the result of the warning.
     * @param ctx Context object
     * @param member Member that is to be warned
     * @param reason Reason for the warning, will be logged if modmail is enabled
     * @return Result of the warning attempt
     */
    fun tryWarn(ctx: Context, member: Member, reason: String) : CompletableFuture<Result> {

        val self = ctx.guild?.selfMember
        val moderator = ctx.member
        val future = CompletableFuture.supplyAsync {

            /* Pre-Run Checks */

            if(!self?.hasPermission(Permission.KICK_MEMBERS)!!) { //permission to kick and permission to warn are one in the same
                return@supplyAsync Result.BOT_NO_PERMS       //Bot lacks kick permission
            }

            if(!moderator.hasPermission(Permission.KICK_MEMBERS)) {
                return@supplyAsync Result.USER_NO_PERMS      //Moderator lacks kick permission
            }

            if(!self.canInteract(member)) {
                return@supplyAsync Result.MEMBER_TOO_HIGH    //Member is above the user or bot
            }

            /* Warn the User */

            if (!member.user.isBot) {
                ctx.jda.openPrivateChannelById(member.user.idLong)
                    .queue{channel ->
                        channel.sendMessageEmbeds(
                            EmbedBuilder()
                                .setTitle("You have been warned for")
                                .setDescription(reason)
                                .setColor(Color.red)
                                .build()
                        )
                    }
            }

            /* Increment and get the Warn Count */
            Database.updateWarnCount(ctx.guild.idLong, member.idLong, true)
            val warnCount = Database.getWarnCount(ctx.guild.idLong, member.idLong)

            /* Log the action in the modlog */
            logWarn(ctx, ctx.member.user, member.user, reason, warnCount)

            return@supplyAsync Result.SUCCESS
        }

        return future
    }

    /**
     * Helper method for pardoning a user, which returns the result of the pardon.
     * @param ctx Context object
     * @param member Member that is to be pardoned
     * @param reason Reason for the pardon, will be logged if modmail is enabled
     * @return Result of the pardon attempt
     */
    fun tryPardon(ctx: Context, member: Member, reason: String) : CompletableFuture<Result> {

        val self = ctx.guild?.selfMember
        val moderator = ctx.member
        val future = CompletableFuture.supplyAsync {

            /* Pre-Run Checks */

            if(!self?.hasPermission(Permission.KICK_MEMBERS)!!) { //permission to kick and permission to warn are one in the same
                return@supplyAsync Result.BOT_NO_PERMS       //Bot lacks kick permission
            }

            if(!moderator.hasPermission(Permission.KICK_MEMBERS)) {
                return@supplyAsync Result.USER_NO_PERMS      //Moderator lacks kick permission
            }

            if(!self.canInteract(member)) {
                return@supplyAsync Result.MEMBER_TOO_HIGH    //Member is above the user or bot
            }

            /* Pardon the User */

            if (!member.user.isBot) {
                ctx.jda.openPrivateChannelById(member.user.idLong)
                    .queue{channel ->
                        channel.sendMessageEmbeds(
                            EmbedBuilder()
                                .setTitle("You have been pardoned for")
                                .setDescription(reason)
                                .setColor(Color.red)
                                .build()
                        )
                    }
            }

            /* Reduce and get the Warn Count */
            Database.updateWarnCount(ctx.guild.idLong, member.idLong, false)
            val warnCount = Database.getWarnCount(ctx.guild.idLong, member.idLong)

            /* Log the action in the modlog */
            logPardon(ctx, ctx.member.user, member.user, reason, warnCount)

            return@supplyAsync Result.SUCCESS
        }

        return future
    }

    /**
     * Helper method for timing out a user, which returns the result of the timeout.
     * @param ctx Context object
     * @param member Member that is to be timed out
     * @param reason Reason for the timeout, will be logged if modmail is enabled
     * @param duration How long the timeout will last, can be null if not used
     * @return Result of the timeout attempt
     */
     fun tryTimeout(ctx: Context, member: Member, reason: String, duration: Duration) : CompletableFuture<Result> {

        val self = ctx.guild?.selfMember
        val moderator = ctx.member
        val future = CompletableFuture.supplyAsync {

            /* Pre-Run Checks */

            if(!self?.hasPermission(Permission.MODERATE_MEMBERS)!!) { //permission to kick and permission to warn are one in the same
                return@supplyAsync Result.BOT_NO_PERMS       //Bot lacks kick permission
            }

            if(!moderator.hasPermission(Permission.MODERATE_MEMBERS)) {
                return@supplyAsync Result.USER_NO_PERMS      //Moderator lacks kick permission
            }

            if(!self.canInteract(member) || member.hasPermission(Permission.ADMINISTRATOR)) {
                return@supplyAsync Result.MEMBER_TOO_HIGH    //Member is above the user or bot
            }

            if(duration.toDays() > 28) {
                return@supplyAsync Result.ERROR              //Timeout is greater than 28 days
            }

            /* Timeout the User */
            ctx.guild.timeoutFor(member, duration)
                .reason(reason)
                .submit()

            /* Log the action in the modlog */
            logTimeout(ctx, moderator.user, member.user, reason, duration)

            return@supplyAsync Result.SUCCESS
        }

        return future
     }
}

/**
 * Enum class for categorizing result types
 */
enum class Result {
    SUCCESS,                //Success
    BOT_NO_PERMS,           //Bot lacks permission
    USER_NO_PERMS,          //User lacks permission
    MEMBER_TOO_HIGH,        //Member is above the user or bot
    MEMBER_NOT_BANNED,      //Member is/was not banned
    ERROR                   //Error has occured
}