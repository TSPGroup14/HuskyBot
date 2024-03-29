package huskybot.commands.misc

import huskybot.Database
import huskybot.cmdFramework.*
import net.dv8tion.jda.api.interactions.commands.OptionType

@CommandProperties(description = "Testing command")
class Test : Command(ExecutionType.STANDARD) {
    override fun execute(context: Context) {
        this.subcommands[context.event.subcommandName]!!.invoke(context)
    }

    @Option("name", "Name that you would like to use for the modmail category", OptionType.STRING, false)
    @SubCommand("modmail", "Setup modmail", false)
    fun modmail(ctx: Context) {
        ctx.deferReply()

        val categoryName = ctx.args.next("name", ArgumentResolver.STRING)!!

        val category = ctx.guild?.createCategory(categoryName)
            ?.submit()

       ctx.guild?.let { Database.setCategory(it.idLong, category?.get()?.idLong) }

        ctx.hookedEmbed("Modmail Category Created", "**Category Name:** ``${category?.get()?.name}``")
    }
}