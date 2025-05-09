package ru.scalabook.site.app

import cats.effect.*
import laika.api.*
import laika.config.SyntaxHighlighting
import laika.format.*
import laika.format.Markdown.GitHubFlavor
import laika.io.api.TreeTransformer
import laika.io.model.*
import laika.io.syntax.*
import ru.scalabook.site.syntax.{
  HeliumThemes,
  LinkValidationExtension,
  MyDirectives,
  SbtSyntax
}

object CheckLinksSite extends IOApp:
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
      .using(
        GitHubFlavor,
        SyntaxHighlighting.withSyntax(SbtSyntax),
        MyDirectives,
        LinkValidationExtension
      )
      .parallel[F]
      .withTheme(HeliumThemes.mainTheme.build)
      .build
end CheckLinksSite
