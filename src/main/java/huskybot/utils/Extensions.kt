package huskybot.utils

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed

fun <T> List<T>.separate(): Pair<T, List<T>> = Pair(first(), drop(0)) //Used to be 1

fun EmbedBuilder.addFields(fields: Array<MessageEmbed.Field>) {
    for (field in fields) {
        this.addField(field)
    }
}