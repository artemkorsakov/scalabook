package ru.scalabook.site.syntax

import laika.ast.*
import laika.ast.Path.*
import laika.helium.Helium
import laika.helium.config.*
import laika.theme.config.*

import scala.annotation.unused

object HeliumThemes:
  lazy val mainTheme: Helium =
    addAll(Helium.defaults)

  private lazy val addAll: Helium => Helium =
    addMeta andThen
      addThemeColors andThen
      addFavIcons andThen
      addCSSJS andThen
      addTopNavigation andThen
      addMainNavigation andThen
      addPageNavigation andThen
      addFooter

  private def addMeta(theme: Helium): Helium =
    theme.all
      .metadata(
        title = Some("Scalabook"),
        description = Some("Функциональная разработка на Scala"),
        identifier = Some("Scalabook"),
        authors = Seq("Artem Korsakov"),
        language = Some("ru"),
        version = Some("0.1.0")
      )

  private def addThemeColors(theme: Helium): Helium =
    theme.all
      .themeColors(
        primary = Color.hex("007c99"),
        secondary = Color.hex("2A8D9C"),
        primaryMedium = Color.hex("a7d4de"),
        primaryLight = Color.hex("ebf6f7"),
        text = Color.hex("000000"),
        background = Color.hex("ffffff"),
        bgGradient = (Color.hex("095269"), Color.hex("007c99"))
      )

  private def addFavIcons(theme: Helium): Helium =
    theme.site.favIcons(
      Favicon.internal(Root / "icons" / "favicon32x32.png", sizes = "32x32"),
      Favicon.internal(Root / "icons" / "favicon64x64.png", sizes = "64x64")
    )

  private def addCSSJS(theme: Helium): Helium =
    theme.site
      .internalCSS(Root / "css" / "sidebar.css")
      .site
      .internalJS(Root / "js" / "tex-mml-chtml.js")

  private def addTopNavigation(theme: Helium): Helium =
    theme.site
      .topNavigationBar(
        homeLink = IconLink.internal(Root / "index.md", HeliumIcon.home),
        navLinks = Seq(
          ButtonLink.internal(Root / "fp/index.md", "Функ. программирование"),
          ButtonLink.internal(Root / "typeclass/index.md", "Типовые классы"),
          ButtonLink.internal(Root / "algorithms/index.md", "Алгоритмы"),
          ButtonLink.internal(Root / "libs/index.md", "Библиотеки"),
          ButtonLink.internal(Root / "books/index.md", "Упражнения"),
          ButtonLink.internal(Root / "extra/index.md", "Ресурсы"),
        )
      )

  private def addMainNavigation(theme: Helium): Helium =
    theme.site
      .mainNavigation(
        depth = 4,
        includePageSections = false,
        prependLinks = Seq(),
        appendLinks = Seq()
      )

  private def addPageNavigation(theme: Helium): Helium =
    theme.site
      .pageNavigation(
        enabled = true,
        depth = 1,
        sourceBaseURL = Some(
          "https://gitverse.ru/artemkorsakov/scalabook/edit/master/docs/"
        ),
        sourceLinkText = "Редактировать страницу",
        keepOnSmallScreens = false
      )

  @unused("Есть ошибка при генерации PDF, которую надо поправить")
  private def addDownloadPage(theme: Helium): Helium =
    theme.site
      .downloadPage(
        title = "Книга в EPUB",
        description = Some(
          "На этой странице находятся ссылки для скачивания рабочей тетради в формате PDF и EPUB."
        ),
        includePDF = false
      )

  private def addFooter(theme: Helium): Helium =
    theme.site.footer()
end HeliumThemes
