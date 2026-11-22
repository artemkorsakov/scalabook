package ru.scalabook.site.syntax

import laika.ast.Path.*
import laika.helium.Helium
import laika.helium.config.*
import laika.theme.config.*

object HeliumThemes:

  enum Language(
      val code: String,
      val description: String,
      val switchLabel: String,
      val fpLabel: String,
      val typeclassLabel: String,
      val algorithmsLabel: String,
      val dataStructuresLabel: String,
      val catsEffectLabel: String,
      val libsLabel: String,
      val practiceLabel: String,
      val translationsLabel: String,
      val extraLabel: String,
      val editPageLabel: String
  ):
    case Russian
        extends Language(
          code = "ru",
          description = "Функциональная разработка на Scala",
          switchLabel = "English",
          fpLabel = "Функ. программирование",
          typeclassLabel = "Классы типов",
          algorithmsLabel = "Алгоритмы",
          dataStructuresLabel = "Структуры данных",
          catsEffectLabel = "Cats effect",
          libsLabel = "Инструментарий",
          practiceLabel = "Практикум",
          translationsLabel = "Переводы",
          extraLabel = "Ресурсы",
          editPageLabel = "Редактировать страницу"
        )
    case English
        extends Language(
          code = "en",
          description = "Functional development in Scala",
          switchLabel = "Русский",
          fpLabel = "Functional programming",
          typeclassLabel = "Type classes",
          algorithmsLabel = "Algorithms",
          dataStructuresLabel = "Data structures",
          catsEffectLabel = "Cats effect",
          libsLabel = "Tooling",
          practiceLabel = "Practicum",
          translationsLabel = "Translations",
          extraLabel = "Resources",
          editPageLabel = "Edit this page"
        )
  end Language

  def mainTheme(language: Language): Helium =
    addAll(language)(Helium.defaults)

  private def addAll(language: Language): Helium => Helium =
    addMeta(language) andThen
      addThemeColors andThen
      addFavIcons andThen
      addCSSJS andThen
      addTopNavigation(language) andThen
      addMainNavigation andThen
      addPageNavigation(language) andThen
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
      .internalCSS(Root / "css" / "language-switch.css")
      .site
      .internalCSS(Root / "css" / "notes.css")
      .site
      .internalJS(Root / "js" / "tex-mml-chtml.js")

  private def addTopNavigation(language: Language)(theme: Helium): Helium =
    theme.site
      .topNavigationBar(
        homeLink = IconLink.internal(Root / "index.md", HeliumIcon.home),
        navLinks = Seq(
          ButtonLink.internal(Root / "fp/index.md", language.fpLabel),
          ButtonLink.internal(
            Root / "typeclass/index.md",
            language.typeclassLabel
          ),
          ButtonLink.internal(
            Root / "algorithms/index.md",
            language.algorithmsLabel
          ),
          ButtonLink.internal(
            Root / "data-structures/index.md",
            language.dataStructuresLabel
          ),
          ButtonLink.internal(
            Root / "cats-effect/index.md",
            language.catsEffectLabel
          ),
          ButtonLink.internal(Root / "libs/index.md", language.libsLabel),
          ButtonLink.internal(
            Root / "practice/index.md",
            language.practiceLabel
          ),
          ButtonLink.internal(
            Root / "translations/index.md",
            language.translationsLabel
          ),
          ButtonLink.internal(Root / "extra/index.md", language.extraLabel)
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

  private def addPageNavigation(language: Language)(theme: Helium): Helium =
    theme.site
      .pageNavigation(
        enabled = true,
        depth = 1,
        sourceBaseURL = Some(
          "https://gitverse.ru/artemkorsakov/scalabook/edit/master/docs/"
        ),
        sourceLinkText = language.editPageLabel,
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
