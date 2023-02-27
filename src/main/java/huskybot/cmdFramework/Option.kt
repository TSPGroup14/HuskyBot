package huskybot.cmdFramework

import net.dv8tion.jda.api.interactions.commands.OptionType

/**
 * Framework to allow for command specific options
 */
annotation class Option(
    val name: String,
    val description: String,
    val type: OptionType,
    val required: Boolean = true
)
