package world.stats.dao

import world.stats.model.Country
import world.stats.dao.Tables.countriesTable
import world.stats.model.Country.CountryId
import slick.jdbc.H2Profile.api._
import Tables.countryIdColumnType

import scala.concurrent.{ ExecutionContext, Future }

class CountryDao(db: Database)(implicit ec: ExecutionContext) {

  def findAll(): Future[Seq[Country]] = db.run(countriesTable.result)

  def findAllCountryIds(): Future[Set[CountryId]] =
    db.run(countriesTable.map(_.id).result).map(_.toSet)

  def removeAll(): Future[Boolean] = db.run(countriesTable.schema.truncate).map(_ => true)

  def insert(countries: Seq[Country]): Future[Boolean] =
    db.run(countriesTable ++= countries).map(_ => true)

}
