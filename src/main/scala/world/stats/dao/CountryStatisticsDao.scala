package world.stats.dao

import slick.jdbc.H2Profile.api._
import world.stats.dao.Tables.countriesStatisticsTable
import world.stats.model.Country.CountryId
import world.stats.model.CountryStatistics
import scala.concurrent.{ ExecutionContext, Future }
import world.stats.dao.Tables.countryIdColumnType

class CountryStatisticsDao(db: Database)(implicit ec: ExecutionContext) {

  def removeAll(): Future[Boolean] =
    db.run(countriesStatisticsTable.schema.truncate).map(_ => true)

  def insert(countriesStatistics: Seq[CountryStatistics]): Future[Boolean] =
    db.run(countriesStatisticsTable ++= countriesStatistics).map(_ => true)

  def setGdpPpp(countryId: CountryId, year: Int, gdpPpp: BigDecimal): Future[Int] = {
    val q = for {
      stats <- countriesStatisticsTable
      if stats.countryId === countryId && stats.year === year
    } yield stats.gdpPpp

    db.run(q.update(Some(gdpPpp)))
  }

  def getNumberOfStatistics(): Future[Int] =
    db.run(countriesStatisticsTable.length.result)

}
