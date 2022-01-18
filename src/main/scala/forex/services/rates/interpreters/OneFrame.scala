package forex.services.rates.interpreters

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

import cats.data.EitherT
import cats.effect.{ Clock, Sync }
import forex.config.ApplicationConfig
import forex.domain.Rate
import forex.services.rates.{ Algebra, CacheService }
import forex.services.rates.errors.Error

import org.http4s.client.Client
import org.http4s.{ Header, Query, Request }
import cats.syntax.flatMap._
import cats.syntax.applicative._
import cats.syntax.functor._
import cats.syntax.applicativeError._
import cats.syntax.either._
import forex.services.rates.errors.Error.OneFrameLookupFailed
import forex.services.rates.OneFrameProtocol._
import forex.http._

class OneFrame[F[_]: Sync: Clock](config: ApplicationConfig, http: Client[F], cacheService: CacheService[F])
    extends Algebra[F] {

  val DefaultRatesTimeOutInSeconds = 300L

  private def executeAndUpdateCache(pair: Rate.Pair) = {
    val s = for {
      rate <- EitherT(executeOneFrameRequest(pair))
      _ <- EitherT(cacheService.updatePairRate(pair, rate))
    } yield rate
    s.value
  }

  private def checkAndUpdateCache(pair: Rate.Pair, cachedRate: Option[Rate]) =
    cachedRate.fold(executeAndUpdateCache(pair)) { r =>
      {
        Clock[F].realTime(TimeUnit.MILLISECONDS).flatMap { current =>
          {
            if (r.timestamp.value.toInstant.isAfter(Instant.ofEpochMilli(current).minus(DefaultRatesTimeOutInSeconds, ChronoUnit.SECONDS)))
              r.asRight[Error].pure[F]
            else {
              executeAndUpdateCache(pair)
            }
          }
        }

      }
    }

  override def get(pair: Rate.Pair): F[Error Either Rate] = {
    val s = for {
      cachedRate <- EitherT(cacheService.getPairRate(pair))
      resRate <- EitherT(checkAndUpdateCache(pair, cachedRate))
    } yield resRate
    s.value

  }

  def executeOneFrameRequest(pair: Rate.Pair): F[Error Either Rate] = {

    val q = Query.fromString(s"pair=${pair.from}${pair.to}")
    val req = Request[F]()
      .withHeaders(Header("token", config.oneFrame.token))
      .withUri(config.oneFrame.uri.copy(query = q, path = "/rates"))
    http.expect[List[OneFrameResponse]](req).attempt.map {
      case Right(xs) =>
        Either.cond(
          xs.nonEmpty,
          xs.map(r => Rate(Rate.Pair(r.from, r.to), price = r.price, timestamp = r.time_stamp)).head,
          OneFrameLookupFailed("Exchanges rates are unavailable now ")
        )
      case Left(_) => OneFrameLookupFailed("Exchanges rates are unavailable now ").asLeft
    }
  }
}
