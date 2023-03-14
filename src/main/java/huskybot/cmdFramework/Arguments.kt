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

        /**
         * Function that checks if a given option exists or has been used
         * @param name Name of the option
         * @return The option as an Option type, or null if the option cannot be found
         */
        override fun hasNext(name: String) = event.getOption(name) != null

        /**
         * Function that changes the option output given by Discord from a generic
         * string type to its desired type
         *
         * **Example:**
         *
         *     val someInt:Int = context.args.next("input_integer", ArgumentResolver.INTEGER)
         */
        override fun <T> next(name: String, resolver: ArgumentResolver<T>): T? = try {
            event.getOption(name, null, resolver.optionResolver)
        } catch (t: Throwable) {
            null
        }

        /**
         * Function that returns an Option's output as a string, no matter what
         * type that option is
         *
         * @param name Name of the option
         * @return Output of the option given as a string. Will return "" if the option is not found
         */
        override fun gatherNext(name: String): String {
            return event.getOption(name, OptionMapping::getAsString)
                ?: ""
        }
    }
}