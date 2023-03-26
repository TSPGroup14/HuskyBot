package huskybot.commands.moderation

import huskybot.cmdFramework.*
import huskybot.modules.cmdHelpers.ModHelper
import huskybot.modules.cmdHelpers.Result
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.time.Duration

@CommandProperties(description = "Timeout a requested user")
@Options([
    Option("user", "User that you would like to issue a warning", OptionType.USER, true),
    Option("duration", "How long the timeout should last for formatted in '1d2h3m4s' or some other combination", OptionType.STRING, true),
    Option("reason", "Reason for warning the user", OptionType.STRING, false)
])
class Timeout : Command(ExecutionType.STANDARD) {
    override fun execute(context: Context) {

        val user = context.args.next("user", ArgumentResolver.USER)!!
        var reason = context.args.next("reason", ArgumentResolver.STRING) ?: "No reason given."     //Gets the reason from the reason option and if null uses a default response
        val member = context.guild?.getMemberById(user.idLong)     //Refers to the user's member-id in the guild
        val duration = parseDuration(context.args.next("duration", ArgumentResolver.STRING)!!)

        /* Null check */
        if (member == null) {
            context.post("❌ **Could not find user!** ❌")
            return
        }

        val result = ModHelper.tryTimeout(context, member, reason, duration!!).get()

        when (result) {
            Result.BOT_NO_PERMS -> context.post("❌ **I do not have permissions to issue a timeout!** ❌")
            Result.USER_NO_PERMS -> context.post("❌ **You do not have access to this command** ❌")
            Result.MEMBER_TOO_HIGH -> context.post("❌ **Cannot timeout member, <@${user.idLong}> role is above mine!** ❌")
            Result.SUCCESS -> context.post("**<@${user.idLong}> has been timed out for ${duration.toHours()} hour(s)!**")
            else -> context.post("❌ **An error has occurred** ❌")             //This is here to handle any extraneous enum cases.
        }
    }

    private fun parseDuration(args: String) : Duration? {

        if (args.uppercase().contains("D")) {
            return Duration.parse(String.format("P%s",args.uppercase()))
        }

        return Duration.parse(String.format("PT%s",args.uppercase()))
    }
}