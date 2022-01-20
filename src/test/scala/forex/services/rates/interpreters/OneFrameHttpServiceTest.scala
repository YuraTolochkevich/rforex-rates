package forex.services.rates.interpreters

import java.time.OffsetDateTime

import cats.effect.IO
import forex.config.OneFrameConfig
import forex.domain.{Currency, Price, Rate, Timestamp}
import io.circe.Json
import org.http4s.{HttpRoutes, Request, Response, Uri}
import org.http4s.client.Client
import org.http4s.implicits._
import org.http4s.circe._
import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import io.circe.parser._

class OneFrameHttpServiceTest
    extends AnyWordSpec
    with Matchers
    with EitherValues {

  private def client[T](r: Request[IO] => T, body: Json) =
    Client.fromHttpApp(
      HttpRoutes
        .of[IO] {
          case req =>
            r(req)
            IO.pure(Response[IO]().withEntity(body))
        }
        .orNotFound
    )
  val conf = OneFrameConfig("qwe12", Uri.unsafeFromString("localhost://8080"))

  "get batch" in {
    val rateUsdJpy = Rate(
      Rate.Pair(Currency.USD, Currency.JPY),
      Price(0.4144514665455052),
      Timestamp(OffsetDateTime.parse("2022-01-20T15:41:46.637Z"))
    )
    val rateJpyUsd = Rate(
      Rate.Pair(Currency.JPY, Currency.USD),
      Price(0.47532294085094196),
      Timestamp(OffsetDateTime.parse("2022-01-20T15:41:46.637Z"))
    )
    val expectedUri =
      Uri.unsafeFromString("localhost://8080/rates?pair=USDJPY&pair=JPYUSD")
    val body =
      parse("""
          [
  {
    "from": "USD",
    "to": "JPY",
    "bid": 0.022850736094369384,
    "ask": 0.806052196996641,
    "price": 0.4144514665455052,
    "time_stamp": "2022-01-20T15:41:46.637Z"
  },
  {
    "from": "JPY",
    "to": "USD",
    "bid": 0.04117121598043749,
    "ask": 0.9094746657214464,
    "price": 0.47532294085094196,
    "time_stamp": "2022-01-20T15:41:46.637Z"
  }
]
          """).getOrElse(Json.Null)
    val mockedClient = client(req => req.uri shouldBe expectedUri, body)
    val pairs = List(rateUsdJpy, rateJpyUsd)
    val res = new OneFrameHttp[IO](conf, mockedClient)
      .getButch(pairs.map(_.pair))
      .unsafeRunSync()
    res shouldBe Right(pairs.toList)
  }

  "incorrect currencies pair" in {
    val rateJpyJpy =
      Rate(Rate.Pair(Currency.JPY, Currency.JPY),
           Price(0.47532294085094196),
           Timestamp(OffsetDateTime.parse("2022-01-20T15:41:46.637Z")))

    val expectedUri =
      Uri.unsafeFromString("localhost://8080/rates?pair=USDJPY&pair=JPYUSD")
    val mockedClient = client(req => req.uri shouldBe expectedUri, Json.Null)
    val pairs = List(rateJpyJpy)
    val res = new OneFrameHttp[IO](conf, mockedClient)
      .getButch(pairs.map(_.pair))
      .unsafeRunSync()
    res should matchPattern { case Left(_) => }
  }
}
