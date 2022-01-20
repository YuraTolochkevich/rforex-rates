package forex.services.rates.interpreters

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.implicits._
import forex.domain.Rate
import forex.domain.Rate.Pair
import forex.services.rates.CacheServiceAlgebra
import forex.services.rates.errors._



class LocalCacheService[F[_]: Sync ](cache: Ref[F, Map[Pair, Rate]]) extends CacheServiceAlgebra[F] {


  override def getPairRate(pair: Pair): F[Either[Error, Option[Rate]]] =
    cache.get.map(_.get(pair).asRight[Error])

  override def updateButch(rates: List[Rate]): F[Error Either Unit] =
    cache.set(rates.map(r=>(r.pair, r)).toMap).map(_.asRight)
}
