package huskybot

import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.time.Instant

object Database {
    private val pool = HikariDataSource()
    var calls = 0L
        private set

    val connection: Connection
        get() {
            calls++
            return pool.connection
        }

    init {
        if (!pool.isRunning) {
            pool.jdbcUrl = "jdbc:sqlite:huskybot.db"
        }

        setupTables()
    }

    /**
     * Private function that sets up the sql tables
     */
    private fun setupTables() = runSuppressed {
        connection.use {
            it.createStatement().apply {
                // Guild Settings
                addBatch("CREATE TABLE IF NOT EXISTS prefixes (id INTEGER PRIMARY KEY, prefix TEXT NOT NULL)")
                addBatch("CREATE TABLE IF NOT EXISTS modlogs (guildid INTEGER PRIMARY KEY, channelid INTEGER NOT NULL)")
                addBatch("CREATE TABLE IF NOT EXISTS joinlogs (guildid INTEGER PRIMARY KEY, channelid INTEGER NOT NULL)")
                addBatch("CREATE TABLE IF NOT EXISTS leavelogs (guildid INTEGER PRIMARY KEY, channelid INTEGER NOT NULL)")
                addBatch("CREATE TABLE IF NOT EXISTS guildsettings (guildid INTEGER PRIMARY KEY, admin_id INTEGER, mod_id INTEGER, modmail_category INTEGER, modmail_log INTEGER)")
                // ModLog stuff
                addBatch("CREATE TABLE IF NOT EXISTS casenum (guildid INTEGER PRIMARY KEY, count INTEGER NOT NULL)")
                addBatch("CREATE TABLE IF NOT EXISTS warns (guildid INTEGER, userid INTEGER, count INTEGER NOT NULL, PRIMARY KEY(guildid, userid))")
                // Modmail stuff
                addBatch("CREATE TABLE IF NOT EXISTS category (guildid INTEGER PRIMARY KEY, id INTEGER NOT NULL)")
                addBatch("CREATE TABLE IF NOT EXISTS modmailenabled (guildid INTEGER PRIMARY KEY, state INTEGER)")
                addBatch("CREATE TABLE IF NOT EXISTS userinfo (id INTEGER PRIMARY KEY, previousguild INTEGER, auto_confirm INTEGER)")
                // User stuff
                addBatch("CREATE TABLE IF NOT EXISTS userlevel (userid INTEGER, guildid INTEGER, level INTEGER, xp INTEGER, last_update TEXT, PRIMARY KEY(userid, guildid))")
            }.executeBatch()
        }
    }

    /* Server specififc admin-role */
    fun getAdminRole(guildId: Long) = getFromDatabase("guildsettings", guildId, "admin_id")?.toLong()

    fun setAdminRole(guildId: Long, roleId: Long?) = runSuppressed {
        connection.use {
            if (roleId == null) {
                buildStatement(it, "UPDATE guildsettings SET admin_id = null WHERE guildid = ?", guildId).executeUpdate()
                return@runSuppressed
            }

            buildStatement(
                it, "INSERT INTO guildsettings(guildid, admin_id) VALUES (?, ?) ON CONFLICT(guildid) DO UPDATE SET admin_id = ?",
                guildId, roleId, roleId
            ).executeUpdate()
        }
    }

    /* Server specififc mod-role */
    fun getModRole(guildId: Long) = getFromDatabase("guildsettings", guildId, "mod_id")?.toLong()

    fun setModRole(guildId: Long, roleId: Long?) = runSuppressed {
        connection.use {
            if (roleId == null) {
                buildStatement(it, "UPDATE guildsettings SET mod_id = null WHERE guildid = ?", guildId).executeUpdate()
                return@runSuppressed
            }

            buildStatement(
                it, "INSERT INTO guildsettings(guildid, mod_id) VALUES (?, ?) ON CONFLICT(guildid) DO UPDATE SET mod_id = ?",
                guildId, roleId, roleId
            ).executeUpdate()
        }
    }

    /* Modlog related methods */

    fun getModlogChannel(guildId: Long) = getFromDatabase("modlogs", guildId, "channelid")?.toLong()

    fun setModlogChannel(guildId: Long, channelId: Long?) = runSuppressed {
        connection.use {
            if (channelId == null) {
                buildStatement(it, "DELETE FROM modlogs WHERE guildid = ?", guildId).executeUpdate()
                return@runSuppressed
            }

            buildStatement(
                it, "INSERT INTO modlogs(guildid, channelid) VALUES (?, ?) ON CONFLICT(guildid) DO UPDATE SET channelid = ?",
                guildId, channelId, channelId
            ).executeUpdate()
        }
    }

    /* Modlog case count related methods */

    fun getCaseCount(guildId: Long) = getFromDatabase("casenum", guildId, "count")?.toInt() ?: 0

