import Dependencies._

ThisBuild / organization      := "ru.gitflic.artemkorsakov"
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
