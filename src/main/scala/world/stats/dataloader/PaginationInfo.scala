package world.stats.dataloader

import io.circe.{ Decoder, HCursor }

case class PaginationInfo(page: Int, pages: Int, perPage: Int, total: Int)

object PaginationInfo {
  implicit lazy val decoder: Decoder[PaginationInfo] = (c: HCursor) =>
    for {
      page    <- c.downField("page").as[Int]
      pages   <- c.downField("pages").as[Int]
      perPage <- c.downField("per_page").as[Int]
      total   <- c.downField("total").as[Int]
    } yield {
      PaginationInfo(page, pages, perPage, total)
    }
}
