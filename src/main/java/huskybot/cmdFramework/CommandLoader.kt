package huskybot.cmdFramework

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import warden.framework.CommandScanner

class CommandLoader {
    fun loadCommands(jda: JDA) {
        val cmds = jda.updateCommands()
        //val commandList = commands.toList()

        commands.values.forEach { value ->
            //Read each command file and register it as a slash command

            //Create slash command
            val data = Commands.slash(value.name.lowercase(), value.properties.description)

            //Add the command options
            for (option: Option in value.option) {
                data.addOption(option.type, option.name, option.description, option.required)
            }

            //Register the command
            cmds.addCommands(data)

        }

        cmds.queue()   //Queue the commands to Discord

    }

    /**
     * Create map of commands
     */
    companion object {
        val commands = CommandScanner("huskybot.commands").scan().toMutableMap()
    }
}
