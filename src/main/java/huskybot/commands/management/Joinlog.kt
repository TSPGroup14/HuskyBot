package huskybot.commands.management

import huskybot.Database
import huskybot.cmdFramework.*
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.interactions.commands.OptionType

@CommandProperties(description = "Enable joinlog and set what channel you would like to be the log")
@Options([
    Option("channel", "What channel you would like the log to be in, leave blank to disable JoinLog", OptionType.CHANNEL, false)
])
class Joinlog : Command(ExecutionType.STANDARD) {
    override fun execute(context: Context) {
        val channel = context.args.next("channel", ArgumentResolver.CHANNEL)

        when (channel) {
            null -> disableJoinlog(context)               //If channel option is empty, disable the joinlog
            else -> updateJoinlog(context, channel)       //Update the channel that the modlog is in.
        }

        //TODO("Create subcommand group for joinlog to allow for better usability")
    }

    /**
     * Private method that handles updating the joinlog channel in the database
     * @param ctx Context object
     * @param channel Channel object
     */
    private fun updateJoinlog(ctx: Context, channel: GuildChannel) {
        ctx.guild?.let { Database.setJoinlogChannel(it.idLong, channel.idLong) }     //Set the new joinlog channel in the database

        ctx.embed("✅ Joinlog Channel Set ✅", "**New channel:** <#${channel.idLong}>")
    }

    /**
     * Private method that handles disabling joinlog and clears the current channel from the database
     * @param ctx Context object
     */
    private fun disableJoinlog(ctx: Context) {

        ctx.guild?.let { Database.setJoinlogChannel(it.idLong, null)}

        ctx.post("✅ **Joinlog Successfully Disabled** ✅")
    }
}