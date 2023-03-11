package huskybot.commands.moderation

import huskybot.cmdFramework.*
import huskybot.modules.cmdHelpers.ModHelper.tryUnban
import huskybot.modules.cmdHelpers.Result
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.commands.OptionType

@CommandProperties(description = "Unban a requested user")
@Options([
    Option(name = "user", description = "User that you would like to unban", type = OptionType.USER, required = true),
    Option(name = "reason", description = "Reason for unbanning the user", type = OptionType.STRING, required = false)
])
class Unban : Command(ExecutionType.STANDARD) {
    override fun execute(context: Context) {
        val userId = context.args.gatherNext("user").toLong()
        var user = context.jda.getUserById(userId)

        if (user == null) {

            /* Retrieve ban list */
            context.guild?.retrieveBanList()?.queue { list ->
                for (ban in list) {
                    if (ban.user.idLong.equals(userId)) {
                       unbanUser(context, userId, ban.user)
                    }
                }
                if (user == null) {
                    context.post("❌ **Could not find user!** ❌")
                }
            }
        }
    }

    private fun unbanUser(context: Context, uID: Long, user: User) {

        var reason = "No reason given."      //Default value }

        /* Change default value if option is used */
        if (context.args.hasNext("reason")) {
            reason = context.args.gatherNext("reason")
        }

        /* Send action call to ModHelper to execute the unban */
        val result = tryUnban(context, user, reason).get()        //Result of the unban attempt

        when (result) {
            Result.BOT_NO_PERMS -> context.post("❌ **I do not have permissions to unban!** ❌")
            Result.USER_NO_PERMS -> context.post("❌ **You do not have access to this command** ❌")
            Result.MEMBER_NOT_BANNED -> context.post("❌ **Cannot unban member, <@${uID}> is not banned!** ❌")
            Result.SUCCESS -> context.post("**<@${uID}> has been unbanned!**")
            else -> context.post("❌ **An error has occured** ❌")             //This is here to handle any extraneous enum cases.
        }
    }
}