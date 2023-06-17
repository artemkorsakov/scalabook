package ru.scalabook.site

import laika.ast.*
import laika.ast.Path.*
import laika.helium.Helium
import laika.helium.config.*
import laika.theme.config.*

import scala.annotation.unused

object HeliumThemes:
  private val addMeta: Helium => Helium =
    _.all
      .metadata(
        title = Some("Scalabook"),
        description = Some("Функциональная разработка на Scala"),
        identifier = Some("Scalabook"),
        authors = Seq("Artem Korsakov"),
        language = Some("ru"),
        version = Some("0.1.0")
      )

  private val addThemeColors: Helium => Helium =
    _.all
      .themeColors(
        primary = Color.hex("007c99"),
        secondary = Color.hex("2A8D9C"),
        primaryMedium = Color.hex("a7d4de"),
        primaryLight = Color.hex("ebf6f7"),
        text = Color.hex("000000"),
        background = Color.hex("ffffff"),
        bgGradient = (Color.hex("095269"), Color.hex("007c99"))
      )

  private val addCSSJS: Helium => Helium =
    _.site
      .autoLinkCSS(Root / "css" / "sidebar.css")
      .site
      .autoLinkJS(Root / "js" / "tex-mml-chtml.js")

  private val addTopNavigation: Helium => Helium =
    _.site
      .topNavigationBar(
        homeLink = IconLink.internal(Root / "index.md", HeliumIcon.home),
        navLinks = Seq(
          ButtonLink.internal(Root / "blog.md", "Блог"),
          ButtonLink.internal(Root / "scala.md", "Scala"),
          ButtonLink.internal(Root / "fp.md", "ФП"),
          ButtonLink.internal(Root / "typeclass.md", "Теория категорий"),
          ButtonLink.internal(Root / "algorithms.md", "Алгоритмы"),
          ButtonLink.internal(Root / "libs.md", "Scala libs"),
          ButtonLink.internal(Root / "patterns.md", "Паттерны"),
          ButtonLink.internal(Root / "books.md", "Упражнения"),
          ButtonLink.internal(Root / "extra.md", "Ресурсы")
        )
      )

  private val addMainNavigation: Helium => Helium =
    _.site
      .mainNavigation(
        depth = 4,
        includePageSections = false,
        prependLinks = Seq(),
        appendLinks = Seq()
      )

  private val addPageNavigation: Helium => Helium =
    _.site
      .pageNavigation(
        enabled = true,
        depth = 1,
        sourceBaseURL = Some(
          "https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=docs"
        ),
        sourceLinkText = "Редактировать страницу",
        keepOnSmallScreens = false
      )

  @unused("Есть ошибка при генерации PDF, которую надо поправить")
  private val addDownloadPage: Helium => Helium =
    _.site
      .downloadPage(
        title = "Книга в EPUB",
        description = Some(
          "На этой странице находятся ссылки для скачивания рабочей тетради в формате PDF и EPUB."
        ),
        includePDF = false
      )

  private val addFooter: Helium => Helium =
    _.site.footer(Text(""))

  private val addAll: Helium => Helium =
    addMeta andThen
      addThemeColors andThen
      addCSSJS andThen
      addTopNavigation andThen
      addMainNavigation andThen
      addPageNavigation andThen
      addFooter

  val mainTheme: Helium =
    addAll(Helium.defaults)
