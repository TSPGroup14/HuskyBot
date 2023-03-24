package huskybot.commands.moderation

import huskybot.cmdFramework.*
import huskybot.modules.cmdHelpers.ModHelper.tryPardon
import huskybot.modules.cmdHelpers.Result
import net.dv8tion.jda.api.interactions.commands.OptionType

@CommandProperties(description = "Issue a pardon to a given user")
@Options([
    Option(name = "user", description = "User that you would like to issue a pardon", type = OptionType.USER, required = true),
    Option(name = "reason", description = "Reason for pardoning the user", type = OptionType.STRING, required = false)
])
class Pardon : Command(ExecutionType.STANDARD) {
    override fun execute(context: Context) {
        val user = context.args.next("user", ArgumentResolver.USER)!!
        var reason = context.args.next("reason", ArgumentResolver.STRING) ?: "No reason given."     //Gets the reason from the reason option and if null uses a default response
        val member = context.guild?.getMemberById(user.idLong)     //Refers to the user's member-id in the guild

        /* Null check */
        if (member == null) {
            context.post("❌ **Could not find user!** ❌")
            return
        }

        val result = tryPardon(context, member, reason).get()

        when (result) {
            Result.BOT_NO_PERMS -> context.post("❌ **I do not have permissions to issue a pardon!** ❌")
            Result.USER_NO_PERMS -> context.post("❌ **You do not have access to this command** ❌")
            Result.MEMBER_TOO_HIGH -> context.post("❌ **Cannot pardon the member, <@${user.idLong}> role is above mine!** ❌")
            Result.SUCCESS -> context.post("**<@${user.idLong}> has been pardoned!**")
            else -> context.post("❌ **An error has occured** ❌")             //This is here to handle any extraneous enum cases.
        }
    }
}