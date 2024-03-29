package huskybot.commands.management

import huskybot.Database
import huskybot.cmdFramework.*
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.awt.Color

@CommandProperties(description = "Setup and modify settings for the modmail system")
class Modmail : Command(ExecutionType.STANDARD) {
    override fun execute(context: Context) {
        this.subcommands[context.event.subcommandName]!!.invoke(context)
    }

    @Option("name", "Name that you would like to use for the modmail category", OptionType.STRING, true)
    @SubCommand("setup", "Configures modmail and creates the category that all tickets will be placed under", false)
    fun setup(ctx: Context) {
        /* Check Perms and State */
        if(!checkPerms(ctx)) {
            ctx.post("❌ **You do not have access to this command** ❌")
            return
        }

        if (Database.getModmailState(ctx.guild!!.idLong)) {
            ctx.post("❌ **Modmail is already enabled within this server!** ❌")
            return
        }

        /* Create Category and Log Channel */
        ctx.deferReply()

        val categoryName = ctx.args.next("name", ArgumentResolver.STRING)!!

        val category = ctx.guild.createCategory(categoryName)
            .submit()

        val logChannel = category.get()?.createTextChannel("modmail-logs")
            ?.submit()


        /* Log Category and Channel ID */
        ctx.guild.let { Database.setCategory(it.idLong, category.get()?.idLong) }
        ctx.guild.let { Database.setModmailLog(it.idLong, logChannel?.get()?.idLong) }

        ctx.hookedEmbed("Modmail Category Created", "**Category Name:** ``${category.get()?.name}``")
    }

    @SubCommand("disable", "Disables modmail system and deletes the modmail category", false)
    fun disable(ctx: Context) {

        /* Check Perms and State */
        if(!checkPerms(ctx)) {
           ctx.post("❌ **You do not have access to this command** ❌")
            return
        }

        if (!Database.getModmailState(ctx.guild!!.idLong)) {
            ctx.post("❌ **Modmail is not enabled within this server!** ❌")
            return
        }

        ctx.deferReply()

        /* Get Category & Log Channel */
        val category = Database.getCategory(ctx.guild!!.idLong)?.let { ctx.guild.getCategoryById(it) }
        val logChannel = Database.getModmailLog(ctx.guild.idLong).let { ctx.guild.getTextChannelById(it!!) }

        /* Delete Category and Log Channel */
        logChannel?.delete()?.queue{
            category?.delete()?.queue()
        }

        ctx.guild.let { Database.setCategory(it.idLong, null) }
        ctx.guild.let { Database.setModmailLog(it.idLong, null) }

        ctx.hookedEmbed{
            setTitle("Modmail Successfully Disabled")
            setColor(Color.GREEN)
        }
    }

    /**
     * Helper method that determins of the acting user has the proper permissions within the guild
     */
    private fun checkPerms(ctx: Context): Boolean {
        if (ctx.member.hasPermission(Permission.MANAGE_SERVER) && ctx.member.hasPermission(Permission.MANAGE_CHANNEL)) {
            return true
        }

        return false
    }
}