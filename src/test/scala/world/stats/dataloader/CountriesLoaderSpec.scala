package world.stats.dataloader

import world.stats.dataloader.CountriesLoader.getCountriesOnly
import world.stats.BaseSpec
import world.stats.model.Country
import world.stats.model.Country.CountryId

class CountriesLoaderSpec extends BaseSpec {

  "getCountriesOnly" - {
    "should return only countries" in {
      val items = Seq(
        Country(CountryId("AL"), "Albania", "Tirane"),
        Country(CountryId("AF"), "Africa", ""),
        Country(CountryId("AR"), "Argentina", "Buenos Aires")
      )

      getCountriesOnly(items).map(_.id.value) should contain theSameElementsAs (Seq(
        "AL",
        "AR"
      ))
    }
  }
}
