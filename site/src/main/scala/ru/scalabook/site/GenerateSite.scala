package ru.scalabook.site

import cats.effect.*
import laika.api.*
import laika.format.*
import laika.io.implicits.*
import laika.markdown.github.GitHubFlavor
import laika.parse.code.SyntaxHighlighting

object GenerateSite extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    val transformer = Transformer
      .from(Markdown)
      .to(HTML)
      .using(GitHubFlavor, SyntaxHighlighting)
      .parallel[IO]
      .withTheme(Hellium.default.build)
      .build

    for _ <- IO.println("Hello world")
    yield ExitCode.Success

//import laika.ast.*
//import laika.ast.Path.*
//import laika.helium.Helium
//import laika.helium.config.*
//import laika.theme.config.*
//
//lazy val tlSiteHeliumSettings: Helium => Helium =
//  _.all
//    .metadata(
//      title = Some("Scalabook"),
//      description = Some("Функциональная разработка на Scala"),
//      identifier = Some("Scalabook"),
//      authors = Seq("Artem Korsakov"),
//      language = Some("ru"),
//      version = Some("0.1.0")
//    )
//    .all
//    .themeColors(
//      primary = Color.hex("007c99"),
//      secondary = Color.hex("2A8D9C"),
//      primaryMedium = Color.hex("a7d4de"),
//      primaryLight = Color.hex("ebf6f7"),
//      text = Color.hex("000000"),
//      background = Color.hex("ffffff"),
//      bgGradient = (Color.hex("095269"), Color.hex("007c99"))
//    )
//    .site
//    .topNavigationBar(
//      homeLink = IconLink.internal(Root / "index.md", HeliumIcon.home),
//      navLinks = Seq(
//        ButtonLink.internal(Root / "blog.md", "Блог"),
//        ButtonLink.internal(Root / "scala.md", "Scala"),
//        ButtonLink.internal(Root / "fp.md", "ФП"),
//        ButtonLink.internal(Root / "typeclass.md", "Теория категорий"),
//        ButtonLink.internal(Root / "algorithms.md", "Алгоритмы"),
//        ButtonLink.internal(Root / "libs.md", "Scala libs"),
//        ButtonLink.internal(Root / "patterns.md", "Паттерны"),
//        ButtonLink.internal(Root / "books.md", "Упражнения"),
//        ButtonLink.internal(Root / "extra.md", "Ресурсы")
//      )
//    )
//    .site
//    .mainNavigation(
//      depth = 4,
//      includePageSections = false,
//      prependLinks = Seq(),
//      appendLinks = Seq()
//    )
//    .site
//    .autoLinkCSS(Root / "css" / "sidebar.css")
//    .site
//    .autoLinkJS(Root / "js" / "tex-mml-chtml.js")
//    .site
//    .pageNavigation(
//      enabled = true,
//      depth = 1,
//      sourceBaseURL = Some(
//        "https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=docs"
//      ),
//      sourceLinkText = "Редактировать страницу"
//    )
//    .site
//    .footer(Text(""))
////        .site
////        .downloadPage(
////          title = "Книга в EPUB",
////          description = Some(
////            "На этой странице находятся ссылки для скачивания рабочей тетради в формате PDF и EPUB."
////          ),
////          includePDF = false
////        )
//
//lazy val docs = project
//  .in(file("site"))
//  .settings(
//    name         := "scalabook",
//    version      := "0.1",
//    tlSiteHelium := tlSiteHeliumSettings(tlSiteHelium.value)
//  )
//  .enablePlugins(TypelevelSitePlugin)
