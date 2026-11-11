package ru.scalabook.site.app

import cats.effect.*
import laika.api.*
import laika.config.SyntaxHighlighting
import laika.format.*
import laika.format.Markdown.GitHubFlavor
import laika.io.api.{BinaryTreeTransformer, TreeTransformer}
import laika.io.syntax.*
import ru.scalabook.site.syntax.{HeliumThemes, MyDirectives, SbtSyntax}

object GenerateSite extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    createMain().as(ExitCode.Success)

  private def createMain(): IO[Unit] =
    for
      _ <- IO.blocking(
        java.nio.file.Files.createDirectories(
          java.nio.file.Path.of("generated")
        )
      )
      _ <- createHtmlTransformer[IO]()
        .use(_.fromDirectory("docs").toDirectory("generated").transform)
      _ <- createEpubTransformer[IO]()
        .use(
          _.fromDirectory("docs").toFile("generated/scalabook.epub").transform
        )
    yield ()

  private def createHtmlTransformer[F[_]: Async]()
      : Resource[F, TreeTransformer[F]] =
    Transformer
      .from(Markdown)
      .to(HTML)
      .using(
        GitHubFlavor,
        SyntaxHighlighting.withSyntax(SbtSyntax),
        MyDirectives
      )
      .parallel[F]
      .withTheme(HeliumThemes.mainTheme.build)
      .build

  private def createEpubTransformer[F[_]: Async]()
      : Resource[F, BinaryTreeTransformer[F]] =
    Transformer
      .from(Markdown)
      .to(EPUB)
      .using(
        GitHubFlavor,
        SyntaxHighlighting.withSyntax(SbtSyntax),
        MyDirectives
      )
      .parallel[F]
      .withTheme(HeliumThemes.mainTheme.build)
      .build
end GenerateSite
