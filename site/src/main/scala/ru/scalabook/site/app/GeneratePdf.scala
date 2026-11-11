package ru.scalabook.site.app

import cats.effect.*
import laika.api.*
import laika.config.SyntaxHighlighting
import laika.format.*
import laika.format.Markdown.GitHubFlavor
import laika.io.api.BinaryTreeTransformer
import laika.io.syntax.*
import ru.scalabook.site.syntax.{HeliumThemes, MyDirectives, SbtSyntax}

object GeneratePdf extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    createMain().as(ExitCode.Success)

  private def createMain(): IO[Unit] =
    for
      _ <- IO.blocking(
        java.nio.file.Files.createDirectories(
          java.nio.file.Path.of("generated")
        )
      )
      _ <- createPdfTransformer[IO]()
        .use(
          _.fromDirectory("docs").toFile("generated/scalabook.pdf").transform
        )
    yield ()

  private def createPdfTransformer[F[_]: Async]()
      : Resource[F, BinaryTreeTransformer[F]] =
    Transformer
      .from(Markdown)
      .to(PDF)
      .using(
        GitHubFlavor,
        SyntaxHighlighting.withSyntax(SbtSyntax),
        MyDirectives
      )
      .parallel[F]
      .withTheme(HeliumThemes.mainTheme.build)
      .build
end GeneratePdf
