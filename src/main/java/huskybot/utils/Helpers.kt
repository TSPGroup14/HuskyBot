package huskybot.utils

import huskybot.Database
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent

object Helpers {

    /**
     * Removes any stored data from the user in the database
     * pertaining to the guild that they left
     */
    fun cleanUser(event: GuildMemberRemoveEvent) {
        Database.removeUserData(event.guild.idLong, event.user.idLong)
    }
}