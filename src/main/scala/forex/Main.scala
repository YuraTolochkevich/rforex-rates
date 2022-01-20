package forex

import scala.concurrent.ExecutionContext
import cats.effect._
import cats.effect.concurrent.Ref
import forex.config._
import forex.domain.Rate
import fs2.Stream
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.server.blaze.BlazeServerBuilder

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    new Application[IO]
      .stream(executionContext)
      .compile
      .drain
      .as(ExitCode.Success)

}

class Application[F[_]: ConcurrentEffect: Timer] {

  import scala.concurrent.ExecutionContext.global
  def stream(ec: ExecutionContext): Stream[F, Unit] =
    for {
      config <- Config.stream("app")
      client <- BlazeClientBuilder[F](global).stream
      c <- Stream.eval(Ref[F].of(Map[Rate.Pair, Rate]()))
      module = new Module[F](config, client, c)
      s = BlazeServerBuilder[F](ec)
        .bindHttp(config.http.port, config.http.host)
        .withHttpApp(module.httpApp)
        .serve
      _ <- fs2
        .Stream(s, module.scheduleService.getsScheduler().drain)
        .parJoinUnbounded

    } yield ()
}
