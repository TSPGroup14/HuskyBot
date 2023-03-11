package huskybot.modules.cmdHelpers

import java.util.function.Consumer

class RestException : Exception() {
    fun onFail() : Consumer<Throwable> {
        throw Exception("Failed to execute Rest action.")
    }
    //tronl
}