package world.stats.config

import com.typesafe.config.ConfigFactory
import io.circe
import io.circe.config.syntax._
import io.circe.generic.auto._
import scala.util.{ Failure, Success, Try }

case class ApplicationConfig(
    worldBank: WorldBackConfig,
    db: DbConfig,
    statistics: StatisticsConfig
)

case class WorldBackConfig(apiBaseUrl: String)

case class StatisticsConfig(fromYear: Int, toYear: Int)

case class DbConfig(
    url: String,
    driver: String,
    keepAliveConnection: Boolean,
    connectionPool: String
)

object ApplicationConfig {

  def build(configFile: String = "application.conf"): Try[ApplicationConfig] =
    for {
      config <- Try(ConfigFactory.parseResources(configFile).resolve()).recoverWith {
        case e =>
          Failure(new ConfigException(e.getMessage, Some(e)))
      }
      applicationConfig <- config.as[ApplicationConfig] match {
        case Left(error: circe.Error) =>
          Failure(
            new ConfigException(
              s"Failed to parse config, error: ${error.getMessage}",
              Some(error)
            )
          )
        case Right(applicationConfig) => Success(applicationConfig)
      }
    } yield {
      applicationConfig
    }
}
