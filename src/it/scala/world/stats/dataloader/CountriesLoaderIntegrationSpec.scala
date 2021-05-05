package world.stats.dataloader

import world.stats.BaseIntegrationSpec
import sttp.client3.testing._

class CountriesLoaderIntegrationSpec extends BaseIntegrationSpec {

  val response =
    """
        [
          {
            "page": 1,
            "pages": 1,
            "per_page": "500",
            "total": 297
          },
          [
            {
              "id": "ALB",
              "iso2Code": "AL",
              "name": "Albania",
              "region": {
                "id": "ECS",
                "iso2code": "Z7",
                "value": "Europe & Central Asia"
              },
              "adminregion": {
                "id": "ECA",
                "iso2code": "7E",
                "value": "Europe & Central Asia (excluding high income)"
              },
              "incomeLevel": {
                "id": "UMC",
                "iso2code": "XT",
                "value": "Upper middle income"
              },
              "lendingType": {
                "id": "IBD",
                "iso2code": "XF",
                "value": "IBRD"
              },
              "capitalCity": "Tirane",
              "longitude": "19.8172",
              "latitude": "41.3317"
            },
            {
              "id": "AND",
              "iso2Code": "AD",
              "name": "Andorra",
              "region": {
                "id": "ECS",
                "iso2code": "Z7",
                "value": "Europe & Central Asia"
              },
              "adminregion": {
                "id": "",
                "iso2code": "",
                "value": ""
              },
              "incomeLevel": {
                "id": "HIC",
                "iso2code": "XD",
                "value": "High income"
              },
              "lendingType": {
                "id": "LNX",
                "iso2code": "XX",
                "value": "Not classified"
              },
              "capitalCity": "Andorra la Vella",
              "longitude": "1.5218",
              "latitude": "42.5075"
            }
          ]
        ]
        """.stripMargin

  override lazy val sttpBackend = SttpBackendStub.asynchronousFuture
    .whenRequestMatches(_.uri.path.containsSlice(Seq("country", "all")))
    .thenRespond(response)

  "loadCountries" in {
    for {
      countries       <- countriesLoader.load()
      countriesFromDb <- countryDao.findAll()
    } yield {
      countries.map(_.id.value) should contain theSameElementsAs Seq("AL", "AD")
      countries should contain theSameElementsAs countriesFromDb
    }
  }
}
