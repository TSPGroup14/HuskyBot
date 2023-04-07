package huskybot.commands.misc

import huskybot.Database
import huskybot.cmdFramework.Command
import huskybot.cmdFramework.CommandProperties
import huskybot.cmdFramework.Context
import net.dv8tion.jda.api.entities.MessageEmbed

@CommandProperties(description = "View your level")
class Level : Command(ExecutionType.STANDARD) {
    override fun execute(ctx: Context) {
        val xp = ctx.guild?.let { Database.getUserXP(it.idLong, ctx.member.idLong) }

        ctx.embed{
            setTitle("Your Level")
            addField(MessageEmbed.Field("XP:", "$xp", true))
            addField(MessageEmbed.Field("Level:", "(TBA)", true))
            setThumbnail(ctx.member.user.avatarUrl)
        }
    }

}