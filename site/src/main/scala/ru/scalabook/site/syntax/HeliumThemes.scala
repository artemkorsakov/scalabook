package ru.scalabook.site.syntax

import laika.ast.Path.*
import laika.ast.Styles
import laika.helium.Helium
import laika.helium.config.*
import laika.theme.config.*

object HeliumThemes:

  enum Language(
      val code: String,
      val description: String,
      val switchLabel: String
  ):
    case Russian
        extends Language("ru", "Функциональная разработка на Scala", "English")
    case English
        extends Language("en", "Functional development in Scala", "Русский")

  def mainTheme(language: Language): Helium =
    addAll(language)(Helium.defaults)

  private def addAll(language: Language): Helium => Helium =
    addMeta(language) andThen
      addThemeColors andThen
      addFavIcons andThen
      addCSSJS andThen
      addTopNavigation(language) andThen
      addMainNavigation andThen
      addPageNavigation andThen
      addEpub andThen
      addPdf andThen
      addFooter

  private def addMeta(language: Language)(theme: Helium): Helium =
    theme.all
      .metadata(
        title = Some("Scalabook"),
        description = Some(language.description),
        identifier = Some("Scalabook"),
        authors = Seq("Artem Korsakov"),
        language = Some(language.code),
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
      .internalCSS(Root / "css" / "left-menu.css")
      .site
      .internalCSS(Root / "css" / "right-menu.css")
      .site
      .internalCSS(Root / "css" / "prev-next-pages.css")
      .site
      .internalCSS(Root / "css" / "notes.css")
      .site
      .internalJS(Root / "js" / "tex-mml-chtml.js")
      .site
      .internalJS(Root / "js" / "lang-switch.js")

  private def addTopNavigation(language: Language)(theme: Helium): Helium =
    theme.site
      .topNavigationBar(
        homeLink = IconLink.internal(Root / "index.md", HeliumIcon.home),
        navLinks = Seq(
          ButtonLink
            .external(
              "#",
              language.switchLabel,
              options = Styles("language-switch")
            ),
          ButtonLink.internal(Root / "fp/index.md", "Функ. программирование"),
          ButtonLink.internal(Root / "typeclass/index.md", "Классы типов"),
          ButtonLink.internal(Root / "algorithms/index.md", "Алгоритмы"),
          ButtonLink.internal(
            Root / "data-structures/index.md",
            "Структуры данных"
          ),
          ButtonLink.internal(Root / "cats-effect/index.md", "Cats effect"),
          ButtonLink.internal(Root / "libs/index.md", "Инструментарий"),
          ButtonLink.internal(Root / "practice/index.md", "Практикум"),
          ButtonLink.internal(Root / "translations/index.md", "Переводы"),
          ButtonLink.internal(Root / "extra/index.md", "Ресурсы")
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

  private def addEpub(theme: Helium): Helium =
    theme.epub
      .navigationDepth(4)
      .epub
      .tableOfContent("Содержание", 3)

  private def addPdf(theme: Helium): Helium =
    theme.pdf
      .navigationDepth(4)
      .pdf
      .tableOfContent("Содержание", 3)

  private def addFooter(theme: Helium): Helium =
    theme.site.footer()
end HeliumThemes
