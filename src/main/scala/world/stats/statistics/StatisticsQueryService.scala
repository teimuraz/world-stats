package world.stats.statistics

import world.stats.config.StatisticsConfig
import world.stats.dao.{ CountryDao, Tables }
import world.stats.model.Country
import world.stats.model.Country.CountryId
import world.stats.statistics.StatisticsQueryService.{ GdpGrowth, PopulationGrowth }
import world.stats.util.Tabulator
import slick.jdbc.H2Profile.api._
import scala.collection.MapView
import scala.concurrent.{ ExecutionContext, Future }

class StatisticsQueryService(
    db: Database,
    countryDao: CountryDao,
    statisticsConfig: StatisticsConfig
)(
    implicit ec: ExecutionContext
) {

  /**
    * Get the top countries by population growth
    * from [[world.stats.config.ApplicationConfig.statistics.fromYear]]
    * to [[world.stats.config.ApplicationConfig.statistics.toYear]]
    */
  def getTopCountriesByPopulationGrowth(topN: Int): Future[List[PopulationGrowth]] = {
    val tableName = Tables.countriesStatisticsTable.baseTableRow.tableName
    val query =
      sql"""
        SELECT "population_diffs"."country_id", SUM("population_diffs"."diff") "growth" FROM 
        |(
        |	SELECT 
        |		"population" - (LAG("population") OVER (PARTITION BY "country_id" ORDER BY "year")) AS "diff", 	
        |		"country_id", "year", "population" FROM "#$tableName"
        |)  "population_diffs" 
        |
        |GROUP BY "population_diffs"."country_id" 
        |ORDER BY "growth" DESC
        |LIMIT $topN
       """.stripMargin.as[(String, Long)]

    for {
      countries <- getAllCountries()
      populationGrowth <- db
        .run(query)
        .map(_.map {
          case (countryIdRaw, growth) =>
            val countryId = CountryId(countryIdRaw)
            PopulationGrowth(
              countryId,
              countries.get(countryId).map(_.name).getOrElse(""),
              growth
            )
        }.toList)
    } yield {
      populationGrowth
    }
  }

  def getAndDisplayTopCountriesByPopulationGrowth(top: Int): Future[Boolean] =
    getTopCountriesByPopulationGrowth(top).map {
      populationGrowth: Seq[PopulationGrowth] =>
        println(
          s"Top $top countries by population growth from ${statisticsConfig.fromYear} to ${statisticsConfig.toYear}"
        )
        val formatter = java.text.NumberFormat.getIntegerInstance
        val dataToDisplay =
          Seq(
            Seq("Country Id", "Country Name", "Population Growth")
          ) ++ populationGrowth.map(
            p => Seq(p.countryId.value, p.countryName, formatter.format(p.growth))
          )
        println(Tabulator.format(dataToDisplay))
        true
    }

  /**
    * Get the top countries by GDP/PPP growth
    * from [[world.stats.config.ApplicationConfig.statistics.fromYear]]
    * to [[world.stats.config.ApplicationConfig.statistics.toYear]]
    * limited to the subset of top countries by population growth.
    */
  def getTopCountriesByGdpGrowth(
      topNByGdpGrowth: Int,
      withinTopNByPopulationGrowth: Int
  ): Future[List[GdpGrowth]] = {
    val tableName = Tables.countriesStatisticsTable.baseTableRow.tableName
    val query =
      sql"""
        |SELECT "gdp_ppp_diffs"."country_id",  SUM("gdp_ppp_diffs"."diff") "gdp_ppp_growth" FROM
        |(
        |	SELECT "gdp_ppp" - (LAG("gdp_ppp") OVER (PARTITION BY "country_id" ORDER BY "year")) AS "diff",
        |	"country_id", "year", "gdp_ppp" FROM "#$tableName" "cs" WHERE "cs"."country_id" IN		
        |	(
        |		SELECT "pop_growth"."country_id" FROM
        |		(
        |		  SELECT SUM("population_diffs"."diff") "population_growth", "population_diffs"."country_id" "country_id" FROM 
        |			(
        |			  SELECT 
        |			    "population" - (LAG("population") OVER (PARTITION BY "country_id" ORDER BY "year")) AS "diff", 	
        |				"country_id", "year", "population" FROM "#$tableName"
        |			)  "population_diffs" 
        |			
        |			GROUP BY "population_diffs"."country_id" 
        |			ORDER BY "population_growth" DESC
        |			LIMIT $withinTopNByPopulationGrowth 
        |		) "pop_growth"
        |	)		
        |) "gdp_ppp_diffs"	
        |GROUP BY "gdp_ppp_diffs"."country_id" 
        |ORDER BY "gdp_ppp_growth" DESC
        |LIMIT $topNByGdpGrowth
        |""".stripMargin.as[(String, BigDecimal)]

    for {
      countries <- getAllCountries()
      populationGrowth <- db
        .run(query)
        .map(_.map {
          case (countryIdRaw, growth) =>
            val countryId = CountryId(countryIdRaw)
            GdpGrowth(
              countryId,
              countries.get(countryId).map(_.name).getOrElse(""),
              growth
            )
        }.toList)
    } yield {
      populationGrowth
    }
  }

  def getAndDisplayTopCountriesByGdpGrowth(
      topNByGdpGrowth: Int,
      withinTopNByPopulationGrowth: Int
  ): Future[Boolean] =
    getTopCountriesByGdpGrowth(topNByGdpGrowth, withinTopNByPopulationGrowth)
      .map { gdpGrowth =>
        println(
          s"Top $topNByGdpGrowth countries by GDP/PPP growth with top $withinTopNByPopulationGrowth by population growth from ${statisticsConfig.fromYear} to ${statisticsConfig.toYear}"
        )
        val formatter = java.text.NumberFormat.getIntegerInstance
        val dataToDisplay =
          Seq(
            Seq("Country Id", "Country Name", "GDP/PPP Growth")
          ) ++ gdpGrowth.map { p =>
            Seq(p.countryId.value, p.countryName, formatter.format(p.growth))
          }
        println(Tabulator.format(dataToDisplay))
        true
      }

  def getAllCountries(): Future[MapView[CountryId, Country]] =
    countryDao.findAll().map(_.groupBy(_.id).view.mapValues(_.head))

}

object StatisticsQueryService {
  case class PopulationGrowth(countryId: CountryId, countryName: String, growth: Long)

  case class GdpGrowth(countryId: CountryId, countryName: String, growth: BigDecimal)
}
