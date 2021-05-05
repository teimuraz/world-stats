package world.stats.dataloader

import world.stats.config.StatisticsConfig
import world.stats.dao.{ CountryDao, CountryStatisticsDao }
import world.stats.dataloader.StatisticsLoader.{
  getStatisticsModelsForCountriesOnly,
  GdpPppStatisticsFromApi,
  PopulationStatisticFromApi
}
import world.stats.model.Country.CountryId
import world.stats.model.CountryStatistics
import com.typesafe.scalalogging.Logger
import scala.concurrent.{ ExecutionContext, Future }
import io.circe.generic.auto._
import world.stats.util.FutureUtils.runSequentially

/**
  * Loads population, gdp/ppp by country and year from worldbank.org and stores into database.
  */
class StatisticsLoader(
    val worldBankApi: WorldBankApi,
    statisticsConfig: StatisticsConfig,
    countriesLoader: CountriesLoader,
    countryStatisticsDao: CountryStatisticsDao,
    countryDao: CountryDao
)(implicit val ec: ExecutionContext)
    extends DataLoaderSupport {

  private val logger = Logger(this.getClass)

  /**
    * Load statistics.
    * First it pulls population statistics from worldbank api and stores into statistics table.
    * Then it pulls gdp/ppp statistics from worldbank api and stores along with population stats
    * of given country/year in the same table.
    */
  def load(): Future[Boolean] = {
    logger.info("Loading statistics...")
    for {
      _ <- countriesLoader.load()
      _ <- countryStatisticsDao.removeAll()
      _ <- loadPopulationStatistics()
      _ <- loadGdpPppStatistics()
    } yield {
      logger.info("Done loading.")
      true
    }
  }

  def statisticsLoaded(): Future[Boolean] =
    countryStatisticsDao.getNumberOfStatistics().map(_ > 0)

  private def loadPopulationStatistics(): Future[Boolean] = {
    logger.info("Loading population statistics...")
    for {
      allCountryIds <- countryDao.findAllCountryIds()
      _ <- loadDataByPages[PopulationStatisticFromApi, Boolean](
        s"/country/all/indicator/SP.POP.TOTL?date=${statisticsConfig.fromYear}:${statisticsConfig.toYear}&format=json",
        200
      ) { populationStatsFromApi: Seq[PopulationStatisticFromApi] =>
        val countryStatisticModels =
          getStatisticsModelsForCountriesOnly(populationStatsFromApi, allCountryIds)
        countryStatisticsDao.insert(countryStatisticModels).map(_ => true)
      }
    } yield {
      true
    }
  }

  private def loadGdpPppStatistics(): Future[Boolean] = {
    logger.info("Loading GDP/PPP statistics...")
    for {
      _ <- loadDataByPages[GdpPppStatisticsFromApi, Boolean](
        s"/country/all/indicator/NY.GDP.MKTP.PP.CD?date=${statisticsConfig.fromYear}:${statisticsConfig.toYear}&format=json",
        200
      ) { gdpPppStatsFromApi: Seq[GdpPppStatisticsFromApi] =>
        setGdpPppStatistics(gdpPppStatsFromApi).map(_ => true)
      }
    } yield {
      true
    }
  }

  private def setGdpPppStatistics(
      gdpPppStatsFromApi: Seq[GdpPppStatisticsFromApi]
  ): Future[Seq[Boolean]] = {
    val setGdpPppFutures = gdpPppStatsFromApi.map { stats => () =>
      stats.value match {
        case None =>
          Future.successful(false)
        case Some(value) =>
          countryStatisticsDao
            .setGdpPpp(stats.country.id, stats.date, value)
            .map(_ => true)
      }

    }
    runSequentially(setGdpPppFutures)
  }
}

object StatisticsLoader {

  /**
    * Worldbank returns population statistics not only for countries, but for regions or continents (Like Africa, etc).
    * Also, for some items, population might not be set.
    * This method accepts all statistics from api, filters out statistics for countries only and narrowing down
    * to countries for which population is set, and converts data to application's statistics model.
    */
  def getStatisticsModelsForCountriesOnly(
      statistics: Seq[PopulationStatisticFromApi],
      allCountryIds: Set[CountryId]
  ): Seq[CountryStatistics] =
    statistics.foldLeft(Seq.empty[CountryStatistics])(
      (statisticsModels, statisticFromApi) =>
        statisticFromApi.value match {
          case Some(value) if allCountryIds.contains(statisticFromApi.country.id) =>
            val countryStatisticsModel = CountryStatistics(
              countryId = statisticFromApi.country.id,
              year = statisticFromApi.date,
              population = value,
              gdpPpp = None
            )
            statisticsModels :+ countryStatisticsModel
          case _ => statisticsModels
        }
    )

  case class PopulationStatisticFromApi(
      country: CountryFromApi,
      date: Int,
      value: Option[Long]
  )

  case class GdpPppStatisticsFromApi(
      country: CountryFromApi,
      date: Int,
      value: Option[BigDecimal]
  )

  case class CountryFromApi(id: CountryId, value: String)
}
