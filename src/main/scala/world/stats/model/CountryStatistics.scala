package world.stats.model

import world.stats.model.Country.CountryId

/**
  * Statistics of the country in given year.
  */
case class CountryStatistics(
    countryId: CountryId,
    year: Int,
    population: Long,
    gdpPpp: Option[BigDecimal]
)
