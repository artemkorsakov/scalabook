package ru.scalabook.site

import cats.effect.*
import laika.api.*
import laika.format.*
import laika.io.api.TreeTransformer
import laika.io.implicits.*
import laika.markdown.github.GitHubFlavor
import laika.parse.code.SyntaxHighlighting

object GenerateSite extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    createTransformer[IO]
      .use { transformer =>
        transformer
          .fromDirectory("docs")
          .toDirectory("generated")
          .transform
      }
      .as(ExitCode.Success)

  private def createTransformer[F[_]: Async]: Resource[F, TreeTransformer[F]] =
    Transformer
      .from(Markdown)
      .to(HTML)
      .using(GitHubFlavor, SyntaxHighlighting)
      .parallel[F]
      .withTheme(HeliumThemes.mainTheme.build)
      .build
