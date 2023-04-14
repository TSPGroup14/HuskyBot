package huskybot.modules.leveling

import huskybot.cmdFramework.Context
import kotlin.math.*

object LevelManager {
    private fun calcLevel (xp : Int, lvl : Int): Int {
        val r = 1.9
        var highest = 0
        var x = 100.0
        do {
            x *= r
            highest++
        } while (highest < lvl+1)
        return if (xp >= x) {
            lvl+1
        } else {
            lvl
        }
    }
}