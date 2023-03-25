package huskybot.commands.management

import huskybot.Database
import huskybot.cmdFramework.*
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.interactions.commands.OptionType

@CommandProperties(description = "Enable Leavelog and set what channel you would like to be the log")
@Options([
    Option("channel", "What channel you would like the log to be in, leave blank to disable LeaveLog", OptionType.CHANNEL, false)
])
class Leavelog : Command(ExecutionType.STANDARD) {
    override fun execute(context: Context) {
        val channel = context.args.next("channel", ArgumentResolver.CHANNEL)

        when (channel) {
            null -> disableLeavelog(context)               //If channel option is empty, disable the leavelog
            else -> updateLeavelog(context, channel)       //Update the channel that the modlog is in.
        }

        //TODO("Create subcommand group for leavelog to allow for better usability")
    }

    /**
     * Private method that handles updating the leavelog channel in the database
     * @param ctx Context object
     * @param channel Channel object
     */
    private fun updateLeavelog(ctx: Context, channel: GuildChannel) {
        ctx.guild?.let { Database.setLeavelogChannel(it.idLong, channel.idLong) }     //Set the new leavelog channel in the database

        ctx.embed("✅ Leavelog Channel Set ✅", "**New channel:** <#${channel.idLong}>")
    }

    /**
     * Private method that handles disabling leavelog and clears the current channel from the database
     * @param ctx Context object
     */
    private fun disableLeavelog(ctx: Context) {

        ctx.guild?.let { Database.setLeavelogChannel(it.idLong, null)}

        ctx.post("✅ **Leavelog Successfully Disabled** ✅")
    }
}