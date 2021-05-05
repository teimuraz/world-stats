package world.stats

import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.{ EitherValues, TryValues }
import org.scalatest.matchers.should.Matchers

class BaseSpec extends AsyncFreeSpec with Matchers with TryValues with EitherValues
