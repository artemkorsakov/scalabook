package ru.scalabook.site

import cats.effect.*
import laika.api.*
import laika.format.*
import laika.io.api.TreeTransformer
import laika.io.implicits.*
import laika.io.model.RenderedTreeRoot
import laika.markdown.github.GitHubFlavor
import laika.parse.code.SyntaxHighlighting

object GenerateSite extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    val allResources =
      for
        _ <- createMain()
        _ <- createPath("/blog")
        _ <- createPath("/scala")
        _ <- createPath("/fp")
//        _ <- createPath("/typeclass")
//        _ <- createPath("/algorithms")
//        _ <- createPath("/libs")
//        _ <- createPath("/patterns")
//        _ <- createPath("/books")
        _ <- createPath("/extra")
      yield ()

    allResources.as(ExitCode.Success)

  private def createMain(): IO[RenderedTreeRoot[IO]] =
    createTransformer[IO]("/main")
      .use:
        _.fromDirectory("docs/main")
          .toDirectory("generated")
          .transform

  private def createPath(path: String): IO[RenderedTreeRoot[IO]] =
    createTransformer[IO](path)
      .use:
        _.fromDirectory(s"docs$path")
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
