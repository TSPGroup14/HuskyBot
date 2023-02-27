package huskybot.commands.misc

import huskybot.cmdFramework.Command
import huskybot.cmdFramework.CommandProperties
import huskybot.cmdFramework.Context
import huskybot.cmdFramework.Option
import net.dv8tion.jda.api.interactions.commands.OptionType

@CommandProperties(description = "Make the bot repeat an input string")
@Option(type = OptionType.STRING,
    name = "text",
    description = "A string that the bot will repeat back to you.",
    required = true)
class Repeat : Command(ExecutionType.STANDARD) {
    override fun execute(context: Context) {
        context.embed(
            "You said",
            context.args.gatherNext("text").removePrefix("<").removeSuffix(">")
        )
    }
}