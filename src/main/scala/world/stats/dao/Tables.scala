package world.stats.dao

import world.stats.model.{ Country, CountryStatistics }
import world.stats.model.Country.CountryId
import org.h2.jdbc.JdbcSQLSyntaxErrorException
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.jdbc.H2Profile.api._

import scala.concurrent.{ ExecutionContext, Future }

object Tables {

  def createSchemas(db: Database)(implicit ec: ExecutionContext): Future[Boolean] =
    db.run(
        DBIO.seq(
          countriesTable.schema.createIfNotExists,
          countriesStatisticsTable.schema.createIfNotExists
        )
      )
      .map(_ => true)
      .recover {
        // Slick tries to creates primary key each time even if call schema.createIfNotExists, just ignore this error.
        // https://github.com/slick/slick/issues/1999
        case e: JdbcSQLSyntaxErrorException
            if e.getMessage.contains("Constraint \"cs_pk\" already exists") =>
          true
      }

  /// Countries

  implicit val countryIdColumnType: JdbcType[CountryId] with BaseTypedType[CountryId] =
    MappedColumnType.base[CountryId, String](
      { c =>
        c.value
      }, { v =>
        CountryId(v)
      }
    )

  class CountriesTable(tag: Tag) extends Table[Country](tag, None, "countries") {

    val id          = column[CountryId]("id", O.PrimaryKey)
    val name        = column[String]("name")
    val capitalCity = column[String]("capital_city")

    override def * =
      (id, name, capitalCity) <> (Country.tupled, Country.unapply)
  }

  val countriesTable = TableQuery[CountriesTable]

  /// CountriesStatisticsInYear

  class CountriesStatisticsTable(tag: Tag)
      extends Table[CountryStatistics](tag, None, "countries_statistics") {
    val countryId  = column[CountryId]("country_id")
    val year       = column[Int]("year")
    val population = column[Long]("population")
    val gdpPpp     = column[Option[BigDecimal]]("gdp_ppp")

    def pk = primaryKey("cs_pk", (countryId, year))

    override def * =
      (countryId, year, population, gdpPpp) <> (CountryStatistics.tupled, CountryStatistics.unapply)

  }

  val countriesStatisticsTable = TableQuery[CountriesStatisticsTable]

}
