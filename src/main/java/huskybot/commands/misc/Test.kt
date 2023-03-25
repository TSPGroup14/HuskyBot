package huskybot.commands.misc

import huskybot.cmdFramework.Command
import huskybot.cmdFramework.CommandProperties
import huskybot.cmdFramework.Context
import huskybot.cmdFramework.Option
import net.dv8tion.jda.api.interactions.commands.OptionType
import warden.framework.SubCommand

@CommandProperties(description = "Testing command")
class Test : Command(ExecutionType.STANDARD) {
    override fun execute(context: Context) {
        this.subcommands[context.event.subcommandName]!!.invoke(context)
    }

    @Option("channel", "Channel that you would like the modlog to be in", OptionType.CHANNEL, false)
    @SubCommand("modlog", "set modlog channel", false)
    fun modlog(ctx: Context) {
        ctx.post("Bonk")
    }
}