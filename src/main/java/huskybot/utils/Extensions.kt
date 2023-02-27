package huskybot.utils

fun <T> List<T>.separate(): Pair<T, List<T>> = Pair(first(), drop(0)) //Used to be 1