package huskybot.cmdFramework

import net.dv8tion.jda.api.Permission

class CommandChecks {

    /**
     * Helper method that determins of the acting user has the proper permissions within the guild
     */
    fun checkModPerms(ctx: Context): Boolean {
        if (ctx.member.hasPermission(Permission.MANAGE_SERVER) && ctx.member.hasPermission(Permission.MANAGE_CHANNEL)) {
            return true
        }

        return false
    }
    //Class for any general command pre-checks.
}