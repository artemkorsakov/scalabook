package ru.scalabook.site.theme

import laika.ast.*
import laika.ast.Path.*
import laika.helium.Helium
import laika.helium.config.*
import laika.theme.config.*

import scala.annotation.unused

object HeliumThemes:
  def mainTheme(part: String): Helium =
    addAll(part)(Helium.defaults)

  private def addAll(part: String): Helium => Helium =
    addMeta andThen
      addThemeColors andThen
      addCSSJS andThen
      addTopNavigation andThen
      addMainNavigation andThen
      addPageNavigation(part) andThen
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

  private def addCSSJS(theme: Helium): Helium =
    theme.site
      .autoLinkJS(Root / "js" / "tex-mml-chtml.js")

  private def addTopNavigation(theme: Helium): Helium =
    theme.site
      .topNavigationBar(
        homeLink = IconLink.external("https://scalabook.ru", HeliumIcon.home),
        navLinks = Seq(
          ButtonLink.external("https://scalabook.ru/blog", "Блог"),
          ButtonLink.external("https://scalabook.ru/scala", "Scala"),
          ButtonLink.external("https://scalabook.ru/fp", "ФП"),
          ButtonLink
            .external("https://scalabook.ru/typeclass", "Теория категорий"),
          ButtonLink.external("https://scalabook.ru/algorithms", "Алгоритмы"),
          ButtonLink.external("https://scalabook.ru/libs", "Scala libs"),
          ButtonLink.external("https://scalabook.ru/patterns", "Паттерны"),
          ButtonLink.external("https://scalabook.ru/books", "Упражнения"),
          ButtonLink.external("https://scalabook.ru/extra", "Ресурсы")
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

  private def addPageNavigation(part: String)(theme: Helium): Helium =
    theme.site
      .pageNavigation(
        enabled = true,
        depth = 1,
        sourceBaseURL = Some(
          s"https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=docs$part"
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
    theme.site.footer(Text(""))
