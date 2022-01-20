package forex.services.rates

import cats.effect.concurrent.Ref
import cats.effect.{Clock, Concurrent, Sync, Timer}
import forex.config.OneFrameConfig
import forex.domain.Rate
import forex.domain.Rate.Pair
import forex.services.rates.interpreters._
import org.http4s.client.Client

object Interpreters {
  def oneFrame[F[_]: Clock: Sync](
      cacheService: CacheServiceAlgebra[F]): Algebra[F] =
    new RateService[F](cacheService)
  def oneFrameHttp[F[_]: Sync](config: OneFrameConfig,
                               http: Client[F]): OneFrameHttpAlgebra[F] =
    new OneFrameHttp[F](config, http)

  def cache[F[_]: Sync](
      cache: Ref[F, Map[Pair, Rate]]): CacheServiceAlgebra[F] =
    new LocalCacheService[F](cache)
  def scheduler[F[_]: Concurrent: Timer](
      oneFrameHttp: OneFrameHttpAlgebra[F],
      cacheService: CacheServiceAlgebra[F]): SchedulerAlgebra[F] =
    new CacheRefreshSchedulerService(oneFrameHttp, cacheService)
}
