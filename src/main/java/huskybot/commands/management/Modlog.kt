package huskybot.commands.management

import huskybot.Database
import huskybot.cmdFramework.*
import net.dv8tion.jda.api.interactions.commands.OptionType

@CommandProperties(
    description = "Enable modlog and set what channel you would like to be the log",
    category = CommandCategory.MANAGEMENT
)
@Options([
    Option("channel", "What channel you would like the log to be in, leave blank to disable modlog", OptionType.CHANNEL, false)
])
class Modlog : Command(ExecutionType.STANDARD) {
    override fun execute(context: Context) {
        //Invoke subcommand
        this.subcommands[context.event.subcommandName]!!.invoke(context)
    }

    /**
     * Method that handles updating the modlog channel in the database
     * @param ctx Context object
     */
    @Option("channel", "What channel you would like the log to be in", OptionType.CHANNEL, true)
    @SubCommand("set", "Enable modlog and set what channel you would like to be the log", false)
    fun set(ctx: Context) {

        val channel = ctx.args.next("channel", ArgumentResolver.CHANNEL)!!

        ctx.guild?.let { Database.setModlogChannel(it.idLong, channel.idLong) }     //Set the new modlog channel in the database

        ctx.embed("✅ Modlog Channel Set ✅", "**New channel:** <#${channel.idLong}>")
    }

    /**
     * Method that handles disabling modlog and clears the current channel from the database
     * @param ctx Context object
     */
    @SubCommand("disable", "Disables modlog", false)
    fun disable(ctx: Context) {

        ctx.guild?.let { Database.setModlogChannel(it.idLong, null)}

        ctx.post("✅ **Modlog Successfully Disabled** ✅")
    }
}