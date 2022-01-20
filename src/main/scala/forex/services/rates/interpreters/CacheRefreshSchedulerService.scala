package forex.services.rates.interpreters

import cats.data.EitherT
import cats.effect.{Sync, Timer}
import forex.services.rates.{
  CacheServiceAlgebra,
  OneFrameHttpAlgebra,
  SchedulerAlgebra
}
import forex.domain.{Currency, Rate}
import cats.implicits._

import scala.concurrent.duration._

class CacheRefreshSchedulerService[F[_]: Sync: Timer](
    oneFrameHttp: OneFrameHttpAlgebra[F],
    cacheService: CacheServiceAlgebra[F])
    extends SchedulerAlgebra[F] {

  val CacheRafreshInterval = 200.seconds

  def refreshCache = {

    val s = for {
      b <- EitherT(oneFrameHttp.getButch(getCurrencyCombinations.toList))
      s <- EitherT(cacheService.updateButch(b))
    } yield s
    s.value.rethrow

  }

  def getsScheduler() = {
    val baseStream = fs2.Stream.retry(refreshCache, 5.seconds, identity, 3).attempt
    baseStream ++ fs2.Stream.awakeEvery[F](CacheRafreshInterval) >> baseStream
  }

  private def getCurrencyCombinations = {
    Currency.values
      .combinations(2)
      .flatMap {
        case Seq(a: Currency, b: Currency) =>
          List(Rate.Pair(a, b), Rate.Pair(b, a))
      }
      .toSet
  }
}
