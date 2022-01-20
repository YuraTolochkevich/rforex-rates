package forex

package object services {
  type RatesService[F[_]] = rates.Algebra[F]
  type OneFrameHttpService[F[_]] = rates.OneFrameHttpAlgebra[F]
  type CacheService[F[_]] = rates.CacheServiceAlgebra[F]
  type Scheduler[F[_]] = rates.SchedulerAlgebra[F]
  final val RatesServices = rates.Interpreters
}
