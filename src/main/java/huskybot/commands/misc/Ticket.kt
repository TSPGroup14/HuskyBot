package huskybot.commands.misc

import huskybot.cmdFramework.*
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal

@CommandProperties(description = "Create a ticket")
@Options([
    Option(type = OptionType.STRING, name = "subject", description = "The subject of your ticket", required = true),
    Option(type = OptionType.STRING, name = "body", description = "The body text of your ticket", required = true)
])
class Ticket : Command(ExecutionType.STANDARD) {

    override fun execute(context: Context) {

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

        context.event.replyModal(modal)
                .queue()
    }

}