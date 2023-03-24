package huskybot

import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException

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
                addBatch("CREATE TABLE IF NOT EXISTS modroles (guildid INTEGER PRIMARY KEY, roleid INTEGER NOT NULL)")
                addBatch("CREATE TABLE IF NOT EXISTS adminroles (guildid INTEGER PRIMARY KEY, roleid INTEGER NOT NULL)")
                addBatch("CREATE TABLE IF NOT EXISTS modlogs (guildid INTEGER PRIMARY KEY, channelid INTEGER NOT NULL)")
                addBatch("CREATE TABLE IF NOT EXISTS joinlogs (guildid INTEGER PRIMARY KEY, channelid INTEGER NOT NULL)")
                addBatch("CREATE TABLE IF NOT EXISTS leavelogs (guildid INTEGER PRIMARY KEY, channelid INTEGER NOT NULL)")
                // ModLog stuff
                addBatch("CREATE TABLE IF NOT EXISTS casenum (guildid INTEGER PRIMARY KEY, count INTEGER NOT NULL)")
                addBatch("CREATE TABLE IF NOT EXISTS warns (guildid INTEGER, userid INTEGER, count INTEGER NOT NULL, PRIMARY KEY(guildid, userid))")
                // Modmail stuff
                addBatch("CREATE TABLE IF NOT EXISTS category (guildid INTEGER PRIMARY KEY, id INTEGER NOT NULL)")
                addBatch("CREATE TABLE IF NOT EXISTS modmailenabled (guildid INTEGER PRIMARY KEY, state INTEGER)")
                addBatch("CREATE TABLE IF NOT EXISTS confirmation (id INTEGER PRIMARY KEY, state INTEGER)")
                addBatch("CREATE TABLE IF NOT EXISTS previousguild (id INTEGER PRIMARY KEY, guildid INTEGER NOT NULL)")
                // Log stuff
            }.executeBatch()
        }
    }

    /* Server specififc admin-role */
    fun getAdminRole(guildId: Long) = getFromDatabase("adminroles", guildId, "roleid")?.toLong()

    fun setAdminRole(guildId: Long, roleId: Long?) = runSuppressed {
        connection.use {
            if (roleId == null) {
                buildStatement(it, "DELETE FROM adminroles WHERE guildid = ?", guildId).executeUpdate()
                return@runSuppressed
            }

            buildStatement(
                it, "INSERT INTO adminroles(guildid, roleid) VALUES (?, ?) ON CONFLICT(guildid) DO UPDATE SET roleid = ?",
                guildId, roleId, roleId
            ).executeUpdate()
        }
    }

    /* Server specififc mod-role */
    fun getModRole(guildId: Long) = getFromDatabase("modroles", guildId, "roleid")?.toLong()

    fun setModRole(guildId: Long, roleId: Long?) = runSuppressed {
        connection.use {
            if (roleId == null) {
                buildStatement(it, "DELETE FROM modroles WHERE guildid = ?", guildId).executeUpdate()
                return@runSuppressed
            }

            buildStatement(
                it, "INSERT INTO modroles(guildid, roleid) VALUES (?, ?) ON CONFLICT(guildid) DO UPDATE SET roleid = ?",
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

    fun getWarnCount(guildId: Long) = getFromDatabase("warns", guildId, "count")?.toInt() ?: 0

    fun warnCount(guildId: Long, userId: Long) = getWarnsFromDatabase("warns", guildId, userId, "count")?.toInt() ?: 0

    fun updateWarnCount(guildId: Long, userId: Long, inc: Boolean) = runSuppressed {
        var warns = warnCount(guildId, userId)

        when (inc) {
            true -> connection.use {
                buildStatement(
                    it, "INSERT INTO warns(guildid, userid, count) VALUES (?, ?, ?) ON CONFLICT(guildid) DO UPDATE SET count = ?",
                    guildId, userId, warns + 1, warns + 1
                ).executeUpdate()
            }
            false -> connection.use {
                if (warns == 0) {
                    return@use
                }

                buildStatement(
                    it, "INSERT INTO warns(guildid, userid, count) VALUES (?, ?, ?) ON CONFLICT(guildid) DO UPDATE SET count = ?",
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

    /* Modmail */

    fun getCategory(guildId: Long) = getFromDatabase("category", guildId, "id")?.toLong()

    fun setCategory(guildId: Long, categoryId: Long) = runSuppressed {
        if (categoryId == null) {
            return@runSuppressed
        }

        connection.use {
            buildStatement(
                it, "INSERT INTO category(guildid, id) VALUES (?, ?) ON CONFLICT(guildid) DO UPDATE SET id = ?",
                guildId, categoryId, categoryId
            ).executeUpdate()
        }
    }

    fun getModmailState(guildId: Long) = getFromDatabase("modmailenabled", guildId, "state")?.toInt() ?: 0

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

    fun getConfirmationState(userId: Long) = getFromDatabase("confirmation", userId, "state")?.toInt() ?: 0

    fun setConfirmationState(userId: Long, newState: Boolean) = runSuppressed {
        when (newState) {
            true -> connection.use {
                buildStatement(
                    it, "INSERT INTO confirmation(id, state) VALUES (?, ?) ON CONFLICT(id) DO UPDATE SET state = ?",
                    userId, 1, 1
                ).executeUpdate()
            }
            false -> connection.use {
                buildStatement(
                    it, "INSERT INTO confirmation(id, state) VALUES (?, ?) ON CONFLICT(id) DO UPDATE SET state = ?",
                    userId, 0, 0
                ).executeUpdate()
            }
        }
    }

    fun getPreviousGuild(userId: Long) = getFromDatabase("previousguild", userId, "guildid")?.toLong()

    fun setPreviousGuild(userId: Long, guildId: Long) = runSuppressed {
        connection.use {
            buildStatement(
                it, "INSERT INTO previousguild(id, guildid) VALUES (?, ?) ON CONFLICT(id) DO UPDATE SET guildid = ?",
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
            val idColumn = if (table.contains("previousguild") || table.contains("confirmation"))
                "id" else "guildid" // I'm an actual idiot I stg

            val results = buildStatement(it, "SELECT * FROM $table WHERE $idColumn = ?", id)
                .executeQuery()

            if (results.next()) results.getString(columnId) else null
        }

    private fun getWarnsFromDatabase(table: String, id1: Long, id2: Long, columnId: String): String? =
        suppressedWithConnection({ null }) {
            val results = buildStatement(it, "SELECT ? FROM ? WHERE guildid = ? AND userid = ?", columnId, table, id1, id2)
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