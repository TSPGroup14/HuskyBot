package huskybot.commands.misc

import com.sun.management.OperatingSystemMXBean
import huskybot.Database
import huskybot.HuskyBot
import huskybot.cmdFramework.Command
import huskybot.cmdFramework.CommandProperties
import huskybot.cmdFramework.Context
import huskybot.utils.toTimeString
import net.dv8tion.jda.api.JDA
import java.lang.management.ManagementFactory
import java.text.DecimalFormat

@CommandProperties(description = "Shows the bot's current statistics")
class Stats : Command(ExecutionType.STANDARD) {

    private val dpFormatter = DecimalFormat("0.00")

    override fun execute(context: Context) {

        /* RAM Use Stats */
        val rUsedRaw = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        val rPercent = dpFormatter.format(rUsedRaw.toDouble() / Runtime.getRuntime().totalMemory() * 100)
        val usedMB = dpFormatter.format(rUsedRaw.toDouble() / 1048576)

        /* Guild and User Stats */
        val servers = HuskyBot.shardManager.guildCache.size()
        val users = HuskyBot.shardManager.guilds.sumOf { it.memberCount }

        /* Shard Stats */
        val shards = HuskyBot.shardManager.shardsTotal
        val shardsOnline = HuskyBot.shardManager.shards.count { s -> s.status == JDA.Status.CONNECTED }
        val averageShardLatency = HuskyBot.shardManager.averageGatewayPing.toInt()

        /* CPU Stats */
        val osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean::class.java)
        val procCpuUsage = dpFormatter.format(osBean.processCpuLoad * 100)
        val sysCpuUsage = dpFormatter.format(osBean.systemCpuLoad * 100)

        /* Database Stats */
        val secondsSinceBoot = ((System.currentTimeMillis() - HuskyBot.startTime) / 1000).toDouble()
        val callsPerSecond = Database.calls / secondsSinceBoot
        val formattedCPS = dpFormatter.format(callsPerSecond)

        context.post(buildString {
            append("```asciidoc\n")
            append("= JVM =\n")
            append("Uptime          :: ").append((System.currentTimeMillis() - HuskyBot.startTime).toTimeString())
                .append("\n")
            append("JVM CPU Usage   :: ").append(procCpuUsage).append("%\n")
            append("System CPU Usage:: ").append(sysCpuUsage).append("%\n")
            append("RAM Usage       :: ").append(usedMB).append("MB (").append(rPercent).append("%)\n")
            append("Threads         :: ").append(Thread.activeCount()).append("\n\n")
            append("== HuskyBot ==\n")
            append("Guilds          :: ").append(servers).append("\n")
            append("Users           :: ").append(users).append("\n")
            append("Database Calls  :: ").append(Database.calls).append(" (").append(formattedCPS).append("/sec)")
                .append("\n")
            append("Shards Online   :: ").append(shardsOnline).append("/").append(shards).append("\n")
            append("Average Latency :: ").append(averageShardLatency).append("ms\n")
            append("```")
        })
    }
}