package huskybot.commands.management

import huskybot.Database
import huskybot.cmdFramework.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.interactions.commands.OptionType

@CommandProperties(description = "Enable modlog and set what channel you would like to be the log")
@Options([
    Option("channel", "What channel you would like the log to be in, leave blank to disable modlog", OptionType.CHANNEL, false)
])
class Modlog : Command(ExecutionType.STANDARD) {
    override fun execute(context: Context) {
        val channel = context.args.next("channel", ArgumentResolver.CHANNEL)

        when (channel) {
            null -> disableModlog(context)               //If channel option is empty, disable the modlog
            else -> updateModlog(context, channel)       //Update the channel that the modlog is in.
        }

        //TODO("Create subcommand group for modlog to allow for better usability")
    }

    /**
     * Private method that handles updating the modlog channel in the database
     * @param ctx Context object
     * @param channel Channel object
     */
    private fun updateModlog(ctx: Context, channel: GuildChannel) {
        ctx.guild?.let { Database.setModlogChannel(it.idLong, channel.idLong) }     //Set the new modlog channel in the database

        ctx.embed("✅ Modlog Channel Set ✅", "**New channel:** <#${channel.idLong}>")
    }

    /**
     * Private method that handles disabling modlog and clears the current channel from the database
     * @param ctx Context object
     */
    private fun disableModlog(ctx: Context) {

        ctx.guild?.let { Database.setModlogChannel(it.idLong, null)}

        ctx.post("✅ **Modlog Successfully Disabled** ✅")
    }
}