package world.stats.dataloader

import world.stats.dataloader.DataLoaderSupport.calculateNumberOfPages
import world.stats.util.FutureUtils.runSequentially
import com.typesafe.scalalogging.Logger
import io.circe.Decoder

import scala.concurrent.{ ExecutionContext, Future }

trait DataLoaderSupport {

  private val logger = Logger(this.getClass)

  def worldBankApi: WorldBankApi
  implicit def ec: ExecutionContext

  /**
    * Since the amount of statistics data can be huge, loading data in one request can overflow the memory.
    * Instead, we load data by slices (pages) sequentially.
    * Logic:
    *   - determine total number of items of given statistics (by performing request to statistics endpoint)
    *   - calculate number of pages based on total number of items and desired page size.
    *   - load pages and handle (store) items of the page, sequentially, page by page.
    */
  def loadDataByPages[T: Decoder, R](
      statisticsPath: String,
      itemsPerPage: Int
  )(handleItems: Seq[T] => Future[R]): Future[Seq[R]] = {
    logger.info(s"Loading statistics from $statisticsPath with page size $itemsPerPage")
    for {
      itemsTotal <- getItemsTotal[T](statisticsPath)
      numberOfPages = calculateNumberOfPages(itemsTotal, itemsPerPage)
      pagesFutures = Range.inclusive(1, numberOfPages).map { page => () =>
        getAndHandleItemsInPage(statisticsPath, page, itemsPerPage)(handleItems)
      }
      pagesResults <- runSequentially(pagesFutures)
    } yield {
      pagesResults
    }
  }

  private def getAndHandleItemsInPage[T: Decoder, R](
      statisticsPath: String,
      page: Int,
      itemsPerPage: Int
  )(
      handleItems: Seq[T] => Future[R]
  ): Future[R] =
    for {
      response <- worldBankApi.get[PaginatedResponse[T]](
        s"$statisticsPath&page=$page&per_page=$itemsPerPage"
      )
      result <- handleItems(response.items)
    } yield {
      result
    }

  /**
    * Get total number of items of given statistics.
    */
  def getItemsTotal[T: Decoder](statisticsPath: String): Future[Int] =
    for {
      response <- worldBankApi.get[PaginatedResponse[T]](
        s"$statisticsPath&per_page=1"
      )
    } yield {
      response.pagination.total
    }
}

object DataLoaderSupport {
  def calculateNumberOfPages(itemsTotal: Int, itemsPerPage: Int): Int =
    Math.ceil(itemsTotal.toDouble / itemsPerPage).toInt
}
