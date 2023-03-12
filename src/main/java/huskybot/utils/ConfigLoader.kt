package huskybot.utils

import java.io.FileReader
import java.util.*

class ConfigLoader(filePath: String) {
    private val conf = FileReader(filePath).use { fr -> Properties().apply { load(fr) } }

    operator fun contains(key: String) = conf.containsKey(key) && conf.getProperty(key).isNotEmpty()
    operator fun get(key: String, default: String? = null): String = conf.getProperty(key)
        ?: default
        ?: throw IllegalArgumentException("$key is not present in config!")

    private fun opt(key: String, default: String? = null): String? = conf.getProperty(key, default)

    val token = get("token")        //discord login token
    val sentryDsn = opt("sentry")   //sentry login token

    companion object {
        fun load(): ConfigLoader {
            val configPath = System.getenv("warden_config")
                ?: "config.properties"

            return ConfigLoader(configPath)
            // TODO: Perhaps allow loading config from an env, and additionally, a program flag (--config).
            // Also consider switching to a more flexible config like hocon.
        }
    }
}