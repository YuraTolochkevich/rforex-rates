package forex.config

import org.http4s.Uri

import scala.concurrent.duration.FiniteDuration

case class ApplicationConfig(
    http: HttpConfig,
    oneFrame: OneFrameConfig
)

case class OneFrameConfig (
    token: String,
    uri: Uri
                          )
case class HttpConfig(
    host: String,
    port: Int,
    timeout: FiniteDuration
)
