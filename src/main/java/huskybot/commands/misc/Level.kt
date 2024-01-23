package huskybot.commands.misc

import huskybot.Database
import huskybot.cmdFramework.*
import huskybot.modules.leveling.LevelManager.calcLevel
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.commands.OptionType

@CommandProperties(description = "View your level")
@Option(name = "user", description = "User whose level you would like to view", type = OptionType.USER, required = false)
class Level : Command(ExecutionType.STANDARD) {
    override fun execute(ctx: Context) {
        val user = ctx.args.next("user", ArgumentResolver.USER) ?: ctx.member.user

        val xp = ctx.guild?.let { Database.getUserXP(it.idLong, user.idLong) } ?: 0
        var lvl = ctx.guild?.let { Database.getUserLevel(it.idLong, user.idLong) } ?: 0
        ctx.guild?.let { Database.updateUserLevel(it.idLong, user.idLong, lvl) }
        lvl = calcLevel(xp, lvl)

        /* Update Level */
        ctx.guild?.idLong?.let { Database.updateUserLevel(it, user.idLong, lvl) }

        ctx.embed{
            setTitle("Your Level")
            addField(MessageEmbed.Field("XP:", "$xp", true))
            addField(MessageEmbed.Field("Level:", "$lvl", true))
            setThumbnail(user.avatarUrl)
        }
    }
}