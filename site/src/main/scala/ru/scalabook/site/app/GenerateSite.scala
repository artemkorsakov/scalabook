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
    val allResources =
      for
        _ <- createMain()
        _ <- createPath("/blog")
        _ <- createPath("/scala")
        _ <- createPath("/fp")
        _ <- createPath("/typeclass")
        _ <- createPath("/algorithms")
        _ <- createPath("/libs")
        _ <- createPath("/patterns")
        _ <- createPath("/books")
        _ <- createPath("/extra")
      yield ()

    allResources.as(ExitCode.Success)

  private def createMain(): IO[RenderedTreeRoot[IO]] =
    createTransformer[IO]("/main")
      .use:
        _.fromDirectories(
          Seq(
            FilePath.parse("docs/main"),
            FilePath.parse("docs/icons")
          )
        )
          .toDirectory("generated")
          .transform

  private def createPath(path: String): IO[RenderedTreeRoot[IO]] =
    createTransformer[IO](path)
      .use:
        _.fromDirectories(
          Seq(
            FilePath.parse(s"docs$path"),
            FilePath.parse("docs/icons"),
            FilePath.parse("docs/js")
          )
        )
          .toDirectory(s"generated$path")
          .transform

  private def createTransformer[F[_]: Async](
      part: String
  ): Resource[F, TreeTransformer[F]] =
    Transformer
      .from(Markdown)
      .to(HTML)
      .using(GitHubFlavor, SyntaxHighlighting)
      .parallel[F]
      .withTheme(HeliumThemes.mainTheme(part).build)
      .build
