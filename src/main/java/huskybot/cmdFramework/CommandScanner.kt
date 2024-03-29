package huskybot.cmdFramework

import com.google.common.reflect.ClassPath
import huskybot.HuskyBot

class CommandScanner(private val pkg: String) {
    fun scan(): Map<String, Command> {
        val classes = ClassPath.from(this::class.java.classLoader).getTopLevelClassesRecursive(pkg)
        HuskyBot.log.debug("Discovered ${classes.size} commands")

        return classes
            .asSequence()
            .map { it.load() }
            .map { it.getDeclaredConstructor().newInstance() as Command }
            .map(::loadSubCommands)
            .associateBy { it.name.lowercase() }
    }

    private fun loadSubCommands(cmd: Command): Command {
        val methods = cmd::class.java.methods.filter { it.isAnnotationPresent(SubCommand::class.java) }
        HuskyBot.log.debug("Discovered ${methods.size} subcommands for command ${cmd.name}")

        for (meth in methods) {
            val annotation = meth.getAnnotation(SubCommand::class.java)
            val trigger = annotation.trigger.lowercase()
            val description = annotation.description

            val option = meth.getAnnotation(Options::class.java)?.options
                    ?: meth.getAnnotation(Option::class.java)?.let { arrayOf(it) }
                    ?: emptyArray()

            val wrapper = MethodWrapper(description, option, meth, cmd)
            cmd.subcommands[trigger] = wrapper
        }

        return cmd
    }
}