package ru.scalabook.site.app

import cats.effect.*
import laika.api.*
import laika.format.*
import laika.io.api.TreeTransformer
import laika.io.implicits.*
import laika.io.model.*
import laika.markdown.github.GitHubFlavor
import laika.parse.code.SyntaxHighlighting
import ru.scalabook.site.theme.HeliumThemes

object GenerateSite extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    createMain().as(ExitCode.Success)

  private def createMain(): IO[RenderedTreeRoot[IO]] =
    createTransformer[IO]()
      .use:
        _.fromDirectory("docs")
          .toDirectory("generated")
          .transform

  private def createTransformer[F[_]: Async]()
      : Resource[F, TreeTransformer[F]] =
    Transformer
      .from(Markdown)
      .to(HTML)
      .using(GitHubFlavor, SyntaxHighlighting)
      .parallel[F]
      .withTheme(HeliumThemes.mainTheme.build)
      .build
