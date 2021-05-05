package world.stats.dataloader

import world.stats.BaseSpec
import io.circe.parser._

class PaginatedResponseSpec extends BaseSpec {
  "decode" in {
    val json = parse("""
      [
        {
          "page": 1,
          "pages": 48,
          "per_page": 50,
          "total": 2376,
          "sourceid": "2",
          "sourcename": "World Development Indicators",
          "lastupdated": "2021-03-19"
        },
        [
          {
            "username": "ragnar",
            "email": "ragnar@kattegat.vh"
          },
          {
            "username": "bjorn",
            "email": "bjorn@kattegat.vh"
          }
        ]
      ]
      """.stripMargin).value

    val expectedResponse = PaginatedResponse(
      PaginationInfo(page = 1, pages = 48, perPage = 50, total = 2376),
      Seq(User("ragnar", "ragnar@kattegat.vh"), User("bjorn", "bjorn@kattegat.vh"))
    )

    json.as[PaginatedResponse[User]].value should be(expectedResponse)
  }
}
