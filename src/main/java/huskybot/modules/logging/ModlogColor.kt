package huskybot.modules.logging

import java.awt.Color

enum class ModlogColor(val color: Color) {
    BAN(toColor("0xff0000")),
    UNBAN(toColor("0x79add1")),
    SOFTBAN(toColor("0xffee02")),
    KICK(toColor("0xdb7b1c")),
    WARN(toColor("0xd1be79")),
    UNMUTE(toColor("0x1cdb68")),
    MUTE(toColor("0xd80f66")),
    PARDON(toColor("0x79d196")),
    TIMEOUT(toColor("0xe69138")),
    TIMEOUTCLEAR(toColor("0xb6d7a8"))


}

/**
 * Gets the requested color
 */
fun toColor(str: String): Color {
    return Color.decode(str)
}