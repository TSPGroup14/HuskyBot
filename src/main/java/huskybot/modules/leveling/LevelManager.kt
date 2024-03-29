package huskybot.modules.leveling

import huskybot.Database
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.time.Instant
import kotlin.math.pow
import kotlin.random.Random

object LevelManager {

    fun updateXp(event: MessageReceivedEvent) {

        /* Calculate XP */
        val xp = Random.nextLong(2,8)

        /* Get Timestamp of Last XP Update */
        val timestamp = Database.getUserLastXPUpdate(event.guild.idLong, event.author.idLong)


        if (timestamp == null) {
            event.member?.user?.idLong?.let { Database.updateUserXP(event.guild.idLong, it, xp) }
            return      //This only runs if the user is not in the database yet
        }

        val time = Instant.parse(timestamp)
        val parsedTime = time.plusSeconds(120)

        //Only update the user xp if their current message is sent two minutes after the last xp update
        if (Instant.now().isAfter(parsedTime)) {
            event.member?.user?.idLong?.let { Database.updateUserXP(event.guild.idLong, it, xp) }

            /* Check and Update User Level */
            val totalXP = Database.getUserXP(event.guild.idLong, event.author.idLong)
            var level = Database.getUserLevel(event.guild.idLong, event.author.idLong)
            level = calcLevel(totalXP, level)

            Database.updateUserLevel(event.guild.idLong, event.author.idLong, level)
        }
    }

    fun calcLevel (xp: Long, lvl: Int): Int {
        val r = 1.15        //Level factor
        var highest = 1     //Highest level based off of xp
        var x = 100.0       //Level 1 Base xp

        do {
            x += 1000*r.pow(highest)
            highest++
        } while (xp >= x)

        return highest-1
    }

    fun notifyLevelUp(event: MessageReceivedEvent) {

    }
}