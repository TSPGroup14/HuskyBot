package huskybot.commands.management

import huskybot.Database
import huskybot.cmdFramework.*
import net.dv8tion.jda.api.interactions.commands.OptionType

@CommandProperties(
    description = "Enable joinlog and set what channel you would like to be the log",
    category = CommandCategory.MANAGEMENT
)
class Joinlog : Command(ExecutionType.STANDARD) {
    override fun execute(context: Context) {
        //Invoke subcommand
        this.subcommands[context.event.subcommandName]!!.invoke(context)
    }

    /**
     * Method that handles updating the joinlog channel in the database
     * @param ctx Context object
     */
    @Option("channel", "What channel you would like the log to be in", OptionType.CHANNEL, true)
    @SubCommand("set","Enable joinlog and set what channel you would like to be the log", false)
    fun set(ctx: Context) {

        val channel = ctx.args.next("channel", ArgumentResolver.CHANNEL)!!

        ctx.guild?.let { Database.setJoinlogChannel(it.idLong, channel.idLong) }     //Set the new joinlog channel in the database

        ctx.embed("✅ Joinlog Channel Set ✅", "**New channel:** <#${channel.idLong}>")
    }

    /**
     * Method that handles disabling joinlog and clears the current channel from the database
     * @param ctx Context object
     */
    @SubCommand("disable", "Disables joinlog", false)
    fun disable(ctx: Context) {

        ctx.guild?.let { Database.setJoinlogChannel(it.idLong, null)}

        ctx.post("✅ **Joinlog Successfully Disabled** ✅")
    }
}