package world.stats.config

class ConfigException(message: String, cause: Option[Throwable] = None)
    extends RuntimeException(message, cause.orNull)
