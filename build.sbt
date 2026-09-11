import Dependencies.*

ThisBuild / organization     := "ru.scalabook"
ThisBuild / organizationName := "Artem Korsakov"
ThisBuild / startYear        := Some(2022)
ThisBuild / licenses         := Seq(License.Apache2)

ThisBuild / scalaVersion      := Scala
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

ThisBuild / scalacOptions ++=
  Seq(
    "-deprecation",
    "-explain",
    "-Wunused:imports",
    "-unchecked",
    "-Werror",
    "-feature",
    "-Wnonunit-statement"
  )

lazy val root = (project in file("."))
  .aggregate(examples, site)
  .settings(
    name           := "scalabook",
    publish / skip := true
  )

lazy val examples = (project in file("examples"))
  .settings(name := "scalabook-examples")
  .settings(
    libraryDependencies ++= exampleLibs,
    libraryDependencies ++= exampleTestLibs
  )

lazy val site = project
  .in(file("site"))
  .settings(
    libraryDependencies ++= siteLibs
  )

import sbtwelcome._

logo :=
  raw"""
       |Welcome to
       |
       |    _____           _       _                 _
       |  / ____|         | |     | |               | |
       | | (___   ___ __ _| | __ _| |__   ___   ___ | | __
       |  \___ \ / __/ _` | |/ _` | '_ \ / _ \ / _ \| |/ /
       |  ____) | (_| (_| | | (_| | |_) | (_) | (_) |   <
       | |_____/ \___\__,_|_|\__,_|_.__/ \___/ \___/|_|\_\
       |
       |
       |Version ${version.value}
   """.stripMargin

logoColor := scala.Console.MAGENTA

usefulTasks := Seq(
  UsefulTask("com", "Compile main and test sources"),
  UsefulTask("dep", "Show available dependency updates"),
  UsefulTask("site", "Generate static site from project sources"),
  UsefulTask("checkLinks", "Validate all links in the generated site"),
  UsefulTask(
    "prePush",
    "Pre-commit quality gate: compile, test, format, check dependencies"
  ),
  UsefulTask(
    "fullCheck",
    "Full validation: compile, test, fmtcheck, check dependencies (CI-ready)"
  )
)
