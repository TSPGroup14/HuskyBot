package huskybot.commands.misc

import huskybot.Database
import huskybot.cmdFramework.Command
import huskybot.cmdFramework.CommandProperties
import huskybot.cmdFramework.Context

@CommandProperties(description = "View your level")
class Level : Command(ExecutionType.STANDARD) {
    override fun execute(ctx: Context) {
        val level = ctx.guild?.let { Database.getUserXP(it.idLong, ctx.member.idLong) }

        ctx.post("${ctx.member.nickname}, your level is: $level" )
    }

}