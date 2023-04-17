package huskybot.modules.leveling

import huskybot.cmdFramework.Context
import kotlin.math.*

object LevelManager {
    fun calcLevel (xp: Int, lvl: Int): Int {
        val r = 1.9         //Level factor
        var highest = 1     //Highest level based off of xp
        var x = 100.0       //Level 1 Base xp

        //Base level
//        if (xp in 0..189) return 1

        do {
            x *= r
            highest++
        } while (xp >= x)

        return highest-1
    }
}