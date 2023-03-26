package huskybot.commands.misc

import huskybot.cmdFramework.Command
import huskybot.cmdFramework.CommandProperties
import huskybot.cmdFramework.Context
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal

/**
 * Example command for testing modal interaction
 */
@CommandProperties(description = "Open a modal interaction with the bot")
class Request : Command(ExecutionType.STANDARD) {
    override fun execute(context: Context) {
        //Text input sections
        val t1 = TextInput.create("subject", "Subject", TextInputStyle.SHORT)
            .setPlaceholder("Subject of this modal")
            .setMinLength(0)
            .setMaxLength(100) // or setRequiredRange(10, 100)
            .build()

        val t2 = TextInput.create("body", "Body", TextInputStyle.PARAGRAPH)
            .setPlaceholder("Please enter your main text here.")
            .setMinLength(0)
            .setMaxLength(4000)
            .build()

        val modal = Modal.create("modalOne", "Test Modal")
            .addComponents(ActionRow.of(t1), ActionRow.of(t2))
            .build()

        context.event.replyModal(modal)
            .queue()
    }
}