    fun updateCaseCount(guildId: Long) = runSuppressed {
        connection.use {
            buildStatement(
                it, "INSERT INTO casenum(guildid, count) VALUES (?, ?) ON CONFLICT(guildid) DO UPDATE SET count = ?",
                guildId, getCaseCount(guildId)+1, getCaseCount(guildId)+1
            ).executeUpdate()
        }
    }

    /* Warn Count related methods */

    fun getWarnCount(guildId: Long, userId: Long) = getValueFromDatabase("warns", guildId, userId, "count")?.toInt() ?: 0

    fun updateWarnCount(guildId: Long, userId: Long, inc: Boolean) = runSuppressed {
        var warns = getWarnCount(guildId, userId)

        when (inc) {
            true -> connection.use {
                buildStatement(
                    it, "INSERT INTO warns(guildid, userid, count) VALUES (?, ?, ?) ON CONFLICT(guildid, userid) DO UPDATE SET count = ?",
                    guildId, userId, warns + 1, warns + 1
                ).executeUpdate()
            }
            false -> connection.use {
                if (warns == 0) {
                    return@use
                }

                buildStatement(
                    it, "INSERT INTO warns(guildid, userid, count) VALUES (?, ?, ?) ON CONFLICT(guildid, userid) DO UPDATE SET count = ?",
                    guildId, userId, warns - 1, warns - 1
                ).executeUpdate()
            }
        }
    }

    /* Guild-Specific Logs */

    fun getJoinlogChannel(guildId: Long) = getFromDatabase("joinlogs", guildId, "channelid")?.toLong()

    fun setJoinlogChannel(guildId: Long, channelId: Long?) = runSuppressed {
        connection.use {
            if (channelId == null) {
                buildStatement(it, "DELETE FROM joinlogs WHERE guildid = ?", guildId).executeUpdate()
                return@runSuppressed
            }

            buildStatement(
                it, "INSERT INTO joinlogs(guildid, channelid) VALUES (?, ?) ON CONFLICT(guildid) DO UPDATE SET channelid = ?",
                guildId, channelId, channelId
            ).executeUpdate()
        }
    }

    fun getLeavelogChannel(guildId: Long) = getFromDatabase("leavelogs", guildId, "channelid")?.toLong()

    fun setLeavelogChannel(guildId: Long, channelId: Long?) = runSuppressed {
        connection.use {
            if (channelId == null) {
                buildStatement(it, "DELETE FROM leavelogs WHERE guildid = ?", guildId).executeUpdate()
                return@runSuppressed
            }

            buildStatement(
                it, "INSERT INTO leavelogs(guildid, channelid) VALUES (?, ?) ON CONFLICT(guildid) DO UPDATE SET channelid = ?",
                guildId, channelId, channelId
            ).executeUpdate()
        }
    }

    /* User Leveling */

    fun getUserXP(guildId: Long, userId: Long) = getValueFromDatabase("userlevel", guildId, userId, "xp")?.toInt()

    fun updateUserXP(guildId: Long, userId: Long, count: Int) = runSuppressed {
        connection.use {
            buildStatement(it, "INSERT INTO userlevel (userid, guildid, xp, last_update) VALUES (?, ?, ?, ?) ON CONFLICT(userid, guildid) DO UPDATE SET xp = xp + ?, last_update = ?",
            userId, guildId, count, Instant.now().toString(), count, Instant.now().toString())
                .executeUpdate()
        }
    }

    fun getUserLastXPUpdate(guildId: Long, userId: Long) = getValueFromDatabase("userlevel", guildId, userId, "last_update")

    fun getUserLevel(guildId: Long, userId: Long) = getValueFromDatabase("userlevel", guildId, userId, "level")?.toInt() ?: 0

    fun updateUserLevel(guildId: Long, userId: Long, level: Int) = runSuppressed {
        connection.use {
            buildStatement(it, "UPDATE userlevel SET level = ? WHERE guildid = ? AND userid = ?",
            level, guildId, userId)
        }
    }

    /* Modmail */

    fun getCategory(guildId: Long) = getFromDatabase("guildsettings", guildId, "modmail_category")?.toLong()

    fun setCategory(guildId: Long, categoryId: Long?) = runSuppressed {
        connection.use {
            if (categoryId == null) {
                buildStatement(
                    it, "UPDATE guildsettings SET modmail_category = null WHERE guildid = ?", guildId
                ).executeUpdate()
                return@runSuppressed
            }

            buildStatement(
                it, "INSERT INTO guildsettings(guildid, modmail_category) VALUES (?, ?) ON CONFLICT(guildid) DO UPDATE SET modmail_category = ?",
                guildId, categoryId, categoryId
            ).executeUpdate()
        }
    }

    fun getModmailLog(guildId: Long) = getFromDatabase("guildsettings", guildId, "modmail_log")?.toLong()

