package ru.scalabook.site.app

import cats.effect.*
import laika.api.*
import laika.config.SyntaxHighlighting
import laika.format.*
import laika.format.Markdown.GitHubFlavor
import laika.io.model.*
import laika.io.syntax.*
import laika.preview.ServerBuilder
import ru.scalabook.site.syntax.HeliumThemes

object PreviewSite extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    val parser = MarkupParser
      .of(Markdown)
      .using(GitHubFlavor, SyntaxHighlighting)
      .parallel[IO]
      .withTheme(HeliumThemes.mainTheme(HeliumThemes.Language.Russian).build)
      .build

    val inputs = InputTree[IO]
      .addDirectory("docs")

    ServerBuilder[IO](parser, inputs).build
      .use(_ => IO.never)
