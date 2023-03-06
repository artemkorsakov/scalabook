import Dependencies._
import laika.ast._
import laika.ast.Path._
import laika.helium.config._

ThisBuild / organization     := "ru.gitflic.artemkorsakov"
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
    "-feature",
    "-language:implicitConversions",
    "-unchecked",
    "-Xfatal-warnings",
    "-Ykind-projector"
  )

lazy val root = (project in file("."))
  .aggregate(examples)
  .settings(
    name           := "scalabook",
    publish / skip := true
  )

lazy val examples = (project in file("examples"))
  .settings(name := "scalabook-examples")
  .settings(commonSettings)
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
        scalaz.iteratee,
        spark.core,
        spark.sql
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

lazy val commonSettings =
  Seq(
    Compile / console / scalacOptions --= Seq(
      "-Wunused:_",
      "-Xfatal-warnings"
    ),
    Test / console / scalacOptions :=
      (Compile / console / scalacOptions).value
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
        .site
        .topNavigationBar(
          homeLink = IconLink.internal(Root / "index.md", HeliumIcon.home),
          navLinks = Seq(
            ButtonLink.external(
              "https://scalabook.gitflic.space",
              "Scalabook на gitflic"
            ),
            ButtonLink
              .external("https://github.com/artemkorsakov/scalabook", "Github"),
            ButtonLink.external(
              "https://gitflic.ru/project/artemkorsakov/scalabook",
              "Gitflic"
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
        .pageNavigation(
          enabled = true,
          depth = 3,
          sourceBaseURL =
            Some("https://github.com/artemkorsakov/scalabook/blob/master/docs"),
          sourceLinkText = "Редактировать страницу"
        )
        .site
        .footer(Text(""))
//        .site
//        .downloadPage(
//          title = "Книга в PDF и EPUB",
//          description = Some(
//            "На этой странице находятся ссылки для скачивания рабочей тетради в формате PDF и EPUB."
//          )
//        )
  )
  .enablePlugins(TypelevelSitePlugin)
