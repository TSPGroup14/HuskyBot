package huskybot.commands.moderation

import huskybot.cmdFramework.*
import huskybot.modules.cmdHelpers.*
import huskybot.modules.cmdHelpers.ModHelper.tryBan
import net.dv8tion.jda.api.interactions.commands.OptionType

@CommandProperties(description = "Ban a requested user")
@Options([
        Option(name = "user", description = "User that you would like to ban", type = OptionType.USER, required = true),
        Option(name = "reason", description = "Reason for banning the user", type = OptionType.STRING, required = false),
        Option(name = "duration", description = "How long the ban will last for in days", type = OptionType.INTEGER, required = false),
        Option(name = "days", description = "How many days worth of messages from the user to remove", OptionType.INTEGER, required = false)
    ])
class Ban : Command(ExecutionType.STANDARD) {
    override fun execute(context: Context) {
        val user = context.args.next("user", ArgumentResolver.USER)!!
        val reason = context.args.next("reason", ArgumentResolver.STRING) ?: "No reason given." //Gets the reason from the reason option and if null uses a default response
        val duration = context.args.next("duration", ArgumentResolver.INTEGER) ?: 0             //Gets the count from the "duration" option and if null uses a default value of 0
        val days = context.args.next("days", ArgumentResolver.INTEGER) ?: 0                     //Gets the count from the "days" option and if null uses a default value of 0
        val member = context.guild?.getMemberById(user.idLong)

        /* Null check */
        if (member == null) {
            context.post("❌ **Could not find user!** ❌")
            return
        }

        /* Send action call to ModHelper to execute the ban */
        val result = tryBan(context, member, reason, duration, days).get()      //Result of the ban attempt

        when (result) {
            Result.BOT_NO_PERMS -> context.post("❌ **I do not have permissions to ban!** ❌")
            Result.USER_NO_PERMS -> context.post("❌ **You do not have access to this command** ❌")
            Result.MEMBER_TOO_HIGH -> context.post("❌ **Cannot ban member, <@${user.idLong}> role is above mine!** ❌")
            Result.SUCCESS -> context.post("**<@${user.idLong}> has been banned!**")
            else -> context.post("❌ **An error has occured** ❌")             //This is here to handle any extraneous enum cases.
        }
    }
}