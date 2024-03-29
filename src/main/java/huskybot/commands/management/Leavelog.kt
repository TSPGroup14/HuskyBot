package huskybot.commands.management

import huskybot.Database
import huskybot.cmdFramework.*
import net.dv8tion.jda.api.interactions.commands.OptionType

@CommandProperties(
    description = "Enable Leavelog and set what channel you would like to be the log",
    category = CommandCategory.MANAGEMENT
)
class Leavelog : Command(ExecutionType.STANDARD) {
    override fun execute(context: Context) {
        //Invoke subcommand
        this.subcommands[context.event.subcommandName]!!.invoke(context)
    }

    /**
     * Method that handles updating the leavelog channel in the database
     * @param ctx Context object
     */
    @Option("channel", "What channel you would like the log to be in", OptionType.CHANNEL, true)
    @SubCommand("set", "Enable Leavelog and set what channel you would like to be the log", false)
    fun set(ctx: Context) {

        val channel = ctx.args.next("channel", ArgumentResolver.CHANNEL)!!

        ctx.guild?.let { Database.setLeavelogChannel(it.idLong, channel.idLong) }     //Set the new leavelog channel in the database

        ctx.embed("✅ Leavelog Channel Set ✅", "**New channel:** <#${channel.idLong}>")
    }

    /**
     * Method that handles disabling leavelog and clears the current channel from the database
     * @param ctx Context object
     */
    @SubCommand("disable", "Disables leavelog", false)
    fun disable(ctx: Context) {

        ctx.guild?.let { Database.setLeavelogChannel(it.idLong, null)}

        ctx.post("✅ **Leavelog Successfully Disabled** ✅")
    }
}