package libs.http4s

import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits.*
import org.http4s.server.Router

object PingApp extends IOApp:

  private val httpApp = Router(
    "/" -> PingApi.routes
  ).orNotFound

  override def run(args: List[String]): IO[ExitCode] =
    stream(args).compile.drain.as(ExitCode.Success)

  private def stream(args: List[String]): fs2.Stream[IO, ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8000, "0.0.0.0")
      .withHttpApp(httpApp)
      .serve
