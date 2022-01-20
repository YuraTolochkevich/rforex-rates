package forex.services.rates.interpreters

import cats.effect.Sync
import forex.config.OneFrameConfig
import forex.domain.Rate
import forex.services.rates.OneFrameHttpAlgebra
import forex.services.rates.errors.Error
import org.http4s.client.Client
import org.http4s.{Header, Query, Request}
import cats.syntax.functor._
import cats.syntax.applicativeError._
import cats.syntax.either._
import forex.domain.Rate.Pair
import forex.services.rates.errors.Error.OneFrameLookupFailed
import forex.services.rates.OneFrameProtocol._
import forex.http._

class OneFrameHttp[F[_]: Sync](config: OneFrameConfig, http: Client[F])
  extends OneFrameHttpAlgebra[F] {

  def getButch(ratePairs:  List[Pair]): F[Error Either List[Rate]] = {
    //need to query the whole butch of currencies to satisfy One Frame limit conditions
    // 1000 requests per day => one request per 86 seconds

    val q = Query.fromString(ratePairs.map(p=>s"pair=${p.from}${p.to}").mkString("&"))
    val req = Request[F]()
      .withHeaders(Header("token", config.token))
      .withUri(config.uri.copy(query = q, path = "/rates"))
    http.expect[List[OneFrameResponse]](req).attempt.map {
      case Right(xs) =>
        Either.cond(
          xs.nonEmpty,
          xs.map(r => Rate(Rate.Pair(r.from, r.to), price = r.price, timestamp = r.time_stamp)),
          OneFrameLookupFailed("Empty exchange rates")
        )
      case Left(_) => OneFrameLookupFailed("Exchanges rates are unavailable now").asLeft
    }
  }
}
