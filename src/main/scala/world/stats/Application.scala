package world.stats

import akka.Done
import akka.actor.CoordinatedShutdown
import com.typesafe.scalalogging.Logger
import scala.concurrent.Future

object Application extends App with Components {

  private val logger = Logger(this.getClass)

  args.toList match {
    case "--dataload" :: _ =>
      execute {
        statisticsLoader.load()
      }
    case "--results" :: _ =>
      execute {
        statisticsLoader.statisticsLoaded().flatMap { statisticsLoaded =>
          if (!statisticsLoaded) {
            Future.successful(println("Please load data first"))
          } else {
            for {
              _ <- statisticsQueryService.getAndDisplayTopCountriesByPopulationGrowth(10)
              _ = println()
              _ <- statisticsQueryService.getAndDisplayTopCountriesByGdpGrowth(3, 10)
            } yield {
              true
            }
          }
        }
      }
    case _ =>
      println(
        """
          |Usage:
          |--dataload - Load data ingestion from Worldbank
          |--results  - Top countries by population, GDP/PPP growth.
          |
          |""".stripMargin
      )
  }

  def execute[T](cmd: => Future[T]): Future[AnyVal] = {
    CoordinatedShutdown(actorSystem)
      .addTask(CoordinatedShutdown.PhaseBeforeServiceUnbind, "cleanup") { () =>
        for {
          _ <- sttpBackend.close()
        } yield {
          db.close()
          Done
        }
      }

    (for {
      _ <- bootstrap()
      _ <- cmd
      _ <- actorSystem.terminate()
    } yield {
      true
    }).recover {
      case e =>
        logger.error(e.getMessage, e)
    }
  }
}
