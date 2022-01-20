package forex.services.rates

import forex.domain.Rate
import forex.domain.Rate.Pair
import forex.services.rates.errors.Error

trait CacheServiceAlgebra[F[_]] {
  def getPairRate(pair: Pair): F[Error Either Option[Rate]]
  def updateButch(rates: List[Rate]): F[Error Either Unit]
}