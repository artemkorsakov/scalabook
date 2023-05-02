import Dependencies.*
import laika.ast.*
import laika.ast.Path.*
import laika.helium.config.*
import laika.theme.config.*

ThisBuild / organization     := "ru.scalabook"
ThisBuild / organizationName := "Artem Korsakov"
ThisBuild / startYear        := Some(2022)
ThisBuild / licenses         := Seq(License.Apache2)
ThisBuild / developers := List(
  tlGitHubDev("artemkorsakov", "Artem Korsakov")
)

ThisBuild / githubWorkflowPublishTargetBranches := Seq()

ThisBuild / tlSitePublishBranch := Some("master")

ThisBuild / scalaVersion      := Scala
ThisBuild / semanticdbEnabled := true

ThisBuild / scalacOptions ++=
  Seq(
    "-deprecation",
    "-explain",
    "-Wunused:imports",
    "-unchecked",
    "-Xfatal-warnings",
    "-feature"
  )

lazy val root = (project in file("."))
  .aggregate(examples)
  .settings(
    name           := "scalabook",
    publish / skip := true
  )

lazy val examples = (project in file("examples"))
  .settings(name := "scalabook-examples")
  .settings(
    libraryDependencies ++=
      Seq(
        cats.core,
        cats.effect,
        cats.free,
        http4s.blaze,
        http4s.dsl,
        iron.iron,
        iron.cats,
        refined.cats,
        refined.core,
        scalaz.core,
        scalaz.effect,
        scalaz.iteratee
      ),
    libraryDependencies ++=
      Seq(
        cats.testkit,
        munit.core,
        munit.catsEffect3,
        munit.scalacheck,
        scalaz.scalacheck
      ).map(_ % Test)
  )

lazy val docs = project
  .in(file("site"))
  .settings(
    name    := "scalabook",
    version := "0.1",
    tlSiteHelium :=
      tlSiteHelium.value.all
        .metadata(
          title = Some("Scalabook"),
          description = Some("Функциональная разработка на Scala"),
          identifier = Some("Scalabook"),
          authors = Seq("Artem Korsakov"),
          language = Some("ru"),
          version = Some("0.1.0")
        )
        .all
        .themeColors(
          primary = Color.hex("007c99"),
          secondary = Color.hex("2A8D9C"),
          primaryMedium = Color.hex("a7d4de"),
          primaryLight = Color.hex("ebf6f7"),
          text = Color.hex("000000"),
          background = Color.hex("ffffff"),
          bgGradient = (Color.hex("095269"), Color.hex("007c99"))
        )
        .site
        .topNavigationBar(
          homeLink = IconLink.internal(Root / "index.md", HeliumIcon.home),
          navLinks = Seq(
            IconLink.external(
              url = "https://gitflic.ru/project/artemkorsakov/scalabook",
              icon = HeliumIcon.edit
            )
          )
        )
        .site
        .mainNavigation(
          depth = 4,
          includePageSections = false,
          prependLinks = Seq(),
          appendLinks = Seq()
        )
        .site
        .autoLinkCSS(Root / "css" / "sidebar.css")
        .site
        .pageNavigation(
          enabled = true,
          depth = 1,
          sourceBaseURL = Some(
            "https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=docs"
          ),
          sourceLinkText = "Редактировать страницу"
        )
        .site
        .footer(Text(""))
//        .site
//        .downloadPage(
//          title = "Книга в EPUB",
//          description = Some(
//            "На этой странице находятся ссылки для скачивания рабочей тетради в формате PDF и EPUB."
//          ),
//          includePDF = false
//        )
  )
  .enablePlugins(TypelevelSitePlugin)
