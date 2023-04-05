package huskybot.modules.leveling

import huskybot.Database.getUserXP
import huskybot.Database.updateUserXP
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.MessageReceivedEvent


object MessageCounter {
    fun updateLevel(event: MessageReceivedEvent){
        updateUserXP(event.guild.idLong, event.member?.idLong!!, 2)
    }
}