package ru.scalabook.site.app

import cats.effect.*
import laika.api.*
import laika.format.*
import laika.io.implicits.*
import laika.io.model.InputTree
import laika.markdown.github.GitHubFlavor
import laika.parse.code.SyntaxHighlighting
import laika.preview.ServerBuilder
import ru.scalabook.site.theme.HeliumThemes

object PreviewSite extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    val parser = MarkupParser
      .of(Markdown)
      .using(GitHubFlavor, SyntaxHighlighting)
      .parallel[IO]
      .withTheme(HeliumThemes.mainTheme.build)
      .build

    val inputs = InputTree[IO]
      .addDirectory("docs")

    ServerBuilder[IO](parser, inputs).build
      .use(_ => IO.never)
