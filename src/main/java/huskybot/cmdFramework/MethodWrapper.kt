package huskybot.cmdFramework

import java.lang.reflect.Method

/**
 * Handles the loading of subcommands
 */
class MethodWrapper(val description: String, private val method: Method, private val cls: Command) {
//    fun invoke(ctx: Context, withArgs: Boolean = false) {
//        if (withArgs) {
//            method.invoke(cls, ctx, ctx.args.drop(1))
//        } else {
//            method.invoke(cls, ctx)
//        }
//    }

    fun invoke(ctx: Context) {
        method.invoke(cls, ctx)
    }
}