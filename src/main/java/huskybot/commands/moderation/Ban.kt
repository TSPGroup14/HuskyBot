package huskybot.commands.moderation

import huskybot.cmdFramework.*
import net.dv8tion.jda.api.interactions.commands.OptionType

@CommandProperties(description = "Ban a requested user")
@Options([
        Option(name = "user", description = "User that you would like to ban", type = OptionType.USER, required = true),
        Option(name = "reason", description = "Reason for banning the user", type = OptionType.STRING, required = false),
        Option(name = "duration", description = "How long the ban will last for", type = OptionType.INTEGER, required = false),
        Option(name = "days", description = "How many days worth of messages from the user to remove", OptionType.INTEGER, required = false)
    ])
class Ban : Command(ExecutionType.STANDARD) {
    override fun execute(context: Context) {

    }
}