    fun setModmailLog(guildId: Long, channelId: Long?) = runSuppressed {
       connection.use {
           if (channelId == null) {
               buildStatement(
                   it, "UPDATE guildsettings SET modmail_log = null WHERE guildid = ?", guildId
               ).executeUpdate()
               return@runSuppressed
           }

           buildStatement(
               it, "INSERT INTO guildsettings(guildid, modmail_log) VALUES (?, ?) ON CONFLICT(guildid) DO UPDATE SET modmail_log = ?",
               guildId, channelId, channelId
           ).executeUpdate()
       }
    }
    fun getModmailState(guildId: Long) : Boolean {
        return getCategory(guildId) != null
    }

    fun setModmailState(guildId: Long, newState: Boolean) = runSuppressed {
        when (newState) {
            true -> connection.use {
                buildStatement(
                    it, "INSERT INTO modmailenabled(guildid, state) VALUES (?, ?) ON CONFLICT(guildid) DO UPDATE SET state = ?",
                    guildId, 1, 1
                ).executeUpdate()
            }
            false -> connection.use {
                buildStatement(
                    it, "INSERT INTO modmailenabled(guildid, state) VALUES (?, ?) ON CONFLICT(guildid) DO UPDATE SET state = ?",
                    guildId, 0, 0
                ).executeUpdate()
            }
        }
    }

    fun getConfirmationState(userId: Long) = getFromDatabase("userinfo", userId, "auto_confirm")?.toInt() ?: 0

    fun setConfirmationState(userId: Long, newState: Boolean) = runSuppressed {
        when (newState) {
            true -> connection.use {
                buildStatement(
                    it, "INSERT INTO userinfo(id, auto_confirm) VALUES (?, ?) ON CONFLICT(id) DO UPDATE SET auto_confirm = ?",
                    userId, 1, 1
                ).executeUpdate()
            }
            false -> connection.use {
                buildStatement(
                    it, "INSERT INTO userinfo(id, auto_confirm) VALUES (?, ?) ON CONFLICT(id) DO UPDATE SET auto_confirm = ?",
                    userId, 0, 0
                ).executeUpdate()
            }
        }
    }

    fun getPreviousGuild(userId: Long) = getFromDatabase("userinfo", userId, "previousguild")?.toLong()

    fun setPreviousGuild(userId: Long, guildId: Long) = runSuppressed {
        connection.use {
            buildStatement(
                it, "INSERT INTO userinfo(id, previousguild) VALUES (?, ?) ON CONFLICT(id) DO UPDATE SET previousguild = ?",
                userId, guildId, guildId
            ).executeUpdate()
        }
    }

    /*
     * +=================================================+
     * |                IGNORE BELOW THIS                |
     * +=================================================+
     */
    private fun getFromDatabase(table: String, id: Long, columnId: String): String? =
        suppressedWithConnection({ null }) {
            val idColumn = if (table.contains("userinfo") || table.contains("confirmation"))
                "id" else "guildid" // I'm an actual idiot I stg

            val results = buildStatement(it, "SELECT * FROM $table WHERE $idColumn = ?", id)
                .executeQuery()

            if (results.next()) results.getString(columnId) else null
        }

    private fun getSpecificFromDatabase(table: String, id: Long, columnId: String): String? =
        suppressedWithConnection({ null }) {
            val idColumn = if (table.contains("userinfo")) "id" else "guildid"

            val results = buildStatement(it, "SELECT $columnId FROM $table WHERE $idColumn = ?", id)
                .executeQuery()

            if (results.next()) results.getString(columnId) else null
        }

    private fun getValueFromDatabase(table: String, id1: Long, id2: Long, columnId: String): String? =
        suppressedWithConnection({ null }) {
            val results = buildStatement(it, "SELECT $columnId FROM $table WHERE guildid = ? AND userid = ?", id1, id2)
                .executeQuery()

            if (results.next()) results.getString(columnId) else null
        }

    private fun setEnabled(table: String, id: Long, enable: Boolean) = runSuppressed {
        connection.use {
            val stmt = if (!enable) {
                buildStatement(it, "DELETE FROM $table WHERE id = ?", id)
            } else {
                buildStatement(it, "INSERT OR IGNORE INTO $table (id) VALUES (?)", id)
            }

            stmt.execute()
        }
    }

    fun buildStatement(con: Connection, sql: String, vararg obj: Any): PreparedStatement {
        val statement = con.prepareStatement(sql)

        for ((i, o) in obj.withIndex()) {
            when (o) {
                is String -> statement.setString(i + 1, o)
                is Int -> statement.setInt(i + 1, o)
                is Long -> statement.setLong(i + 1, o)
                is Double -> statement.setDouble(i + 1, o)
            }
        }

        return statement
    }

    fun runSuppressed(block: () -> Unit) = runCatching(block).onFailure{e -> e.stackTrace}

    fun <T> suppressedWithConnection(default: () -> T, block: (Connection) -> T) = try {
        connection.use {
            block(it)
        }
    } catch (e: SQLException) {
        default()
    }
}