import Dependencies.*

ThisBuild / organization     := "ru.scalabook"
ThisBuild / organizationName := "Artem Korsakov"
ThisBuild / startYear        := Some(2022)
ThisBuild / licenses         := Seq(License.Apache2)

ThisBuild / scalaVersion      := Scala
ThisBuild / semanticdbEnabled := true

ThisBuild / scalacOptions ++=
  Seq(
    "-deprecation",
    "-explain",
    "-Wunused:imports",
    "-unchecked",
    "-Xfatal-warnings",
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
    libraryDependencies ++= exampleTestLibs,
    testFrameworks += new TestFramework("weaver.framework.CatsEffect")
  )

lazy val site = project
  .in(file("site"))
  .settings(
    libraryDependencies ++= siteLibs
  )
