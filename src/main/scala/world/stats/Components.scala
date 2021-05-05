package world.stats

import world.stats.config.{ ApplicationConfig, StatisticsConfig, WorldBackConfig }
import world.stats.dataloader.{ CountriesLoader, StatisticsLoader, WorldBankApi }
import world.stats.dao.{ CountryDao, CountryStatisticsDao, Tables }
import world.stats.statistics.StatisticsQueryService
import akka.actor.ActorSystem
import com.softwaremill.macwire.wire
import com.typesafe.scalalogging.Logger
import slick.jdbc.H2Profile.api._
import sttp.client3.SttpBackend
import sttp.client3.akkahttp.AkkaHttpBackend
import scala.concurrent.{ ExecutionContextExecutor, Future }

trait Components {
  private val logger = Logger(this.getClass)

  implicit lazy val actorSystem: ActorSystem = ActorSystem()

  implicit lazy val ec: ExecutionContextExecutor = actorSystem.dispatcher

  lazy val sttpBackend: SttpBackend[Future, Any] =
    AkkaHttpBackend.usingActorSystem(actorSystem)

  // `Application` is the "edge" of our application, so we can do "unsafe" operations here
  lazy val applicationConfig = ApplicationConfig.build().get

  lazy val dbConfig = applicationConfig.db

  lazy val db: Database = Database.forURL(
    url = dbConfig.url,
    driver = dbConfig.driver,
    keepAliveConnection = dbConfig.keepAliveConnection
  )

  lazy val worldBankConfig: WorldBackConfig = applicationConfig.worldBank

  lazy val statisticsConfig: StatisticsConfig = applicationConfig.statistics

  lazy val worldBankApi: WorldBankApi = wire[WorldBankApi]

  lazy val countryDao: CountryDao = wire[CountryDao]

  lazy val countryStatisticsDao: CountryStatisticsDao = wire[CountryStatisticsDao]

  lazy val countriesLoader: CountriesLoader = wire[CountriesLoader]

  lazy val statisticsLoader: StatisticsLoader = wire[StatisticsLoader]

  lazy val statisticsQueryService: StatisticsQueryService = wire[StatisticsQueryService]

  def bootstrap(): Future[Boolean] = {
    logger.info("Creating db schemas...")
    Tables.createSchemas(db)
  }

}
