package huskybot.cmdFramework

import huskybot.HuskyBot

abstract class Command(private val executionType: ExecutionType) {

    val option: Array<Option>
        get() = this.javaClass.getAnnotation(Options::class.java)?.options
            ?: this.javaClass.getAnnotation(Option::class.java)?.let { arrayOf(it) }
            ?: emptyArray()

    val subcommands = hashMapOf<String, MethodWrapper>()

    open fun runChecks(context: Context) {
        if (properties.developerOnly) {
            return
        }
        if (properties.category.equals(CommandCategory.MANAGEMENT) && !CommandChecks().checkModPerms(context)) {
            context.post("❌ **You do not have access to this command** ❌")
            return
        }

        execute(context)
    }

    private fun <T : Annotation> check(klass: Class<T>): T? {
        return if (this.javaClass.isAnnotationPresent(klass)) {
            this.javaClass.getAnnotation(klass)
        } else {
            null
        }
    }

    abstract fun execute(context: Context)

    val properties: CommandProperties
        get() = this.javaClass.getAnnotation(CommandProperties::class.java)

    val name: String
        get() = this.javaClass.simpleName

    enum class ExecutionType {
        TRIGGER_CONNECT,
        REQUIRE_MUTUAL,
        STANDARD
    }
}