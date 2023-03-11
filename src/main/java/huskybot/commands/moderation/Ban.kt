package huskybot.commands.moderation

import huskybot.cmdFramework.*
import huskybot.modules.cmdHelpers.*
import huskybot.modules.cmdHelpers.ModHelper.tryBan
import net.dv8tion.jda.api.interactions.commands.OptionType

@CommandProperties(description = "Ban a requested user")
@Options([
        Option(name = "user", description = "User that you would like to ban", type = OptionType.USER, required = true),
        Option(name = "reason", description = "Reason for banning the user", type = OptionType.STRING, required = false),
        Option(name = "duration", description = "How long the ban will last for in hours", type = OptionType.INTEGER, required = false),
        Option(name = "days", description = "How many days worth of messages from the user to remove", OptionType.INTEGER, required = false)
    ])
class Ban : Command(ExecutionType.STANDARD) {
    override fun execute(context: Context) {
        val user = context.args.gatherNext("user")
        var reason = "No reason given."                     //Default value
        var duration = 0                                   //Default value
        var days = 0                                       //Default value
        val member = context.guild?.getMemberById(user)

        /* Null check */
        if (member == null) {
            context.post("❌ **Could not find user!** ❌")
            return
        }

        /* Change default values if option is used */
        if (context.args.hasNext("reason")) {
            reason = context.args.gatherNext("reason")
        }
        if (context.args.hasNext("duration")) {
            duration = context.args.gatherNext("duration").toInt()
        }
        if (context.args.hasNext("days")) {
            days = context.args.gatherNext("days").toInt()
        }

        val result = tryBan(context, member, reason, duration.toInt(), days.toInt()).get()

        when (result) {
            Result.BOT_NO_PERMS -> context.post("❌ **I do not have permissions to ban!** ❌")
            Result.USER_NO_PERMS -> context.post("❌ **You do not have access to this command** ❌")
            Result.MEMBER_TOO_HIGH -> context.post("❌ **Cannot ban member, <@${user}> role is above mine!** ❌")
            Result.SUCCESS -> context.post("**<@${user}> has been banned!**")
            else -> context.post("❌ **An error has occured** ❌")             //This is here to handle any extraneous enum cases.
        }
    }
}