package forex.services.rates

import cats.effect.Sync
import forex.domain.{ Currency, Price, Timestamp }
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf
import forex.http._

object OneFrameProtocol {

  case class OneFrameResponse(from: Currency, to: Currency, price: Price, time_stamp: Timestamp)

  implicit val rateDecoder: Decoder[OneFrameResponse] =
    deriveDecoder[OneFrameResponse]
  implicit def rateEncoder[F[_]: Sync]: EntityDecoder[F, List[OneFrameResponse]] =
    jsonOf[F, List[OneFrameResponse]]
}
