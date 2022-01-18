package forex.http
package rates

import cats.effect.Sync
import forex.programs.RatesProgram
import forex.programs.rates.Protocol.GetRatesRequest
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

class RatesHttpRoutes[F[_]: Sync](rates: RatesProgram[F]) extends Http4sDsl[F] {

  import Converters._, QueryParams._, Protocol._

  private[http] val prefixPath = "/rates"
  import cats.implicits._
  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? FromQueryParam(from) +& ToQueryParam(to) => {

      val s = (from, to).mapN(GetRatesRequest)
      s.fold(
        _ => BadRequest("Not supported  currencies pairs"),
        r =>
          rates.get(r).flatMap {
            case Right(r)  => Ok(r.asGetApiResponse)
            case Left(err) => ServiceUnavailable(err.msg)
        }
      )
    }
  }
  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
