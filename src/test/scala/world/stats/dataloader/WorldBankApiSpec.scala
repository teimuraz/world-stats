package world.stats.dataloader

import world.stats.BaseSpec
import world.stats.dataloader.WorldBankApi._
import io.circe
import io.circe.Json
import sttp.client3.{ HttpError, Response, ResponseException }
import sttp.model.StatusCode
import scala.util.Success
import io.circe.parser._

class WorldBankApiSpec extends BaseSpec {
  "decodeResponse" - {
    "when failed response" - {
      "should return failure" in {
        val responseBody: Either[ResponseException[String, circe.Error], User] =
          Left(HttpError("Some error", StatusCode(404)))

        val failedResponse
            : Response[Either[ResponseException[String, circe.Error], User]] =
          Response(responseBody, StatusCode(404))

        decodeResponse[User](failedResponse).failure.exception.getMessage should include(
          "Error fetching data from worldbank API"
        )
      }
    }
    "when successful response" - {
      "should decode it into appropriate type" in {
        val user = User("ragnar", "ragnarok@kattegat.vh")
        val responseBody: Either[ResponseException[String, circe.Error], User] =
          Right(user)

        val response: Response[Either[ResponseException[String, circe.Error], User]] =
          Response(responseBody, StatusCode(200))

        decodeResponse[User](response).success should be(
          Success(user)
        )
      }
    }
  }

  "getResponseJsonBody" - {
    "when failed response" - {
      "should return failure" in {
        val responseBody: Either[ResponseException[String, circe.Error], Json] =
          Left(HttpError("Some error", StatusCode(404)))

        val failedResponse
            : Response[Either[ResponseException[String, circe.Error], Json]] =
          Response(responseBody, StatusCode(404))

        getResponseJsonBody(failedResponse).failure.exception.getMessage should include(
          "Error fetching data from worldbank API"
        )
      }
    }
    "when successful response" - {
      "should get json body" in {
        val json =
          parse("""{"username": "ragnar", "email": "ragnar@kattegat.vh"}""").value

        val responseBody: Either[ResponseException[String, circe.Error], Json] =
          Right(json)

        val response: Response[Either[ResponseException[String, circe.Error], Json]] =
          Response(responseBody, StatusCode(200))

        getResponseJsonBody(response).success should be(
          Success(json)
        )
      }
    }
  }

}
