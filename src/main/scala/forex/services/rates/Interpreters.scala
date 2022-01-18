package forex.services.rates

import cats.effect.{Clock, Sync}
import forex.config.ApplicationConfig
import forex.services.rates.interpreters._
import org.http4s.client.Client

object Interpreters {
  def oneFrame[F[_]: Clock: Sync](config: ApplicationConfig,
                    http: Client[F],
                    cacheService: CacheService[F]): Algebra[F] = new OneFrame[F](config, http,cacheService)
}
