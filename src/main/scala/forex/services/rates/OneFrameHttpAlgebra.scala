package forex.services.rates

import forex.domain.Rate
import forex.domain.Rate.Pair
import forex.services.rates.errors.Error

trait OneFrameHttpAlgebra[F[_]] {
  def getButch(ratePairs:  List[Pair]): F[Error Either List[Rate]]

}
