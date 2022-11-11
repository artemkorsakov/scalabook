package libs.http4s

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object PingApi extends Http4sDsl[IO]:
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "ping"     => Ok("pong\n")
  }
