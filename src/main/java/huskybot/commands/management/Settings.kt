package huskybot.commands.management

import huskybot.Database
import huskybot.cmdFramework.Command
import huskybot.cmdFramework.CommandProperties
import huskybot.cmdFramework.Context
import huskybot.utils.addFields
import net.dv8tion.jda.api.entities.MessageEmbed
import warden.framework.SubCommand
import java.awt.Color

@CommandProperties(description = "View and set server specific settings")
class Settings : Command(ExecutionType.STANDARD) {
    override fun execute(context: Context) {
        //Invoke subcommand
        this.subcommands[context.event.subcommandName]!!.invoke(context)
    }

    /**
     * Subcommand method that displays the bot's guild-specific settings
     * @param ctx Context object
     */
    @SubCommand("view", "View HuskyBot's settings", false)
    fun view(ctx: Context) {

        /* Get displayable settings */
        val adminRole = ctx.guild?.idLong?.let { Database.getAdminRole(it) }
        val adminRoleFormatted = if (adminRole != null) "<&${adminRole}>" else "Default: (Admin)"       //Formatted to a Mention

        val modRole = ctx.guild?.idLong?.let { Database.getModRole(it) }
        val modRoleFormatted = if (modRole != null) "<&${modRole}>" else "Default: (Moderator)"         //Formatted to a Mention

        val joinlogChannel = ctx.guild?.idLong?.let { Database.getJoinlogChannel(it) }
        val joinlogFormatted = if (joinlogChannel != null) "<#${joinlogChannel}>" else "Disabled"

        val leavelogChannel = ctx.guild?.idLong?.let { Database.getLeavelogChannel(it) }
        val leavelogFormatted = if (leavelogChannel != null) "<#${leavelogChannel}>" else "Disabled"

        val modlogChannel = ctx.guild?.idLong?.let { Database.getModlogChannel(it) }
        val modlogFormatted = if (modlogChannel != null) "<#${modlogChannel}>" else "Disabled"

        /* Create array of message embed fields */
        val fields = arrayOf(
            MessageEmbed.Field("Admin Role", adminRoleFormatted, true),
            MessageEmbed.Field("Mod Role", modRoleFormatted, true),
            MessageEmbed.Field("Modlog Channel", modlogFormatted, true),
            MessageEmbed.Field("Joinlog Channel", joinlogFormatted, true),
            MessageEmbed.Field("Leavelog Channel", leavelogFormatted, true),
            MessageEmbed.Field("\u200B", "\u200B", true)
        )

        ctx.embed {
            setTitle("Server Settings | ${ctx.guild?.name}")
            setThumbnail(ctx.guild?.iconUrl)
            setColor(Color.yellow)
            addFields(fields)
        }
    }
}