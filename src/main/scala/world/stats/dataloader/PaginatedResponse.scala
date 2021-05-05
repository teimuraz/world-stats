package world.stats.dataloader

import io.circe.{ Decoder, HCursor }

case class PaginatedResponse[T](pagination: PaginationInfo, items: Seq[T])

object PaginatedResponse {
  implicit def decoder[T: Decoder]: Decoder[PaginatedResponse[T]] =
    (c: HCursor) =>
      for {
        paginationInfo <- c.downArray.as[PaginationInfo]
        items          <- c.downArray.delete.downArray.as[List[T]]
      } yield {
        PaginatedResponse(paginationInfo, items)
      }
}
