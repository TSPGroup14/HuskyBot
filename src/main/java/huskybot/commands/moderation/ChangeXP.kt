package huskybot.commands.moderation

import huskybot.Database
import huskybot.cmdFramework.*
import net.dv8tion.jda.api.interactions.commands.OptionType
import warden.framework.SubCommand

@CommandProperties(description = "Manually change a user's XP count")
@Options([
    Option(name = "user", description = "User whose XP you would like to change", type = OptionType.USER, required = true),
    Option(name = "amount", description = "Amount of XP being granted", type = OptionType.INTEGER, required = true)
])
class UserXP : Command(ExecutionType.STANDARD){
    override fun execute(ctx: Context) {
        this.subcommands[ctx.event.subcommandName]!!.invoke(ctx)
    }

    @SubCommand("grant", "Change a user's XP", false)
    fun grant (ctx: Context) {
        val user = ctx.args.next("user", ArgumentResolver.USER)!!
        val amount = ctx.args.next("amount", ArgumentResolver.INTEGER)!!
        val member = ctx.guild?.getMemberById(user.idLong)

        /* Null check */
        if (member == null) {
            ctx.post("❌ **Could not find user!** ❌")
            return
        }

        Database.updateUserXP(ctx.guild.idLong, user.idLong, amount)
    }
}