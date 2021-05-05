package world.stats.dataloader

import world.stats.model.Country
import world.stats.dao.CountryDao
import world.stats.dataloader.CountriesLoader.{ getCountriesOnly, CountryFromApi }
import world.stats.model.Country.CountryId
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ ExecutionContext, Future }
import io.circe.generic.auto._

/**
  *  Loads countries dataset from worldbank.org and stores into database.
  */
class CountriesLoader(val worldBankApi: WorldBankApi, countryDao: CountryDao)(
    implicit val ec: ExecutionContext
) extends DataLoaderSupport {

  private val logger = Logger(this.getClass)

  def load(): Future[Seq[Country]] = {
    logger.info("Loading countries...")
    for {
      _ <- countryDao.removeAll()
      countries <- loadDataByPages[CountryFromApi, Seq[Country]](
        "/country/all?format=json",
        300
      ) { countriesFromApi =>
        val countiesModels = countriesFromApi.map(_.toCountryModel)
        countryDao.insert(getCountriesOnly(countiesModels)).map(_ => countiesModels)
      }.map(_.flatten)
    } yield {
      countries
    }
  }
}

object CountriesLoader {
  def getCountriesOnly(items: Seq[Country]): Seq[Country] =
    // World bank returns not only countries, but regions or continents (Like Africa, etc).
    // To get only countries, we need to get records where `capitalCity` is not empty.
    items.filter(!_.capitalCity.isEmpty)

  case class CountryFromApi(iso2Code: String, name: String, capitalCity: String) {
    def toCountryModel = Country(CountryId(iso2Code), name, capitalCity)
  }
}
