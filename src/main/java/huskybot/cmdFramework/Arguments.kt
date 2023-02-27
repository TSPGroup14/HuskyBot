package huskybot.cmdFramework

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionMapping

interface Arguments {
    val isEmpty: Boolean

    fun hasNext(name: String): Boolean

    fun <T> next(name: String, resolver: ArgumentResolver<T>): T?

    fun gatherNext(name: String): String

    class SlashArguments(private val event: SlashCommandInteractionEvent) : Arguments {
        override val isEmpty: Boolean
            get() = event.options.isEmpty()

        override fun hasNext(name: String) = event.getOption(name) != null

        override fun <T> next(name: String, resolver: ArgumentResolver<T>): T? = try {
            event.getOption(name, null, resolver.optionResolver)
        } catch (t: Throwable) {
            null
        }

        override fun gatherNext(name: String): String {
            return event.getOption(name, OptionMapping::getAsString)
                ?: ""
        }
    }
}