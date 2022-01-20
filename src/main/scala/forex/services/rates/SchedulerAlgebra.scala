package forex.services.rates

trait SchedulerAlgebra [F[_]] {
  def getsScheduler(): fs2.Stream[F,  Either[Throwable, Unit]]
}
