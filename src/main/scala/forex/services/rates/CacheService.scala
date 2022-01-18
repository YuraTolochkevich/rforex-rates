package forex.services.rates

import cats.effect.Sync
import cats.effect.concurrent.Ref
import forex.domain.Rate
import forex.domain.Rate.Pair

import cats.implicits._
import scala.collection.mutable

import forex.services.rates.errors._

trait CacheService[F[_]] {
  def getPairRate(pair: Pair): F[Error Either Option[Rate]]
  def updatePairRate(pair: Pair, rate: Rate): F[Error Either Unit]
}

class LocalCacheService[F[_]: Sync] extends CacheService[F] {

  val cache: F[Ref[F, mutable.Map[Pair, Rate]]] = Ref[F].of(mutable.Map[Rate.Pair, Rate]())

  override def getPairRate(pair: Pair): F[Either[Error, Option[Rate]]] =
    for {
      c <- cache
      s <- c.get.map(_.get(pair).asRight[Error])
    } yield s

  override def updatePairRate(pair: Pair, rate: Rate): F[Error Either Unit] =
    for {
      c <- cache
      s <- c.get.map(_.update(pair, rate).asRight[Error])
    } yield s
}
