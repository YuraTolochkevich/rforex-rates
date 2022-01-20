package forex.services.rates

object errors {

  sealed trait Error extends Throwable
  object Error {
    final case class OneFrameLookupFailed(msg: String) extends Error
  }
}
