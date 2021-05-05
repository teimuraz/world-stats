package world.stats.model

import world.stats.model.Country.CountryId
import io.circe.{ Decoder, HCursor }
import io.circe.generic.semiauto.deriveDecoder

case class Country(
    id: CountryId,
    name: String,
    capitalCity: String
)

object Country {
  // Country Id - ISO2 code in worlbank api
  case class CountryId(value: String) extends AnyVal

  object CountryId {
    implicit lazy val decoder: Decoder[CountryId] = (c: HCursor) =>
      c.as[String].map(CountryId(_))
  }

  implicit lazy val decoder: Decoder[Country] = deriveDecoder

  val tupled = (this.apply _).tupled
}
