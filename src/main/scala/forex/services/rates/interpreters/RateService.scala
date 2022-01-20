package forex.services.rates.interpreters

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

import cats.effect.{Clock, Sync}
import forex.domain.Rate
import forex.services.rates.{Algebra,  CacheServiceAlgebra}
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.either._
import forex.services.rates.errors.Error.OneFrameLookupFailed


class RateService[F[_]: Sync: Clock](cacheService: CacheServiceAlgebra[F])
    extends Algebra[F] {

  val DefaultRatesTimeOutInSeconds = 300L

  override def get(pair: Rate.Pair) = {

    Clock[F].realTime(TimeUnit.MILLISECONDS).flatMap { current =>
      cacheService.getPairRate(pair).map { eth =>
        eth.flatMap {
          case Some(p) =>
            if (p.timestamp.value.toInstant.isAfter(Instant
                  .ofEpochMilli(current)
                  .minus(DefaultRatesTimeOutInSeconds, ChronoUnit.SECONDS)))
              p.asRight
            else
              OneFrameLookupFailed("Can't get relevant currencies rates").asLeft
          case None =>
            OneFrameLookupFailed("Currency pair doesn't exist ").asLeft
        }
      }
    }
  }
}
