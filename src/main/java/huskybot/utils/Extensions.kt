package huskybot.utils

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color

fun <T> List<T>.separate(): Pair<T, List<T>> = Pair(first(), drop(0)) //Used to be 1

fun EmbedBuilder.addFields(fields: Array<MessageEmbed.Field>) {
    for (field in fields) {
        this.addField(field)
    }
}

fun String.toColorOrNull() = try {
    Color.decode(this)
} catch (e: NumberFormatException) {
    null
}

fun Long.toTimeString(): String {
    val seconds = this / 1000 % 60
    val minutes = this / (1000 * 60) % 60
    val hours = this / (1000 * 60 * 60) % 24
    val days = this / (1000 * 60 * 60 * 24)

    return when {
        days > 0 -> String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds)
        hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
        else -> String.format("%02d:%02d", minutes, seconds)
    }
}