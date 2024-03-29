package huskybot.commands.moderation

import huskybot.Database
import huskybot.HuskyBot
import huskybot.cmdFramework.*
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.commands.OptionType

@CommandProperties(description = "Manually change a user's XP count")
class User : Command(ExecutionType.STANDARD){
    override fun execute(ctx: Context) {
        this.subcommands[ctx.event.subcommandName]!!.invoke(ctx)
    }

    @Options([
        Option(name = "user", description = "User whose XP you would like to change", type = OptionType.USER, required = true),
        Option(name = "amount", description = "Amount of XP being granted", type = OptionType.INTEGER, required = true)
    ])
    @SubCommand("grantxp", "Change a user's XP", false)
    fun grantXP (ctx: Context) {
        val user = ctx.args.next("user", ArgumentResolver.USER)
        val amount = ctx.args.next("amount", ArgumentResolver.LONG) ?: 0

        /* Null check */
        if (user == null) {
            ctx.post("❌ **Could not find user!** ❌")
            return
        }

        ctx.guild?.let { Database.updateUserXP(it.idLong, user.idLong, amount) }

        ctx.embed{
            setTitle("Level Updated")
            setThumbnail(user.avatarUrl)
            addField(MessageEmbed.Field("XP Given:", amount.toString(), true))
        }
    }
}