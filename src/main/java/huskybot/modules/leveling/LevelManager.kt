package huskybot.modules.leveling

import huskybot.Database
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.time.Instant
import kotlin.random.Random

object LevelManager {

    fun updateXp(event: MessageReceivedEvent) {
        var time = Instant.parse(Database.getUserLastXPUpdate(event.guild.idLong, event.author.idLong))
        val parsedTime = time.plusSeconds(120)

        val xp = Random.nextInt(2,8)

        //Only update the user xp if their current message is sent two minutes after the last xp update
        if (Instant.now().isAfter(parsedTime)) {
            event.member?.user?.idLong?.let { Database.updateUserXP(event.guild.idLong, it, xp) }
        }

        /* Check and Update User Level */
        val totalXP = Database.getUserXP(event.guild.idLong, event.author.idLong) ?: 0
        var level = Database.getUserLevel(event.guild.idLong, event.author.idLong)
        level = calcLevel(totalXP, level)


    }

    fun calcLevel (xp: Int, lvl: Int): Int {
        val r = 1.9         //Level factor
        var highest = 1     //Highest level based off of xp
        var x = 100.0       //Level 1 Base xp

        do {
            x *= r
            highest++
        } while (xp >= x)

        return highest-1
    }

    fun notifyLevelUp(event: MessageReceivedEvent) {

    }
}