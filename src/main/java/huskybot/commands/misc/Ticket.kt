package huskybot.commands.misc

import huskybot.Database
import huskybot.cmdFramework.*
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import warden.framework.SubCommand

@CommandProperties(description = "Create a ticket")
class Ticket : Command(ExecutionType.STANDARD) {

    override fun execute(context: Context) {
        this.subcommands[context.event.subcommandName]!!.invoke(context)
    }

    @SubCommand("create", "Create a ticket within this server", false)
    fun create (ctx: Context) {
        //input text
        val t1 = TextInput.create("subject", "Subject", TextInputStyle.SHORT)
            .setPlaceholder("Subject of this ticket")
            .setMinLength(0)
            .setMaxLength(100) // or setRequiredRange(10, 100)
            .build()

        val t2 = TextInput.create("body", "Body", TextInputStyle.PARAGRAPH)
            .setPlaceholder("Please enter your main text here.")
            .setMinLength(0)
            .setMaxLength(4000)
            .build()

        val modal = Modal.create("ticket", "New Ticket")
            .addComponents(ActionRow.of(t1), ActionRow.of(t2))
            .build()

        ctx.event.replyModal(modal)
            .queue()
    }

    @SubCommand("close", "Close a ticket within this server", false)
    fun close (ctx: Context) {
        val category = ctx.guild?.getCategoryById(ctx.guild?.idLong?.let { Database.getCategory(it) }!!)
        var isInCategory = false
        for (channel in category?.channels!!) {
            if (ctx.channel.idLong == channel.idLong) {
                isInCategory = true
            }
        }
        if (!isInCategory) {
            return
        }
    }
}