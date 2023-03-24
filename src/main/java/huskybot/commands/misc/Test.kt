package huskybot.commands.misc

import huskybot.Database
import huskybot.cmdFramework.*
import net.dv8tion.jda.api.interactions.commands.OptionType
import warden.framework.SubCommand

@CommandProperties(description = "Test command for subcommands and database")
@Options([
    Option("option", "What menu you would like to enter, leave blank for standard menu", OptionType.STRING,false),
    Option("channel", "Channel that will store mod logs", OptionType.CHANNEL, false)
])
class Test : Command(ExecutionType.STANDARD) {
    override fun execute(context: Context) {
        val channelID = context.guild?.let { Database.getModlogChannel(it.idLong) } ?: 0
        val menu = context.args.next("option", ArgumentResolver.STRING) ?: ""

        when (menu) {
            "modlog" -> setModlogChannel(context)
            "" -> context.embed("Current Channel", "<#${channelID}>")
        }
    }

    @SubCommand(trigger = "modlog", description = "Set modlog channel")
    fun setModlogChannel(context: Context) {
        val channel = context.args.next("channel", ArgumentResolver.CHANNEL)!!

        context.guild?.let { Database.setModlogChannel(it.idLong, channel.idLong) }

        context.embed("Modlog Channel Set!", "<#${channel.idLong}>")
    }
}