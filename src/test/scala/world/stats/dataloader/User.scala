package world.stats.dataloader

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }

case class User(username: String, email: String)

object User {
  implicit lazy val encoder: Encoder[User] = deriveEncoder[User]
  implicit lazy val decoder: Decoder[User] = deriveDecoder[User]
}
