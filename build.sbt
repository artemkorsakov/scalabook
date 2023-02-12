import Dependencies._
import laika.ast._
import laika.ast.Path._
import laika.ast.InternalTarget
import laika.helium.Helium
import laika.helium.config.Favicon
import laika.helium.config.HeliumIcon
import laika.helium.config.IconLink

ThisBuild / organization     := "ru.gitflic.artemkorsakov"
ThisBuild / organizationName := "Artem Korsakov"
ThisBuild / startYear        := Some(2022)
ThisBuild / licenses         := Seq(License.Apache2)
ThisBuild / developers := List(
  tlGitHubDev("artemkorsakov", "Artem Korsakov")
)

ThisBuild / githubWorkflowPublishTargetBranches := Seq()

ThisBuild / tlSitePublishBranch := Some("main")

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
    tlSiteRelatedProjects := Seq(
      "Scalabook на gitflic" -> url("https://scalabook.gitflic.space/")
    ),
    tlSiteHeliumConfig := {
      tlSiteHeliumConfig.value.all
        .metadata(
          title = Some("Scalabook"),
          language = Some("ru")
        )
        .site
        .topNavigationBar(
          homeLink = IconLink.internal(Root / "index.md", HeliumIcon.home),
          navLinks = Seq(
            IconLink.external(
              "https://github.com/artemkorsakov/scalabook",
              HeliumIcon.github
            )
          )
        )
    }
  )
  .enablePlugins(TypelevelSitePlugin)
