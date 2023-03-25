package warden.framework

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class SubCommand(
    val trigger: String,
    val description: String,
    val async: Boolean
)