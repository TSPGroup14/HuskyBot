package huskybot.modules.modmail

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import java.awt.Color
import java.time.Instant

class ModmailEmbedBuilder(jda: JDA, guild: Guild, userInfo: Array<String>?, moderatorInfo: Array<String>?, message: String) {

    /**
     * Prebuilt embed for user-side message sent
     */
    val pmSendEmbed = EmbedBuilder()
        .setTitle("Message Sent")
        .setDescription(message)
        .setFooter("${guild.name} (${guild.idLong})", guild.iconUrl)
        .setTimestamp(Instant.now())
        .setColor(Color.GREEN)
        .build()

    /**
     * Prebuilt embed for user-side message recieved
     */
    val pmRecieveEmbed = EmbedBuilder()
        .setTitle("Message Recieved")
        .setDescription(message)
        .setAuthor(moderatorInfo?.get(0), null, moderatorInfo?.get(1))
        .setFooter("${guild.name} (${guild.idLong})", guild.iconUrl)
        .setTimestamp(Instant.now())
        .setColor(Color.RED)
        .build()

    /**
     * Prebuilt embed for guild-side message sent
     */
    val guildSendEmbed = EmbedBuilder()
        .setTitle("Message Sent")
        .setDescription(message)
        .setFooter(userInfo?.get(0), userInfo?.get(1))
        .setTimestamp(Instant.now())
        .setColor(Color.GREEN)
        .build()

    /**
     * Prebuilt embed for guild-side message recieved
     */
    val guildReceiveEmbed = EmbedBuilder()
        .setTitle("Message Recieved")
        .setDescription(message)
        .setAuthor(userInfo?.get(0), null, userInfo?.get(1))
        .setFooter(jda.selfUser.name, jda.selfUser.avatarUrl)
        .setTimestamp(Instant.now())
        .setColor(Color.RED)
        .build()
}
