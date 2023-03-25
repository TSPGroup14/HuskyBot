package huskybot.commands.management

import huskybot.cmdFramework.Command
import huskybot.cmdFramework.CommandProperties
import huskybot.cmdFramework.Context
import warden.framework.SubCommand

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
        //TODO("Replace null with actual value")
        val adminRole = null
        val modRole = null
        val joinlogChannel = null
        val leavelogChannel = null
        val modlogChannel = null

        ctx.post("**WIP!**")
    }
}