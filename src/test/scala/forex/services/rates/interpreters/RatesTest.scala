package forex.services.rates.interpreters

import java.time.OffsetDateTime

import cats.effect.concurrent.Ref
import cats.effect.{Clock, IO}
import forex.domain.{Currency, Price, Rate, Timestamp}
import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec


class RatesTest extends AnyWordSpec with Matchers with EitherValues {
  implicit val clock: Clock[IO] = Clock.create[IO]



  "get valid " in {
    val rate =Rate(
      Rate.Pair(Currency.JPY, Currency.USD),
      Price(0.47532294085094196),
      Timestamp.now)

    val test = for {
      cache <- Ref.of[IO, Map[Rate.Pair, Rate]](Map((rate.pair -> rate)))
       cacheService = new LocalCacheService(cache)
      res <- new RateService[IO](cacheService).get(rate.pair)
      _ = res shouldBe  Right(rate)
    } yield ()
    test.unsafeRunSync()
  }

  "get expired" in {
    val rate =Rate(
      Rate.Pair(Currency.JPY, Currency.USD),
      Price(0.47532294085094196),
      Timestamp(OffsetDateTime.parse("2022-01-20T15:41:46.637Z")))
    val test = for {
      cache <- Ref.of[IO, Map[Rate.Pair, Rate]](
                Map(
                  rate.pair-> rate)
                )

      cacheService = new LocalCacheService(cache)
      res <- new RateService[IO](cacheService).get(rate.pair)
      _ = res should matchPattern { case Left(_) => }
    } yield ()
    test.unsafeRunSync()
  }

}
