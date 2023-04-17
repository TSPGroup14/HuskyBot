package huskybot.modules.leveling

import huskybot.Database.updateUserXP
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.time.Instant

object MessageCounter {
    fun updateLevel(event: MessageReceivedEvent){
        event.member?.user?.idLong?.let { updateUserXP(event.guild.idLong, it, 2) }

    }
}