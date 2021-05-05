package world.stats.dataloader

import world.stats.config.WorldBackConfig
import world.stats.dataloader.WorldBankApi.{ decodeResponse, getResponseJsonBody }
import io.circe
import io.circe.{ Decoder, Json }
import sttp.client3.circe._
import sttp.client3.{ Response, _ }
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

class WorldBankApi(
    worldBackConfig: WorldBackConfig,
    sttpBackend: SttpBackend[Future, Any]
)(implicit ec: ExecutionContext) {

  /**
    * Performs GET request against worldbank API and decodes the response to type T on success.
    * @param path - relative path of the endpont, should start from `/`
    */
  def get[T: Decoder](
      path: String
  ): Future[T] = {
    val absoluteUrl = s"${worldBackConfig.apiBaseUrl}$path"
    val request = basicRequest
      .get(uri"$absoluteUrl")
      .response(asJson[T])

    for {
      response      <- request.send(sttpBackend)
      responseTyped <- Future.fromTry(decodeResponse(response))
    } yield {
      responseTyped
    }
  }

  /**
    * Performs GET request against worldbank API and returns json body on success.
    * @param path - relative path of the endpont, should start from `/`
    */
  def getAsJson(
      path: String
  ): Future[Json] = {
    val absoluteUrl = s"${worldBackConfig.apiBaseUrl}$path"
    val request = basicRequest
      .get(uri"$absoluteUrl")
      .response(asJson[Json])

    for {
      response <- request.send(sttpBackend)
      jsonBody <- Future.fromTry(getResponseJsonBody(response))
    } yield {
      jsonBody
    }
  }
}

object WorldBankApi {
  def decodeResponse[T: Decoder](
      response: Response[Either[ResponseException[String, circe.Error], T]]
  ): Try[T] =
    response.body match {
      case Left(error) =>
        Failure(
          new RuntimeException(
            s"Error fetching data from worldbank API, ${error.getMessage}",
            error
          )
        )
      case Right(data) => Success(data)
    }

  def getResponseJsonBody(
      response: Response[Either[ResponseException[String, circe.Error], Json]]
  ): Try[Json] =
    response.body match {
      case Left(error) =>
        Failure(
          new RuntimeException(
            s"Error fetching data from worldbank API, ${error.getMessage}",
            error
          )
        )
      case Right(data) => Success(data)
    }
}
